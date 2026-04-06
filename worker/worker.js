// Cloudflare Worker - 易学 AI 后端（六爻/八字/黄历统一代理）
// 环境变量: ANTHROPIC_API_KEY, ALLOWED_ORIGIN, LLM_PROVIDER, ANTHROPIC_MODEL
// 可选: OPENAI_API_KEY, OPENAI_BASE_URL, OPENAI_MODEL, OPENROUTER_API_KEY, OPENROUTER_MODEL

export default {
  async fetch(request, env) {
    const origin = request.headers.get("Origin") || "*";
    const allowed = env.ALLOWED_ORIGIN || "*";
    const cors = {
      "Access-Control-Allow-Origin": allowed === "*" ? "*" : origin,
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
      "Access-Control-Max-Age": "86400",
    };
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    if (request.method !== "POST") return new Response(JSON.stringify({ error: "Method not allowed" }), { status: 405, headers: { ...cors, "Content-Type": "application/json" } });

    try {
      const body = await request.json();
      const prompt = buildPrompt(body);
      const provider = (env.LLM_PROVIDER || "anthropic").toLowerCase();
      let result;
      if (provider === "openai" || provider === "openrouter") {
        result = await callOpenAI(env, prompt, provider);
      } else {
        result = await callAnthropic(env, prompt);
      }
      return new Response(JSON.stringify({ reading: result }), { headers: { ...cors, "Content-Type": "application/json" } });
    } catch (err) {
      return new Response(JSON.stringify({ error: err.message }), { status: 500, headers: { ...cors, "Content-Type": "application/json" } });
    }
  },
};

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
1. 确认用神：根据所问事类确定用神（问财取妻财、问官取官鬼、问病以官鬼为病子孙为药、问讼取世应、问婚男取财女取官、问出行取世爻等）
2. 用神旺衰：以月建、日建为纲，判断用神的旺相休囚死
3. 动爻分析：动爻是卦的核心信息，动则有变，静则不论
4. 生克制化：日月对用神的生扶克制、动爻对用神的生克、化出之爻的影响
5. 世应关系：世为己、应为人/事/物，世应相生相克的含义
6. 六神辅断：六神为辅助信息，不可喧宾夺主
7. 伏神：卦中缺失的六亲需查伏神，伏神得飞神生扶则有用
8. 卦变趋势：变卦反映事态走向，化进化退化绝化墓各有吉凶

【关键规则】
- "日月为纲"：月建管旺衰，日建管生克与冲合。月建可旺爻，日建可暗动静爻
- 动爻可以生克其他爻，静爻之间不论生克（《增删卜易》核心原则）
- 用神不可空亡、不可月破、不可入墓、不可化绝——若犯此四忌需特别论述
- 忌神动克用神为凶，原神动生用神为吉
- 六冲卦主散、主动荡；六合卦主成、主稳定
- 反吟（本卦与变卦六冲）主反复不安；伏吟（本卦与变卦相同或自化同）主呻吟痛苦

【禁忌】
- 不可只看卦名而忽略爻位生克，这是义理派的做法，六爻纳甲以爻位生克为核心
- 不可只看六神断吉凶，六神仅为辅助类象
- 不可脱离月建日建空谈旺衰
- 回答中不编造古人语录，引用须准确

你的回答应当：结构清晰、逻辑严密、有理有据、直指核心。每一步判断都要交代依据（什么生什么、什么克什么、得月建还是受月建克等）。语言风格兼具古典底蕴和现代可理解性。`,

  bazi: `你是一位精通四柱八字命理的资深命理师，从业三十余年，熟读并融会贯通以下经典：

【核心典籍修养】
- 《子平真诠》：格局论命的理论巅峰，你精通正格八格（正官、七杀、正财、偏财、正印、偏印、食神、伤官）的成格条件和破格原因。
- 《滴天髓》：命理哲学之最高峰，你深谙「天道」「地道」「人道」的三才论命法，理解「体用」「精神」「气候」等核心概念。
- 《穷通宝鉴》（调候用神）：你熟知每个日主在十二月令的调候需求，知道何时该用火暖、何时该用水润。
- 《三命通会》：命理百科全书，你对纳音论命、神煞体系、格局细论都有深入理解。
- 《千里命稿》（韦千里）：近代实战命理之精华，你从中学到了大量实际案例的断法。
- 《渊海子平》：子平法源头，你理解其对十神生克的原始定义和应用。
- 《神峰通考》：你参考其对用神的独到见解。

【论命核心原则（你必须严格遵守）】
1. 日主旺衰判断是一切的基础。得令（月令生扶）、得地（地支有根）、得势（天干透出帮身之神）三要素综合判断。
2. 格局判断：先看月令透出何神定格，再看格局成败（有情无情、有力无力）。正格看格局，特殊格局（从强、从弱、从儿、化气等）看条件是否满足。
3. 喜用神的确定必须基于格局：扶抑格身弱喜印比、身旺喜食伤财官；从格则顺其势。调候用神在特定季节优先。
4. 大运分析：天干管前五年、地支管后五年（也有合看之说）。大运与命局的生克关系决定十年运势走向。
5. 流年与大运、命局的三者关系是断具体年份吉凶的关键。流年引动命局中的某个字，是应验之机。
6. 合冲刑害穿的作用不可忽略：天干五合（化与不化）、地支六合三合三会、六冲、三刑、相害。
7. 十二长生状态（长生、沐浴、冠带、临官、帝旺、衰、病、死、墓、绝、胎、养）辅助判断各五行在不同地支的实际力量。

