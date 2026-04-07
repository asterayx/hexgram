/**
 * 六爻纳甲装卦算法
 * 
 * 实现：纳甲、六亲、世应、六神、伏神、日月建旺衰、动变分析
 * 依据：《卜筮正宗》《增删卜易》《火珠林》
 */

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 基础数据
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

const TIANGAN = ["甲","乙","丙","丁","戊","己","庚","辛","壬","癸"];
const DIZHI = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"];
const WUXING_TIANGAN = {甲:"木",乙:"木",丙:"火",丁:"火",戊:"土",己:"土",庚:"金",辛:"金",壬:"水",癸:"水"};
const WUXING_DIZHI = {子:"水",丑:"土",寅:"木",卯:"木",辰:"土",巳:"火",午:"火",未:"土",申:"金",酉:"金",戌:"土",亥:"水"};

// 五行生克
const WX_SHENG = {木:"火",火:"土",土:"金",金:"水",水:"木"};
const WX_KE = {木:"土",土:"水",水:"火",火:"金",金:"木"};

// 六亲：以卦宫五行为"我"
// 生我 = 父母，我生 = 子孙，克我 = 官鬼，我克 = 妻财，同我 = 兄弟
function getLiuqin(gongWx, yaoWx) {
  if (gongWx === yaoWx) return "兄弟";
  if (WX_SHENG[gongWx] === yaoWx) return "子孙";   // 我生
  if (WX_SHENG[yaoWx] === gongWx) return "父母";   // 生我
  if (WX_KE[gongWx] === yaoWx) return "妻财";       // 我克
  if (WX_KE[yaoWx] === gongWx) return "官鬼";       // 克我
  return "?";
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 八宫八卦与纳甲
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// 经卦象名：用于构造全卦名（如"泽火革"）
const GUA_XIANG = {"乾":"天","坤":"地","坎":"水","离":"火","震":"雷","巽":"风","艮":"山","兑":"泽"};

// 八纯卦（经卦）二进制：从初爻到上爻
const BAGUA = {
  "111": {name:"乾", wx:"金", najia_yang:"甲", najia_yin:"壬"},
  "000": {name:"坤", wx:"土", najia_yang:"乙", najia_yin:"癸"},
  "010": {name:"坎", wx:"水", najia_yang:"戊", najia_yin:"戊"},
  "101": {name:"离", wx:"火", najia_yang:"己", najia_yin:"己"},
  "100": {name:"震", wx:"木", najia_yang:"庚", najia_yin:"庚"},
  "011": {name:"巽", wx:"木", najia_yang:"辛", najia_yin:"辛"},
  "001": {name:"艮", wx:"土", najia_yang:"丙", najia_yin:"丙"},
  "110": {name:"兑", wx:"金", najia_yang:"丁", najia_yin:"丁"},
};

// 纳甲规则：每个经卦的六个地支
// 乾（甲/壬）内卦：子寅辰，外卦：午申戌
// 坤（乙/癸）内卦：未巳卯，外卦：丑亥酉
// 坎（戊）内卦：寅辰午，外卦：申戌子
// 离（己）内卦：卯丑亥，外卦：酉未巳
// 震（庚）内卦：子寅辰，外卦：午申戌
// 巽（辛）内卦：丑亥酉，外卦：未巳卯
// 艮（丙）内卦：辰午申，外卦：戌子寅
// 兑（丁）内卦：巳卯丑，外卦：亥酉未

const NAJIA_DIZHI = {
  // [内卦初二三, 外卦四五上]
  "乾": {inner:["子","寅","辰"], outer:["午","申","戌"]},
  "坤": {inner:["未","巳","卯"], outer:["丑","亥","酉"]},
  "坎": {inner:["寅","辰","午"], outer:["申","戌","子"]},
  "离": {inner:["卯","丑","亥"], outer:["酉","未","巳"]},
  "震": {inner:["子","寅","辰"], outer:["午","申","戌"]},
  "巽": {inner:["丑","亥","酉"], outer:["未","巳","卯"]},
  "艮": {inner:["辰","午","申"], outer:["戌","子","寅"]},
  "兑": {inner:["巳","卯","丑"], outer:["亥","酉","未"]},
};

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 八宫六十四卦世应表
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// 八宫排列规则：
// 第1卦：八纯卦（本宫卦），世在六爻
// 第2卦：初爻变，世在初爻
// 第3卦：二爻变，世在二爻
// 第4卦：三爻变，世在三爻
// 第5卦：四爻变，世在四爻
// 第6卦：五爻变，世在五爻
// 第7卦：四爻变回（游魂），世在四爻
// 第8卦：下卦全变回（归魂），世在三爻

function getGuaBits(key) {
  // key = "111111" 等六位二进制，从初爻到上爻
  return key.split("").map(Number);
}

function flipBit(b) { return b === 1 ? 0 : 1; }

function buildBaGong() {
  const pureGua = ["111","000","010","101","100","011","001","110"];
  const result = {}; // key(6bit) -> {gong, guaXu, shi, ying}

  for (const pg of pureGua) {
    const guaName = BAGUA[pg].name;
    const bits6 = pg.split("").map(Number);

    // 第1卦：八纯卦
    let current = [...bits6, ...bits6]; // 内外相同
    let key = current.join("");
    result[key] = {gong: guaName, guaXu: 1, shi: 5, ying: 2}; // 世在六爻(index 5), 应在三爻(index 2)

    // 第2-6卦：从初爻开始逐爻变
    let prev = [...current];
    for (let i = 0; i < 5; i++) {
      prev[i] = flipBit(bits6[i % 3 === i ? i : i]); // 变初爻到五爻
      // 重新计算：从八纯卦开始变
      let cur = [...bits6, ...bits6];
      for (let j = 0; j <= i; j++) cur[j] = flipBit(cur[j]);
      key = cur.join("");
      const shiIdx = i; // 世在变爻位置
      const yingIdx = (shiIdx + 3) % 6;
      result[key] = {gong: guaName, guaXu: i + 2, shi: shiIdx, ying: yingIdx};
    }

    // 第7卦（游魂）：前5爻都变了，再把四爻变回
    let you = [...bits6, ...bits6];
    for (let j = 0; j < 5; j++) you[j] = flipBit(you[j]);
    you[3] = flipBit(you[3]); // 四爻变回 = 再变一次
    key = you.join("");
    result[key] = {gong: guaName, guaXu: 7, shi: 3, ying: 0, youHun: true};

    // 第8卦（归魂）：下卦变回本宫
    let gui = [...you];
    gui[0] = bits6[0]; gui[1] = bits6[1]; gui[2] = bits6[2];
    key = gui.join("");
    result[key] = {gong: guaName, guaXu: 8, shi: 2, ying: 5, guiHun: true};
  }

  return result;
}

const BA_GONG_TABLE = buildBaGong();

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 六神（六兽）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// 六神按日干排列，从初爻到上爻顺序
// 甲乙日起青龙，丙丁日起朱雀，戊日起勾陈，己日起螣蛇，庚辛日起白虎，壬癸日起玄武
const LIUSHEN_ORDER = ["青龙","朱雀","勾陈","螣蛇","白虎","玄武"];
const LIUSHEN_START = {甲:0,乙:0,丙:1,丁:1,戊:2,己:3,庚:4,辛:4,壬:5,癸:5};

function getLiushen(riGan) {
  const start = LIUSHEN_START[riGan] || 0;
  return [0,1,2,3,4,5].map(i => LIUSHEN_ORDER[(start + i) % 6]);
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 日月建旺衰判断
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// 十二长生：传入五行和地支，返回状态
const CHANGSHENG = {
  // 五行的十二长生起始（阳干）
  木: {亥:"长生",子:"沐浴",丑:"冠带",寅:"临官",卯:"帝旺",辰:"衰",巳:"病",午:"死",未:"墓",申:"绝",酉:"胎",戌:"养"},
  火: {寅:"长生",卯:"沐浴",辰:"冠带",巳:"临官",午:"帝旺",未:"衰",申:"病",酉:"死",戌:"墓",亥:"绝",子:"胎",丑:"养"},
  土: {寅:"长生",卯:"沐浴",辰:"冠带",巳:"临官",午:"帝旺",未:"衰",申:"病",酉:"死",戌:"墓",亥:"绝",子:"胎",丑:"养"},
  金: {巳:"长生",午:"沐浴",未:"冠带",申:"临官",酉:"帝旺",戌:"衰",亥:"病",子:"死",丑:"墓",寅:"绝",卯:"胎",辰:"养"},
  水: {申:"长生",酉:"沐浴",戌:"冠带",亥:"临官",子:"帝旺",丑:"衰",寅:"病",卯:"死",辰:"墓",巳:"绝",午:"胎",未:"养"},
};

function getWangShuai(wx, zhi) {
  const status = CHANGSHENG[wx]?.[zhi];
  if (!status) return "平";
  if (["临官","帝旺"].includes(status)) return "旺";
  if (["长生","冠带","沐浴"].includes(status)) return "相";
  if (["墓","死","绝"].includes(status)) return "衰";
  if (["病","胎","养"].includes(status)) return "弱";
  return "平";
}

// 月建/日建对爻的影响
function getYueJianEffect(yaoDzWx, yueZhi) {
  const yueWx = WUXING_DIZHI[yueZhi];
  if (yaoDzWx === yueWx) return "月建比和，旺";
  if (WX_SHENG[yueWx] === yaoDzWx) return "月建生之，旺";
  if (WX_KE[yueWx] === yaoDzWx) return "月建克之，弱（月破需验地支冲）";
  if (WX_SHENG[yaoDzWx] === yueWx) return "泄气于月建，平";
  if (WX_KE[yaoDzWx] === yueWx) return "耗力于月建，平";
  return "平";
}

// 六冲判断
const LIUCHONG = {子:"午",丑:"未",寅:"申",卯:"酉",辰:"戌",巳:"亥",午:"子",未:"丑",申:"寅",酉:"卯",戌:"辰",亥:"巳"};

function isChong(zhi1, zhi2) {
  return LIUCHONG[zhi1] === zhi2;
}

// 空亡：根据日柱地支，甲子旬中空亡
const XUNKONG = {
  // 甲子旬：戌亥空，甲戌旬：申酉空...
  // 简化：根据日干支序号计算
};

function getKongWang(riGan, riZhi) {
  const tgIdx = TIANGAN.indexOf(riGan);
  const dzIdx = DIZHI.indexOf(riZhi);
  // 甲子旬首：天干序 - 地支序 (mod 12)，确定旬首
  // 空亡为旬中未用到的两个地支
  let diff = dzIdx - tgIdx;
  if (diff < 0) diff += 12;
  const xunStart = (dzIdx - diff + 12) % 12; // 旬首地支
  // 空亡 = 旬首+10, 旬首+11 (mod 12)
  const kong1 = DIZHI[(xunStart + 10) % 12];
  const kong2 = DIZHI[(xunStart + 11) % 12];
  return [kong1, kong2];
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 核心：装卦
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * 完整装卦
 * @param {number[]} lines - 6个值(6/7/8/9)，从初爻到上爻
 * @param {object} options - { riGan, riZhi, yueZhi } 日干支和月支（可选）
 * @returns {object} 完整卦象数据
 */
function zhuangGua(lines, options = {}) {
  const { riGan = "甲", riZhi = "子", yueZhi = "子" } = options;

  // 1. 基本爻信息
  const baseBits = lines.map(v => (v === 7 || v === 9) ? 1 : 0);
  const changedBits = lines.map(v => {
    if (v === 6) return 1; // 老阴变阳
    if (v === 9) return 0; // 老阳变阴
    return (v === 7 || v === 9) ? 1 : 0;
  });
  const changingIdx = lines.map((v, i) => (v === 6 || v === 9) ? i : -1).filter(i => i >= 0);

  const baseKey = baseBits.join("");
  const changedKey = changedBits.join("");
  const innerKey = baseKey.slice(0, 3);
  const outerKey = baseKey.slice(3, 6);

  const innerGua = BAGUA[innerKey];
  const outerGua = BAGUA[outerKey];

  // 2. 查八宫归属、世应
  const gongInfo = BA_GONG_TABLE[baseKey] || {gong: innerGua?.name || "?", shi: 4, ying: 1};
  const gongWx = BAGUA[Object.keys(BAGUA).find(k => BAGUA[k].name === gongInfo.gong)]?.wx || "土";

  // 3. 纳甲：为每爻分配天干地支
  const yaos = [];
  for (let i = 0; i < 6; i++) {
    const isInner = i < 3;
    const gua = isInner ? innerGua : outerGua;
    const guaName = gua.name;
    const localIdx = isInner ? i : i - 3;

    // 天干
    const tg = isInner ? gua.najia_yang : gua.najia_yin;
    // 地支
    const dzArr = isInner ? NAJIA_DIZHI[guaName].inner : NAJIA_DIZHI[guaName].outer;
    const dz = dzArr[localIdx];
    const dzWx = WUXING_DIZHI[dz];

    // 六亲
    const liuqin = getLiuqin(gongWx, dzWx);

    // 旺衰
    const yueEffect = getYueJianEffect(dzWx, yueZhi);
    const riWang = getWangShuai(dzWx, riZhi);

    yaos.push({
      pos: i, // 0=初爻, 5=上爻
      posName: ["初","二","三","四","五","上"][i],
      yinYang: baseBits[i] === 1 ? "阳" : "阴",
      value: lines[i], // 6/7/8/9
      valueLabel: {6:"老阴",7:"少阳",8:"少阴",9:"老阳"}[lines[i]],
      isDong: lines[i] === 6 || lines[i] === 9,
      tiangan: tg,
      dizhi: dz,
      wuxing: dzWx,
      liuqin: liuqin,
      isShi: i === gongInfo.shi,
      isYing: i === gongInfo.ying,
      yueEffect: yueEffect,
      riWangShuai: riWang,
    });
  }

  // 4. 六神
  const liushenArr = getLiushen(riGan);
  yaos.forEach((y, i) => { y.liushen = liushenArr[i]; });

  // 5. 空亡
  const kongWang = getKongWang(riGan, riZhi);
  yaos.forEach(y => {
    y.isKong = kongWang.includes(y.dizhi);
  });

  // 6. 变卦处理
  let changedYaos = null;
  if (changingIdx.length > 0) {
    const chInnerKey = changedKey.slice(0, 3);
    const chOuterKey = changedKey.slice(3, 6);
    const chInnerGua = BAGUA[chInnerKey];
    const chOuterGua = BAGUA[chOuterKey];
    const chGongInfo = BA_GONG_TABLE[changedKey];

    changedYaos = [];
    for (let i = 0; i < 6; i++) {
      const isInner = i < 3;
      const gua = isInner ? chInnerGua : chOuterGua;
      const guaName = gua.name;
      const localIdx = isInner ? i : i - 3;
      const tg = isInner ? gua.najia_yang : gua.najia_yin;
      const dzArr = isInner ? NAJIA_DIZHI[guaName].inner : NAJIA_DIZHI[guaName].outer;
      const dz = dzArr[localIdx];
      const dzWx = WUXING_DIZHI[dz];
      const liuqin = getLiuqin(gongWx, dzWx); // 仍以本卦宫位论六亲

      changedYaos.push({
        pos: i,
        posName: ["初","二","三","四","五","上"][i],
        yinYang: changedBits[i] === 1 ? "阳" : "阴",
        tiangan: tg,
        dizhi: dz,
        wuxing: dzWx,
        liuqin: liuqin,
        isChanged: changingIdx.includes(i),
      });
    }
  }

  // 7. 伏神分析
  // 如果本卦中缺少某个六亲（用神不现），则需要从本宫八纯卦中找到该六亲的爻作为伏神
  const presentLiuqin = new Set(yaos.map(y => y.liuqin));
  const allLiuqin = ["父母","兄弟","子孙","妻财","官鬼"];
  const missingLiuqin = allLiuqin.filter(lq => !presentLiuqin.has(lq));

  const fuShen = [];
  if (missingLiuqin.length > 0) {
    // 找本宫八纯卦
    const gongKey = Object.keys(BAGUA).find(k => BAGUA[k].name === gongInfo.gong);
    if (gongKey) {
      const pureKey = gongKey + gongKey; // 八纯卦
      const pureInner = BAGUA[gongKey];
      const pureOuter = BAGUA[gongKey];
      for (let i = 0; i < 6; i++) {
        const isInner = i < 3;
        const guaName = pureInner.name; // 八纯卦上下相同
        const localIdx = isInner ? i : i - 3;
        const tg = isInner ? pureInner.najia_yang : pureInner.najia_yin;
        const dzArr = isInner ? NAJIA_DIZHI[guaName].inner : NAJIA_DIZHI[guaName].outer;
        const dz = dzArr[localIdx];
        const dzWx = WUXING_DIZHI[dz];
        const lq = getLiuqin(gongWx, dzWx);
        if (missingLiuqin.includes(lq)) {
          fuShen.push({
            liuqin: lq,
            tiangan: tg,
            dizhi: dz,
            wuxing: dzWx,
            fuUnder: i, // 伏于本卦第i爻之下
          });
        }
      }
    }
  }

  // 8. 动变关系分析
  const dongBianAnalysis = [];
  if (changingIdx.length > 0 && changedYaos) {
    for (const idx of changingIdx) {
      const benYao = yaos[idx];
      const bianYao = changedYaos[idx];
      let relation = "";
      const bWx = benYao.wuxing;
      const cWx = bianYao.wuxing;
      if (bWx === cWx) relation = "比和（化同）";
      else if (WX_SHENG[bWx] === cWx) relation = "化泄";
      else if (WX_SHENG[cWx] === bWx) relation = "化回头生（吉）";
      else if (WX_KE[bWx] === cWx) relation = "化克出";
      else if (WX_KE[cWx] === bWx) relation = "化回头克（凶）";

      // 化进化退
      const dzOrder = DIZHI;
      const benIdx = dzOrder.indexOf(benYao.dizhi);
      const bianIdx = dzOrder.indexOf(bianYao.dizhi);
      let jinTui = "";
      if (benYao.wuxing === bianYao.wuxing) {
        // 同五行看地支进退
        // 化进：地支向旺方移动；化退：向衰方
        jinTui = "(同五行化比)";
      }

      // 化墓化绝
      const bianStatus = CHANGSHENG[bWx]?.[bianYao.dizhi] || "";
      let teXing = "";
      if (bianStatus === "墓") teXing = "⚠ 化入墓";
      else if (bianStatus === "绝") teXing = "⚠ 化入绝";

      dongBianAnalysis.push({
        yaoPos: benYao.posName,
        benGan: benYao.tiangan, benZhi: benYao.dizhi, benWx: benYao.wuxing, benLq: benYao.liuqin,
        bianGan: bianYao.tiangan, bianZhi: bianYao.dizhi, bianWx: bianYao.wuxing, bianLq: bianYao.liuqin,
        relation, jinTui, teXing, bianStatus,
      });
    }
  }

  // 9. 六冲六合判断
  // 卦自身是否六冲：上下卦地支全冲
  let isLiuChongGua = true;
  for (let i = 0; i < 3; i++) {
    if (!isChong(yaos[i].dizhi, yaos[i + 3].dizhi)) {
      isLiuChongGua = false;
      break;
    }
  }

  // 返回结果
  const HEXAGRAM_TABLE = {
    "111111":"乾","111110":"夬","111101":"大有","111100":"大壮","111011":"小畜","111010":"需","111001":"大畜","111000":"泰",
    "110111":"履","110110":"兑","110101":"睽","110100":"归妹","110011":"中孚","110010":"节","110001":"损","110000":"临",
    "101111":"同人","101110":"革","101101":"离","101100":"丰","101011":"家人","101010":"既济","101001":"贲","101000":"明夷",
    "100111":"无妄","100110":"随","100101":"噬嗑","100100":"震","100011":"益","100010":"屯","100001":"颐","100000":"复",
    "011111":"姤","011110":"大过","011101":"鼎","011100":"恒","011011":"巽","011010":"井","011001":"蛊","011000":"升",
    "010111":"讼","010110":"困","010101":"未济","010100":"解","010011":"涣","010010":"坎","010001":"蒙","010000":"师",
    "001111":"遁","001110":"咸","001101":"旅","001100":"小过","001011":"渐","001010":"蹇","001001":"艮","001000":"谦",
    "000111":"否","000110":"萃","000101":"晋","000100":"豫","000011":"观","000010":"比","000001":"剥","000000":"坤"
  };

  // 构造全卦名：八纯卦用"X为Y"（如"乾为天"），其余用"外象内象名"（如"泽火革"）
  function fullGuaName(key) {
    const short = HEXAGRAM_TABLE[key] || "未知";
    const ik = key.slice(0, 3), ok = key.slice(3, 6);
    const ig = BAGUA[ik], og = BAGUA[ok];
    if (!ig || !og) return short;
    if (ik === ok) return `${ig.name}为${GUA_XIANG[ig.name] || ""}`;
    return `${GUA_XIANG[og.name] || ""}${GUA_XIANG[ig.name] || ""}${short}`;
  }

  return {
    // 基本信息
    guaName: fullGuaName(baseKey),
    guaKey: baseKey,
    innerGua: innerGua?.name,
    outerGua: outerGua?.name,
    innerWx: innerGua?.wx,
    outerWx: outerGua?.wx,

    // 八宫归属
    gong: gongInfo.gong,
    gongWx: gongWx,
    guaXu: gongInfo.guaXu,
    youHun: gongInfo.youHun || false,
    guiHun: gongInfo.guiHun || false,

    // 六爻详情
    yaos: yaos,

    // 变卦
    hasChanging: changingIdx.length > 0,
    changingIdx: changingIdx,
    changedGuaName: changingIdx.length > 0 ? fullGuaName(changedKey) : null,
    changedGuaKey: changingIdx.length > 0 ? changedKey : null,
    changedYaos: changedYaos,
    dongBianAnalysis: dongBianAnalysis,

    // 伏神
    fuShen: fuShen,
    missingLiuqin: missingLiuqin,

    // 特殊标记
    isLiuChongGua: isLiuChongGua,
    kongWang: kongWang,

    // 日月建
    riGan, riZhi, yueZhi,
  };
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 格式化输出（文本）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

function formatGuaText(result) {
  let t = "";

  // 标题
  t += `## ${result.gong}宫·${result.guaName}卦`;
  if (result.youHun) t += "（游魂）";
  if (result.guiHun) t += "（归魂）";
  t += `\n\n`;
  t += `${result.outerGua}（${result.outerWx}）上 · ${result.innerGua}（${result.innerWx}）下\n`;
  t += `卦宫五行：${result.gongWx}\n`;
  if (result.isLiuChongGua) t += `⚡ **六冲卦** — 主事多变动、冲散\n`;
  t += `日建：${result.riGan}${result.riZhi}　月建：${result.yueZhi}月\n`;
  t += `空亡：${result.kongWang.join("、")}\n\n`;

  // 变卦
  if (result.hasChanging) {
    t += `**本卦 ${result.guaName} → 变卦 ${result.changedGuaName}**\n`;
    t += `动爻：${result.changingIdx.map(i => result.yaos[i].posName + "爻").join("、")}\n\n`;
  }

  // 六爻排盘
  t += `## 六爻排盘\n\n`;
  t += `| 六神 | 六亲 | 本卦 ${result.guaName} |  | 变卦 ${result.changedGuaName || ""} |\n`;
  t += `|:----:|:----:|:----------:|:---:|:----------:|\n`;

  // 从上爻到初爻排列（传统排盘格式）
  for (let i = 5; i >= 0; i--) {
    const y = result.yaos[i];
    const shiYing = y.isShi ? "世" : (y.isYing ? "应" : "　");
    const dong = y.isDong ? "○→" : "　　";
    const kongMark = y.isKong ? "空" : "";
    const yaoSymbol = y.yinYang === "阳" ? "▅▅▅▅▅" : "▅▅　▅▅";

    let bianStr = "";
    if (y.isDong && result.changedYaos) {
      const cy = result.changedYaos[i];
      const cySymbol = cy.yinYang === "阳" ? "▅▅▅▅▅" : "▅▅　▅▅";
      bianStr = `${cy.liuqin} ${cy.tiangan}${cy.dizhi}${cy.wuxing} ${cySymbol}`;
    }

    t += `| ${y.liushen} | ${y.liuqin} | ${y.tiangan}${y.dizhi}${y.wuxing} ${yaoSymbol} ${shiYing}${kongMark} | ${dong} | ${bianStr} |\n`;
  }

  // 伏神
  if (result.fuShen.length > 0) {
    t += `\n### 伏神\n\n`;
    for (const fs of result.fuShen) {
      const underYao = result.yaos[fs.fuUnder];
      t += `**${fs.liuqin}**（${fs.tiangan}${fs.dizhi}${fs.wuxing}）伏于${underYao.posName}爻（${underYao.liuqin} ${underYao.tiangan}${underYao.dizhi}）之下\n`;
    }
  }

  t += "\n";

  // 动变分析
  if (result.dongBianAnalysis.length > 0) {
    t += `## 动变分析\n\n`;
    for (const d of result.dongBianAnalysis) {
      t += `**${d.yaoPos}爻动**：${d.benLq}${d.benGan}${d.benZhi}（${d.benWx}） → ${d.bianLq}${d.bianGan}${d.bianZhi}（${d.bianWx}）\n`;
      t += `关系：${d.relation}`;
      if (d.teXing) t += ` ${d.teXing}`;
      t += `\n\n`;
    }
  }

  // 旺衰分析
  t += `## 旺衰分析\n\n`;
  for (const y of result.yaos) {
    const marks = [];
    if (y.isDong) marks.push("动");
    if (y.isShi) marks.push("世");
    if (y.isYing) marks.push("应");
    if (y.isKong) marks.push("空亡");
    const markStr = marks.length > 0 ? `[${marks.join("·")}]` : "";
    t += `${y.posName}爻 ${y.liuqin}${y.tiangan}${y.dizhi}（${y.wuxing}）${markStr}：月建${y.yueEffect}，日建${y.riWangShuai}\n`;
  }

  return t;
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 导出
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// 在浏览器或 Worker 环境中使用
if (typeof module !== "undefined" && module.exports) {
  module.exports = { zhuangGua, formatGuaText, getLiushen, getKongWang, HEXAGRAM_TABLE: {} };
}
// 也挂在 window 上供 HTML 直接使用
if (typeof window !== "undefined") {
  window.NajiaEngine = { zhuangGua, formatGuaText };
}
