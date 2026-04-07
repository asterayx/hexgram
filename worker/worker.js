// Cloudflare Worker - 易学 AI 后端
// 支持 Gemini / Kimi / Grok 三种 LLM 提供商
// 管理后台 WebUI: GET /admin
// AI 代理接口: POST /
// 配置存储: Cloudflare KV

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;

    // CORS
    const origin = request.headers.get("Origin") || "*";
    const allowed = env.ALLOWED_ORIGIN || "*";
    const cors = {
      "Access-Control-Allow-Origin": allowed === "*" ? "*" : origin,
      "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization",
      "Access-Control-Max-Age": "86400",
    };
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });

    // Admin routes
    if (path === "/admin") return serveAdminUI(env);
    if (path.startsWith("/api/admin/")) return handleAdmin(request, env, path, cors);

    // AI proxy
    if (request.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), {
        status: 405, headers: { ...cors, "Content-Type": "application/json" }
      });
    }

    try {
      const body = await request.json();
      const prompt = buildPrompt(body);
      const config = await loadConfig(env);
      if (!config.provider || !config.apiKey || !config.model) {
        throw new Error("AI未配置。请管理员访问 /admin 进行配置。");
      }
      const result = await callLLM(config, prompt);
      return new Response(JSON.stringify({ reading: result }), {
        headers: { ...cors, "Content-Type": "application/json" }
      });
    } catch (err) {
      return new Response(JSON.stringify({ error: err.message }), {
        status: 500, headers: { ...cors, "Content-Type": "application/json" }
      });
    }
  },
};

// ═══════════════════════════════════════════════════
// KV 配置管理（API Key AES-GCM 加密存储）
// ═══════════════════════════════════════════════════

async function deriveKey(env) {
  const secret = env.ENCRYPTION_KEY || env.ADMIN_PASSWORD || "default-key";
  const keyMaterial = await crypto.subtle.importKey(
    "raw", new TextEncoder().encode(secret), "PBKDF2", false, ["deriveKey"]
  );
  return crypto.subtle.deriveKey(
    { name: "PBKDF2", salt: new TextEncoder().encode("hexgram-salt"), iterations: 100000, hash: "SHA-256" },
    keyMaterial, { name: "AES-GCM", length: 256 }, false, ["encrypt", "decrypt"]
  );
}

async function encrypt(text, env) {
  const key = await deriveKey(env);
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encrypted = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv }, key, new TextEncoder().encode(text)
  );
  // iv(12 bytes) + ciphertext → base64
  const buf = new Uint8Array(iv.length + encrypted.byteLength);
  buf.set(iv);
  buf.set(new Uint8Array(encrypted), iv.length);
  return btoa(String.fromCharCode(...buf));
}

async function decrypt(b64, env) {
  const key = await deriveKey(env);
  const buf = Uint8Array.from(atob(b64), c => c.charCodeAt(0));
  const iv = buf.slice(0, 12);
  const data = buf.slice(12);
  const decrypted = await crypto.subtle.decrypt({ name: "AES-GCM", iv }, key, data);
  return new TextDecoder().decode(decrypted);
}

async function loadConfig(env) {
  const raw = await env.CONFIG.get("llm_config");
  if (!raw) return {};
  const config = JSON.parse(raw);
  // Decrypt API key
  if (config.apiKeyEnc) {
    try {
      config.apiKey = await decrypt(config.apiKeyEnc, env);
    } catch {
      config.apiKey = "";
    }
    delete config.apiKeyEnc;
  }
  return config;
}

async function saveConfig(env, config) {
  const toStore = { ...config };
  // Encrypt API key before storing
  if (toStore.apiKey) {
    toStore.apiKeyEnc = await encrypt(toStore.apiKey, env);
    delete toStore.apiKey;
  }
  await env.CONFIG.put("llm_config", JSON.stringify(toStore));
}

// ═══════════════════════════════════════════════════
// Admin API
// ═══════════════════════════════════════════════════