【论命风格要求】
- 你像一位德高望重的命理前辈，分析层层递进、逻辑严密
- 先论格局，再论喜忌，后论大运流年，最后给建议
- 每个论断都要交代推理过程，不空口说白话
- 性格分析要结合十神组合的具体含义，不泛泛而谈
- 大运分析要每步运都点评，当前运和近几年要重点展开
- 给出具体的趋吉避凶建议：方位、颜色、行业、贵人属相等
- 回答不少于1500字，结构清晰`,

  huangli: `你是一位精通中国传统择日学的资深择日师，熟读《协纪辨方书》《玉匣记》《象吉通书》《择日精粹》等典籍。

你的任务是根据用户提供的黄历信息，给出当天的详细择日分析和行事指导。

【你必须包含的分析内容】
1. 日辰总论：当日天干地支的五行属性、气场特征。
2. 建除十二神详解：解释当天值日的建除星（建/除/满/平/定/执/破/危/成/收/开/闭）的深层含义，不只是列宜忌，要解释为什么宜/忌。
3. 二十八宿值日分析：当天值日星宿的吉凶性质和适宜之事。
4. 吉神方位详解：喜神、财神、福神方位的具体应用建议（面朝哪个方向办公、出行先往哪个方向等）。
5. 冲煞详解：冲什么生肖、煞在什么方位，这些生肖的人今天要注意什么。
6. 彭祖百忌解读：解释当天天干和地支各自的忌讳及其背后的道理。
7. 今日行事建议：综合以上信息，给出今天适合做什么、不适合做什么的具体建议，语气亲切实用。
8. 特定人群提醒：对做生意的人、学生、上班族、老人等不同人群分别给出当天建议。

【风格要求】
- 语气温和亲切，像一位和蔼的老先生在给邻居讲今天的运势
- 既有传统底蕴又接地气，用现代人能理解的语言解释传统概念
- 回答不少于800字`,
};

// ═══════════════════════════════════════════════════
// 构建提示词
// ═══════════════════════════════════════════════════

function buildPrompt(data) {
  // New unified API: { type: "liuyao"|"bazi"|"huangli", data: "排盘文本", question: "..." }
  const type = (data.type || "liuyao").toLowerCase();
  const systemPrompt = PROMPTS[type] || PROMPTS.liuyao;

  // Unified format from mobile apps
  if (data.data) {
    const question = data.question || "";
    let userPrompt;

    if (type === "liuyao") {
      userPrompt = `以下是完整的纳甲排盘数据，请进行专业解读：

${data.data}

${question ? `【所问之事】${question}` : "【未指定问题】请综合解读运势。"}

请按以下结构详细解读（不少于1000字）：

## 卦象总论
卦宫归属、卦体特征（游魂/归魂/六冲等），整体气象。

## 用神分析
根据所问确定用神，分析旺衰（月建日建）、动静、空亡月破。伏神则分析飞伏关系。

## 世应分析
世爻（求卦人）与应爻（对方）的旺衰、生克关系。

## 动爻与变卦
动爻六亲、动变关系（化进/退/回头生克/墓绝），对用神和世爻的影响。

## 六神辅断
结合动爻六神象义补充。

## 综合断语
明确吉凶判断、具体建议、时间应期。`;
    } else if (type === "bazi") {
      userPrompt = `以下是完整的八字排盘数据，请进行专业论命：

${data.data}

${question ? `【用户问题】${question}` : ""}

请按以下结构详细分析（不少于1500字）：

## 格局分析
日主旺衰判断（得令、得地、得势），格局定性。

## 喜用神
基于格局确定喜用神、忌神，说明理由。

## 性格分析
结合十神组合分析性格特征。

## 大运点评
逐步大运分析，当前大运重点展开。

## 流年分析
当前流年及近年运势分析。

## 趋吉避凶
方位、颜色、行业、贵人属相等具体建议。`;
    } else {
      // huangli
      userPrompt = `以下是今日黄历信息，请给出详细的择日分析和行事指导：

${data.data}

${question ? `【用户问题】${question}` : ""}`;
    }

    return { system: systemPrompt, user: userPrompt };
  }

  // Legacy web frontend format: hexagramData.panText
  if (data.hexagramData && data.hexagramData.panText) {
    const q = data.question || "";
    return {
      system: systemPrompt,
      user: `以下是用户通过铜钱摇卦法得到的完整纳甲排盘：

${data.hexagramData.panText}

${q ? `【所问之事】${q}` : "【未指定问题】请综合解读运势。"}

