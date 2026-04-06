// Cloudflare Worker - 六爻纳甲解卦 AI 后端
// 环境变量: ANTHROPIC_API_KEY, ALLOWED_ORIGIN, LLM_PROVIDER, ANTHROPIC_MODEL

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
// 专业六爻纳甲解卦提示词
// ═══════════════════════════════════════════════════

const SYSTEM_PROMPT = `你是一位精通六爻纳甲筮法的资深卦师，深研以下经典并融会贯通：

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

你的回答应当：结构清晰、逻辑严密、有理有据、直指核心。每一步判断都要交代依据（什么生什么、什么克什么、得月建还是受月建克等）。语言风格兼具古典底蕴和现代可理解性。`;

function buildPrompt(data) {
  // New frontend sends panText (pre-formatted najia plate)
  if (data.hexagramData && data.hexagramData.panText) {
    const q = data.question || "";
    return `${SYSTEM_PROMPT}

以下是用户通过铜钱摇卦法得到的完整纳甲排盘：

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
明确吉凶判断、具体建议、时间应期。`;
  }

  // Legacy format from old frontend
  const {
    hexagramName, palace, palaceWuXing, palaceType,
    upperTrigram, upperWuXing, lowerTrigram, lowerWuXing,
    shiYao, yingYao, yaos, hasChanging, changedHexagramName,
    dayStemBranch, missingRelations, question
  } = data;

  // Build plate text
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

  const userPrompt = `请为以下六爻卦象进行专业解读。

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

请按以下结构详细解读：

## 卦象总论
简述本卦卦象特征、所属宫位含义、世应关系大势。

## 用神分析
${question ? `根据所问事类确定用神，分析用神在卦中的位置、旺衰、动静。` : "综合分析世爻旺衰、各六亲状态。"}
- 用神是否得月建（按当前月令推断）生扶？
- 用神是否得日建${dayStemBranch}生扶或克制？
- 用神是否有动爻生克？

## 动爻详解
${hasChanging ? "逐一分析每个动爻：动爻的六亲属性、所化之爻、对用神和世爻的影响。注意化进/化退/化绝/化墓等情况。" : "六爻皆静，分析静卦的特点：事态稳定、以日月建对用神的影响为主。"}

## 六神类象
结合六神提供辅助类象信息（人物、性格、方位、特征等），但不以六神定吉凶。

## 世应关系
分析世爻（求卦人）与应爻（对方/事物）的关系：相生相克、谁旺谁衰。

## 综合断语
给出明确的判断结论：事之成败、时间应期、注意事项。
- 成败判断要有依据
- 时间应期：用神逢值逢合为应期（注明推理过程）
- 如有不利因素，给出化解建议

## 吉凶判定
一句话总结。

回答不少于1000字，每一步判断都要说明依据。`;

  return { system: SYSTEM_PROMPT, user: userPrompt };
}

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
  return data.choices?.[0]?.message?.content || "解卦失败";
}