async function handleAdmin(request, env, path, cors) {
  const jsonHeaders = { ...cors, "Content-Type": "application/json" };

  // Auth check
  if (path !== "/api/admin/login") {
    const authOk = await checkAuth(request, env);
    if (!authOk) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401, headers: jsonHeaders
      });
    }
  }

  // Login
  if (path === "/api/admin/login" && request.method === "POST") {
    const { password } = await request.json();
    const adminPwd = env.ADMIN_PASSWORD || "admin";
    if (password === adminPwd) {
      const token = btoa(Date.now() + ":" + adminPwd);
      return new Response(JSON.stringify({ ok: true, token }), { headers: jsonHeaders });
    }
    return new Response(JSON.stringify({ error: "密码错误" }), { status: 401, headers: jsonHeaders });
  }

  // Get config
  if (path === "/api/admin/config" && request.method === "GET") {
    const config = await loadConfig(env);
    // Don't expose full API key
    const safe = { ...config };
    if (safe.apiKey) safe.apiKey = safe.apiKey.slice(0, 8) + "..." + safe.apiKey.slice(-4);
    return new Response(JSON.stringify(safe), { headers: jsonHeaders });
  }

  // Save config
  if (path === "/api/admin/config" && request.method === "POST") {
    const body = await request.json();
    const config = await loadConfig(env);
    if (body.provider) config.provider = body.provider;
    if (body.apiKey) config.apiKey = body.apiKey;
    if (body.model) config.model = body.model;
    await saveConfig(env, config);
    return new Response(JSON.stringify({ ok: true }), { headers: jsonHeaders });
  }

  // Validate key & fetch models
  if (path === "/api/admin/models" && request.method === "POST") {
    const { provider, apiKey } = await request.json();
    try {
      const models = await fetchModels(provider, apiKey);
      return new Response(JSON.stringify({ models }), { headers: jsonHeaders });
    } catch (err) {
      return new Response(JSON.stringify({ error: err.message }), { status: 400, headers: jsonHeaders });
    }
  }

  return new Response(JSON.stringify({ error: "Not found" }), { status: 404, headers: jsonHeaders });
}

function checkAuth(request, env) {
  const auth = request.headers.get("Authorization") || "";
  const token = auth.replace("Bearer ", "");
  try {
    const decoded = atob(token);
    const pwd = decoded.split(":").slice(1).join(":");
    return pwd === (env.ADMIN_PASSWORD || "admin");
  } catch { return false; }
}

// ═══════════════════════════════════════════════════
// 模型列表获取
// ═══════════════════════════════════════════════════