请按以下结构详细解读（不少于1000字）：

## 卦象总论
卦宫归属、卦体特征（游魂/归魂/六冲等），整体气象。

## 用神分析
根据所问确定用神，分析旺衰（月建日建）、动静、空亡月破。伏神则分析飞伏关系。

## 世应分析
世爻（求卦人）与应爻（对方）的旺衰、生克关系。

## 动爻与变卦
动爻六亲、动变关系（化进/退/回头生克/墓绝），对用神和世爻的影响。

## 六神辅断
结合动爻六神象义补充。

## 综合断语
明确吉凶判断、具体建议、时间应期。`,
    };
  }

  // Legacy web frontend format: old structured liuyao data
  if (data.hexagramName && data.yaos) {
    const {
      hexagramName, palace, palaceWuXing, palaceType,
      upperTrigram, upperWuXing, lowerTrigram, lowerWuXing,
      shiYao, yingYao, yaos, hasChanging, changedHexagramName,
      dayStemBranch, missingRelations, question
    } = data;

    let plateText = "";
    for (let i = 5; i >= 0; i--) {
      const y = yaos[i];
      const shi = y.isShi ? " 【世】" : y.isYing ? " 【应】" : "";
      const chg = y.isChanging ? " ★动★" : "";
      let line = `${y.pos}爻：${y.spirit}　${y.sixRelation}　${y.ganZhi}(${y.wuXing})　${y.isYang ? "▅▅▅▅" : "▅▅ ▅▅"}${chg}${shi}`;
      if (y.isChanging && y.changedGanZhi) {
        line += `　→　${y.changedSixRelation}　${y.changedGanZhi}(${y.changedWuXing})`;
      }
      plateText += line + "\n";
    }

    return {
      system: systemPrompt,
      user: `请为以下六爻卦象进行专业解读。

【卦象排盘】
本卦：${hexagramName}
${hasChanging ? `变卦：${changedHexagramName}` : "无变爻"}
所属：${palace}宫（五行${palaceWuXing}）｜${palaceType}卦
上卦：${upperTrigram}(${upperWuXing})　下卦：${lowerTrigram}(${lowerWuXing})
世爻：第${shiYao}爻　应爻：第${yingYao}爻
起卦日：${dayStemBranch}日

【六爻详情】（从上爻到初爻）
${plateText}
${missingRelations && missingRelations.length > 0 ? `【缺失六亲】${missingRelations.join("、")}（需查伏神）` : ""}
${question ? `\n【所问之事】${question}` : "【未指定问题】请从总体运势角度综合断卦。"}

请按以下结构详细解读（不少于1000字）：
## 卦象总论
## 用神分析
## 动爻详解
## 六神类象
## 世应关系
## 综合断语
## 吉凶判定`,
    };
  }

  // Fallback: pass through systemPrompt and question from legacy worker-mode calls
  if (data.systemPrompt) {
    return { system: data.systemPrompt, user: data.question || data.hexagramData?.panText || "" };
  }

  throw new Error("Invalid request format. Expected { type, data } or legacy hexagram format.");
}

// ═══════════════════════════════════════════════════
// LLM API 调用
// ═══════════════════════════════════════════════════

async function callAnthropic(env, prompt) {
  const apiKey = env.ANTHROPIC_API_KEY;
  if (!apiKey) throw new Error("ANTHROPIC_API_KEY not configured");
  const model = env.ANTHROPIC_MODEL || "claude-sonnet-4-20250514";

  const res = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-api-key": apiKey, "anthropic-version": "2023-06-01" },
    body: JSON.stringify({
      model,
      max_tokens: 4096,
      system: prompt.system,
      messages: [{ role: "user", content: prompt.user }],
    }),
  });

  if (!res.ok) throw new Error(`Anthropic ${res.status}: ${(await res.text()).slice(0, 200)}`);
  const data = await res.json();
  return data.content.filter(b => b.type === "text").map(b => b.text).join("");
}

async function callOpenAI(env, prompt, provider) {
  const apiKey = provider === "openrouter" ? env.OPENROUTER_API_KEY : env.OPENAI_API_KEY;
  if (!apiKey) throw new Error(`${provider} API key not configured`);
  const baseUrl = provider === "openrouter" ? "https://openrouter.ai/api/v1" : (env.OPENAI_BASE_URL || "https://api.openai.com/v1");
  const model = provider === "openrouter" ? (env.OPENROUTER_MODEL || "anthropic/claude-sonnet-4-20250514") : (env.OPENAI_MODEL || "gpt-4o");

  const res = await fetch(`${baseUrl}/chat/completions`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${apiKey}` },
    body: JSON.stringify({
      model,
      max_tokens: 4096,
      messages: [
        { role: "system", content: prompt.system },
        { role: "user", content: prompt.user },
      ],
    }),
  });

  if (!res.ok) throw new Error(`${provider} ${res.status}: ${(await res.text()).slice(0, 200)}`);
  const data = await res.json();
  return data.choices?.[0]?.message?.content || "解析失败";
}