async function fetchModels(provider, apiKey) {
  if (!apiKey) throw new Error("请输入 API Key");

  if (provider === "gemini") {
    const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey}`);
    if (!res.ok) throw new Error(`Gemini API Key 验证失败 (${res.status})`);
    const data = await res.json();
    return (data.models || [])
      .filter(m => m.supportedGenerationMethods && m.supportedGenerationMethods.includes("generateContent"))
      .map(m => ({ id: m.name.replace("models/", ""), name: m.displayName || m.name }));
  }

  if (provider === "kimi") {
    const res = await fetch("https://api.moonshot.cn/v1/models", {
      headers: { Authorization: `Bearer ${apiKey}` }
    });
    if (!res.ok) throw new Error(`Kimi API Key 验证失败 (${res.status})`);
    const data = await res.json();
    return (data.data || []).map(m => ({ id: m.id, name: m.id }));
  }

  if (provider === "grok") {
    const res = await fetch("https://api.x.ai/v1/models", {
      headers: { Authorization: `Bearer ${apiKey}` }
    });
    if (!res.ok) throw new Error(`Grok API Key 验证失败 (${res.status})`);
    const data = await res.json();
    return (data.data || []).map(m => ({ id: m.id, name: m.id }));
  }

  throw new Error("未知的提供商: " + provider);
}

// ═══════════════════════════════════════════════════
// LLM 调用
// ═══════════════════════════════════════════════════

async function callLLM(config, prompt) {
  const { provider, apiKey, model } = config;

  if (provider === "gemini") {
    return callGemini(apiKey, model, prompt);
  }
  if (provider === "kimi") {
    return callOpenAICompat("https://api.moonshot.cn/v1", apiKey, model, prompt);
  }
  if (provider === "grok") {
    return callOpenAICompat("https://api.x.ai/v1", apiKey, model, prompt);
  }
  throw new Error("未配置有效的 LLM 提供商");
}

async function callGemini(apiKey, model, prompt) {
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      system_instruction: { parts: [{ text: prompt.system }] },
      contents: [{ role: "user", parts: [{ text: prompt.user }] }],
      generationConfig: { maxOutputTokens: 8192 },
    }),
  });
  if (!res.ok) throw new Error(`Gemini ${res.status}: ${(await res.text()).slice(0, 200)}`);
  const data = await res.json();
  const parts = data.candidates?.[0]?.content?.parts;
  if (!parts) throw new Error("Gemini 返回为空");
  return parts.map(p => p.text).join("");
}

async function callOpenAICompat(baseUrl, apiKey, model, prompt) {
  const res = await fetch(`${baseUrl}/chat/completions`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${apiKey}` },
    body: JSON.stringify({
      model,
      max_tokens: 8192,
      messages: [
        { role: "system", content: prompt.system },
        { role: "user", content: prompt.user },
      ],
    }),
  });
  if (!res.ok) throw new Error(`LLM ${res.status}: ${(await res.text()).slice(0, 200)}`);
  const data = await res.json();
  return data.choices?.[0]?.message?.content || "解析失败";
}


// ═══════════════════════════════════════════════════
// 系统提示词
// ═══════════════════════════════════════════════════

const PROMPTS = {
  liuyao: `你是一位精通六爻纳甲筮法的资深卦师，深研以下经典并融会贯通：

【核心典籍】
- 《增删卜易》（野鹤老人）：实战断卦之圭臬，用神取用、旺衰判断的核心方法论
- 《卜筮正宗》（王洪绪）：六爻断卦规则体系的集大成之作
- 《易隐》（曹九锡）：高级技法参考，暗动、化绝、反吟伏吟等细节
- 《易冒》（程良玉）：六爻原理的深层理论阐述
- 《火珠林》：纳甲法源头，装卦规则之本

【易学根基】
- 《周易》本经：卦辞、爻辞、十翼
- 《梅花易数》（邵雍）：体用、万物类象
- 《周易尚氏学》（尚秉和）：取象之极致
- 《焦氏易林》：卦变占辞

【断卦原则】
你严格遵循以下六爻断卦逻辑顺序：
1. 确认用神：根据所问事类确定用神
2. 用神旺衰：以月建、日建为纲，判断用神的旺相休囚死
3. 动爻分析：动爻是卦的核心信息，动则有变，静则不论
4. 生克制化：日月对用神的生扶克制、动爻对用神的生克、化出之爻的影响
5. 世应关系：世为己、应为人/事/物
6. 六神辅断：六神为辅助信息，不可喧宾夺主
7. 伏神：卦中缺失的六亲需查伏神
8. 卦变趋势：变卦反映事态走向

【关键规则】
- "日月为纲"：月建管旺衰，日建管生克与冲合
- 动爻可以生克其他爻，静爻之间不论生克
- 用神不可空亡、不可月破、不可入墓、不可化绝
- 六冲卦主散；六合卦主成
- 反吟主反复不安；伏吟主呻吟痛苦

【禁忌】
- 不可只看卦名而忽略爻位生克
- 不可只看六神断吉凶
- 不可脱离月建日建空谈旺衰
- 不编造古人语录

回答结构清晰、逻辑严密、有理有据、直指核心。每一步判断都要交代依据。`,

  bazi: `你是一位精通四柱八字命理的资深命理师，从业三十余年，熟读并融会贯通以下经典：

【核心典籍修养】
- 《子平真诠》：格局论命的理论巅峰
- 《滴天髓》：命理哲学之最高峰
- 《穷通宝鉴》（调候用神）：熟知每个日主在十二月令的调候需求
- 《三命通会》：命理百科全书
- 《千里命稿》（韦千里）：近代实战命理之精华
- 《渊海子平》：子平法源头
- 《神峰通考》：用神的独到见解

【论命核心原则】
1. 日主旺衰判断是基础：得令、得地、得势
2. 格局判断：先看月令透出何神定格
3. 喜用神基于格局确定
4. 大运：天干管前五年、地支管后五年
5. 流年与大运、命局三者关系是断吉凶关键
6. 合冲刑害穿不可忽略

【风格要求】
- 分析层层递进、逻辑严密
- 先论格局，再论喜忌，后论大运流年
- 每个论断交代推理过程
- 大运每步都点评，当前运重点展开
- 给出具体趋吉避凶建议
- 回答不少于1500字`,

  huangli: `你是一位精通中国传统择日学的资深择日师，熟读《协纪辨方书》《玉匣记》《象吉通书》《择日精粹》等典籍。

【分析内容】
1. 日辰总论：当日天干地支五行属性、气场特征
2. 建除十二神详解
3. 二十八宿值日分析
4. 吉神方位详解
5. 冲煞详解
6. 彭祖百忌解读
7. 今日行事建议
8. 特定人群提醒

【风格要求】
- 语气温和亲切
- 既有传统底蕴又接地气
- 回答不少于800字`,
};

// ═══════════════════════════════════════════════════
// 构建提示词
// ═══════════════════════════════════════════════════

function buildPrompt(data) {
  const type = (data.type || "liuyao").toLowerCase();
  const systemPrompt = PROMPTS[type] || PROMPTS.liuyao;

  // Unified format: { type, data, question }
  if (data.data) {
    const question = data.question || "";
    let userPrompt;
    if (type === "liuyao") {
      userPrompt = `以下是完整的纳甲排盘数据，请进行专业解读：\n\n${data.data}\n\n${question ? `【所问之事】${question}` : "【未指定问题】请综合解读运势。"}\n\n请按以下结构详细解读（不少于1000字）：\n\n## 卦象总论\n## 用神分析\n## 世应分析\n## 动爻与变卦\n## 六神辅断\n## 综合断语`;
    } else if (type === "bazi") {
      userPrompt = `以下是完整的八字排盘数据，请进行专业论命：\n\n${data.data}\n\n${question ? `【用户问题】${question}` : ""}\n\n请按以下结构详细分析（不少于1500字）：\n\n## 格局分析\n## 喜用神\n## 性格分析\n## 大运点评\n## 流年分析\n## 趋吉避凶`;
    } else {
      userPrompt = `以下是今日黄历信息，请给出详细的择日分析和行事指导：\n\n${data.data}\n\n${question ? `【用户问题】${question}` : ""}`;
    }
    return { system: systemPrompt, user: userPrompt };
  }

  // Legacy web format: hexagramData.panText
  if (data.hexagramData && data.hexagramData.panText) {
    const q = data.question || "";
    return {
      system: systemPrompt,
      user: `以下是用户通过铜钱摇卦法得到的完整纳甲排盘：\n\n${data.hexagramData.panText}\n\n${q ? `【所问之事】${q}` : "【未指定问题】请综合解读运势。"}\n\n请按以下结构详细解读（不少于1000字）：\n\n## 卦象总论\n## 用神分析\n## 世应分析\n## 动爻与变卦\n## 六神辅断\n## 综合断语`,
    };
  }

  // Legacy: old structured liuyao data
  if (data.hexagramName && data.yaos) {
    const { hexagramName, palace, palaceWuXing, palaceType, upperTrigram, upperWuXing, lowerTrigram, lowerWuXing, shiYao, yingYao, yaos, hasChanging, changedHexagramName, dayStemBranch, missingRelations, question } = data;
    let plateText = "";
    for (let i = 5; i >= 0; i--) {
      const y = yaos[i];
      const shi = y.isShi ? " 【世】" : y.isYing ? " 【应】" : "";
      const chg = y.isChanging ? " ★动★" : "";
      let line = `${y.pos}爻：${y.spirit}　${y.sixRelation}　${y.ganZhi}(${y.wuXing})　${y.isYang ? "▅▅▅▅" : "▅▅ ▅▅"}${chg}${shi}`;
      if (y.isChanging && y.changedGanZhi) line += `　→　${y.changedSixRelation}　${y.changedGanZhi}(${y.changedWuXing})`;
      plateText += line + "\n";
    }
    return {
      system: systemPrompt,
      user: `本卦：${hexagramName}\n${hasChanging ? `变卦：${changedHexagramName}` : "无变爻"}\n所属：${palace}宫（${palaceWuXing}）｜${palaceType}卦\n世爻：第${shiYao}爻　应爻：第${yingYao}爻\n起卦日：${dayStemBranch}日\n\n${plateText}\n${question || "请综合断卦。"}`,
    };
  }

  // Fallback: pass through
  if (data.systemPrompt) {
    return { system: data.systemPrompt, user: data.question || "" };
  }

  throw new Error("Invalid request format");
}

// ═══════════════════════════════════════════════════
// Admin WebUI
// ═══════════════════════════════════════════════════

function serveAdminUI() {
  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>易学三合 - 管理后台</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{background:#0d0b08;color:#e8dcc8;font-family:'Noto Serif SC',serif;min-height:100vh;display:flex;justify-content:center;align-items:flex-start;padding:20px}
.container{max-width:520px;width:100%;padding:24px}
h1{color:#c9a96e;font-size:22px;text-align:center;margin-bottom:4px}
.sub{color:#8b7355;font-size:12px;text-align:center;margin-bottom:24px}
.card{background:#1a1510;border:1px solid #3d3425;border-radius:10px;padding:20px;margin-bottom:16px}
.card h2{color:#f5deb3;font-size:15px;margin-bottom:14px}
label{display:block;color:#8b7355;font-size:12px;margin-bottom:4px;margin-top:12px}
label:first-child{margin-top:0}
input,select{width:100%;padding:10px 12px;background:#13100c;border:1px solid #3d3425;border-radius:8px;color:#e8dcc8;font-size:14px;font-family:inherit;outline:none}
input:focus,select:focus{border-color:#c9a96e}
select{cursor:pointer;-webkit-appearance:none;appearance:none;background-image:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%238b7355' d='M6 8L1 3h10z'/%3E%3C/svg%3E");background-repeat:no-repeat;background-position:right 12px center}
select option{background:#1a1510;color:#e8dcc8}
.btn{display:block;width:100%;padding:12px;border:none;border-radius:8px;font-size:15px;font-family:inherit;font-weight:bold;cursor:pointer;margin-top:16px;transition:opacity .2s}
.btn-gold{background:#c9a96e;color:#0d0b08}
.btn-ghost{background:transparent;border:1px solid #c9a96e;color:#c9a96e}
.btn:hover{opacity:.85}
.btn:disabled{opacity:.4;cursor:not-allowed}
.status{padding:10px;border-radius:8px;margin-top:12px;font-size:13px;text-align:center}
.status.ok{background:#1a2e1a;color:#6ecf6e;border:1px solid #2a4a2a}
.status.err{background:#2e1a1a;color:#cf6e6e;border:1px solid #4a2a2a}
.status.info{background:#1a1a2e;color:#6e9ecf;border:1px solid #2a2a4a}
.model-grid{display:grid;grid-template-columns:1fr;gap:6px;max-height:260px;overflow-y:auto;margin-top:8px;padding-right:4px}
.model-item{padding:10px 12px;background:#13100c;border:1px solid #3d3425;border-radius:6px;cursor:pointer;font-size:13px;transition:all .15s}
.model-item:hover{border-color:#c9a96e;background:#1a1510}
.model-item.selected{border-color:#c9a96e;background:#c9a96e22}
.model-item .mid{color:#e8dcc8;font-weight:500}
.model-item .mname{color:#8b7355;font-size:11px;margin-top:2px}
.current{display:flex;justify-content:space-between;align-items:center;padding:10px 14px;background:#13100c;border:1px solid #3d3425;border-radius:8px;margin-top:12px}
.current .label{color:#8b7355;font-size:11px}
.current .value{color:#c9a96e;font-size:13px;font-weight:500}
.hidden{display:none}
.login-wrap{display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:60vh}
</style>
</head>
<body>
<div class="container">

<!-- Login -->
<div id="loginPage" class="login-wrap">
  <h1>易学三合</h1>
  <p class="sub">管理后台</p>
  <div class="card" style="width:100%">
    <label>管理密码</label>
    <input type="password" id="pwd" placeholder="请输入管理密码" onkeydown="if(event.key==='Enter')doLogin()">
    <button class="btn btn-gold" onclick="doLogin()">登录</button>
    <div id="loginMsg"></div>
  </div>
</div>

<!-- Admin Panel -->
<div id="adminPage" class="hidden">
  <h1>易学三合</h1>
  <p class="sub">AI 后端管理</p>

  <!-- Current config -->
  <div class="card">
    <h2>当前配置</h2>
    <div id="currentConfig">
      <div class="current"><span class="label">提供商</span><span class="value" id="curProvider">未配置</span></div>
      <div class="current"><span class="label">模型</span><span class="value" id="curModel">未配置</span></div>
      <div class="current"><span class="label">API Key</span><span class="value" id="curKey">未配置</span></div>
    </div>
  </div>

  <!-- Provider selection -->
  <div class="card">
    <h2>选择提供商</h2>
    <label>LLM 提供商</label>
    <select id="selProvider">
      <option value="">-- 请选择 --</option>
      <option value="gemini">Google Gemini</option>
      <option value="kimi">Kimi (Moonshot)</option>
      <option value="grok">Grok (xAI)</option>
    </select>

    <label>API Key</label>
    <input type="password" id="apiKey" placeholder="输入 API Key">

    <button class="btn btn-ghost" onclick="doFetchModels()" id="btnFetch">验证 Key 并获取模型列表</button>
    <div id="fetchMsg"></div>
  </div>

  <!-- Model selection -->
  <div class="card hidden" id="modelCard">
    <h2>选择模型</h2>
    <div id="modelList" class="model-grid"></div>
    <input type="hidden" id="selectedModel" value="">
    <button class="btn btn-gold" onclick="doSave()" id="btnSave" disabled>保存配置</button>
    <div id="saveMsg"></div>
  </div>
</div>

</div>

<script>
let TOKEN = '';
const API = '';

async function doLogin() {
  const pwd = document.getElementById('pwd').value;
  const msgEl = document.getElementById('loginMsg');
  try {
    const res = await fetch(API + '/api/admin/login', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({password: pwd})
    });
    const data = await res.json();
    if (data.ok) {
      TOKEN = data.token;
      document.getElementById('loginPage').classList.add('hidden');
      document.getElementById('adminPage').classList.remove('hidden');
      loadCurrentConfig();
    } else {
      msgEl.innerHTML = '<div class="status err">' + (data.error || '登录失败') + '</div>';
    }
  } catch(e) {
    msgEl.innerHTML = '<div class="status err">网络错误</div>';
  }
}

async function loadCurrentConfig() {
  try {
    const res = await fetch(API + '/api/admin/config', {
      headers: {'Authorization': 'Bearer ' + TOKEN}
    });
    const cfg = await res.json();
    const providerNames = {gemini:'Google Gemini', kimi:'Kimi (Moonshot)', grok:'Grok (xAI)'};
    document.getElementById('curProvider').textContent = providerNames[cfg.provider] || '未配置';
    document.getElementById('curModel').textContent = cfg.model || '未配置';
    document.getElementById('curKey').textContent = cfg.apiKey || '未配置';
  } catch(e) {}
}

async function doFetchModels() {
  const provider = document.getElementById('selProvider').value;
  const apiKey = document.getElementById('apiKey').value;
  const msgEl = document.getElementById('fetchMsg');
  const btn = document.getElementById('btnFetch');

  if (!provider) { msgEl.innerHTML = '<div class="status err">请选择提供商</div>'; return; }
  if (!apiKey) { msgEl.innerHTML = '<div class="status err">请输入 API Key</div>'; return; }

  btn.disabled = true;
  btn.textContent = '验证中...';
  msgEl.innerHTML = '<div class="status info">正在验证 API Key 并获取模型列表...</div>';

  try {
    const res = await fetch(API + '/api/admin/models', {
      method: 'POST',
      headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + TOKEN},
      body: JSON.stringify({provider, apiKey})
    });
    const data = await res.json();
    if (data.error) throw new Error(data.error);

    const models = data.models || [];
    if (models.length === 0) throw new Error('未找到可用模型');

    msgEl.innerHTML = '<div class="status ok">验证成功！找到 ' + models.length + ' 个可用模型</div>';
    document.getElementById('modelCard').classList.remove('hidden');

    const listEl = document.getElementById('modelList');
    listEl.innerHTML = '';
    models.forEach(m => {
      const div = document.createElement('div');
      div.className = 'model-item';
      div.innerHTML = '<div class="mid">' + m.id + '</div>' + (m.name !== m.id ? '<div class="mname">' + m.name + '</div>' : '');
      div.onclick = () => selectModel(m.id, div);
      listEl.appendChild(div);
    });

    document.getElementById('selectedModel').value = '';
    document.getElementById('btnSave').disabled = true;
  } catch(e) {
    msgEl.innerHTML = '<div class="status err">' + e.message + '</div>';
    document.getElementById('modelCard').classList.add('hidden');
  } finally {
    btn.disabled = false;
    btn.textContent = '验证 Key 并获取模型列表';
  }
}

function selectModel(modelId, el) {
  document.querySelectorAll('.model-item').forEach(e => e.classList.remove('selected'));
  el.classList.add('selected');
  document.getElementById('selectedModel').value = modelId;
  document.getElementById('btnSave').disabled = false;
}

async function doSave() {
  const provider = document.getElementById('selProvider').value;
  const apiKey = document.getElementById('apiKey').value;
  const model = document.getElementById('selectedModel').value;
  const msgEl = document.getElementById('saveMsg');
  const btn = document.getElementById('btnSave');

  if (!model) { msgEl.innerHTML = '<div class="status err">请选择模型</div>'; return; }

  btn.disabled = true;
  btn.textContent = '保存中...';

  try {
    const res = await fetch(API + '/api/admin/config', {
      method: 'POST',
      headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + TOKEN},
      body: JSON.stringify({provider, apiKey, model})
    });
    const data = await res.json();
    if (data.ok) {
      msgEl.innerHTML = '<div class="status ok">配置已保存！</div>';
      loadCurrentConfig();
    } else {
      throw new Error(data.error || '保存失败');
    }
  } catch(e) {
    msgEl.innerHTML = '<div class="status err">' + e.message + '</div>';
  } finally {
    btn.disabled = false;
    btn.textContent = '保存配置';
  }
}
</script>
</body>
</html>`;

  return new Response(html, {
    headers: { "Content-Type": "text/html;charset=UTF-8" }
  });
}
