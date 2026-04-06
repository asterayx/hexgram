// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 六爻纳甲装卦完整计算模块
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─── 纳甲天干地支 ───
// 每个经卦(三爻)在内卦/外卦位置的天干和地支
// [内卦天干, 外卦天干, 内卦三爻地支(初二三), 外卦三爻地支(四五六)]
const NAJIA = {
  "111": ["甲","壬", ["子","寅","辰"], ["午","申","戌"]],  // 乾
  "000": ["乙","癸", ["未","巳","卯"], ["丑","亥","酉"]],  // 坤
  "100": ["庚","庚", ["子","寅","辰"], ["午","申","戌"]],  // 震
  "011": ["辛","辛", ["丑","亥","酉"], ["未","巳","卯"]],  // 巽
  "010": ["戊","戊", ["寅","辰","午"], ["申","戌","子"]],  // 坎
  "101": ["己","己", ["卯","丑","亥"], ["酉","未","巳"]],  // 离
  "001": ["丙","丙", ["辰","午","申"], ["戌","子","寅"]],  // 艮
  "110": ["丁","丁", ["巳","卯","丑"], ["亥","酉","未"]],  // 兑
};

// ─── 八宫归属 ───
// 每个六爻卦的 key → [宫名, 宫五行, 宫位(1-8)]
// 宫位: 1=本宫 2=一世 3=二世 4=三世 5=四世 6=五世 7=游魂 8=归魂
const GONG_DATA = {
  // 乾宫(金)
  "111111":["乾","金",1],"011111":["乾","金",2],"001111":["乾","金",3],"000111":["乾","金",4],
  "000011":["乾","金",5],"000001":["乾","金",6],"000101":["乾","金",7],"111101":["乾","金",8],
  // 坤宫(土)
  "000000":["坤","土",1],"100000":["坤","土",2],"110000":["坤","土",3],"111000":["坤","土",4],
  "111100":["坤","土",5],"111110":["坤","土",6],"111010":["坤","土",7],"000010":["坤","土",8],
  // 震宫(木)
  "100100":["震","木",1],"000100":["震","木",2],"010100":["震","木",3],"011100":["震","木",4],
  "011000":["震","木",5],"011010":["震","木",6],"011110":["震","木",7],"100110":["震","木",8],
  // 巽宫(木)
  "011011":["巽","木",1],"111011":["巽","木",2],"101011":["巽","木",3],"100011":["巽","木",4],
  "100111":["巽","木",5],"100101":["巽","木",6],"100001":["巽","木",7],"011001":["巽","木",8],
  // 坎宫(水)
  "010010":["坎","水",1],"110010":["坎","水",2],"100010":["坎","水",3],"101010":["坎","水",4],
  "101110":["坎","水",5],"101100":["坎","水",6],"101000":["坎","水",7],"010000":["坎","水",8],
  // 离宫(火)
  "101101":["离","火",1],"001101":["离","火",2],"011101":["离","火",3],"010101":["离","火",4],
  "010001":["离","火",5],"010011":["离","火",6],"010111":["离","火",7],"101111":["离","火",8],
  // 艮宫(土)
  "001001":["艮","土",1],"101001":["艮","土",2],"111001":["艮","土",3],"110001":["艮","土",4],
  "110101":["艮","土",5],"110111":["艮","土",6],"110011":["艮","土",7],"001011":["艮","土",8],
  // 兑宫(金)
  "110110":["兑","金",1],"010110":["兑","金",2],"000110":["兑","金",3],"001110":["兑","金",4],
  "001010":["兑","金",5],"001000":["兑","金",6],"001100":["兑","金",7],"110100":["兑","金",8],
};

// 世应表: 宫位 → [世爻位, 应爻位] (1-indexed)
const SHI_YING = {1:[6,3],2:[1,4],3:[2,5],4:[3,6],5:[4,1],6:[5,2],7:[4,1],8:[3,6]};
const GONG_POS_NAME = {1:"本宫",2:"一世",3:"二世",4:"三世",5:"四世",6:"五世",7:"游魂",8:"归魂"};

// ─── 五行系统 ───
const ZHI_WU = {"子":"水","丑":"土","寅":"木","卯":"木","辰":"土","巳":"火","午":"火","未":"土","申":"金","酉":"金","戌":"土","亥":"水"};
const SHENG = {"金":"水","水":"木","木":"火","火":"土","土":"金"};
const KE = {"金":"木","木":"土","土":"水","水":"火","火":"金"};

// 六亲: 根据宫五行和爻地支五行的关系
function getLiuqin(gongWu, zhiWu) {
  if (gongWu === zhiWu) return "兄弟";
  if (SHENG[zhiWu] === gongWu) return "父母";  // 生我者
  if (SHENG[gongWu] === zhiWu) return "子孙";  // 我生者
  if (KE[zhiWu] === gongWu) return "官鬼";    // 克我者
  if (KE[gongWu] === zhiWu) return "妻财";    // 我克者
  return "？";
}

// ─── 六神 ───
const LIUSHEN_ORDER = ["青龙","朱雀","勾陈","螣蛇","白虎","玄武"];
const TIAN_GAN = ["甲","乙","丙","丁","戊","己","庚","辛","壬","癸"];
// 日干→六神起始: 甲乙=青龙(0), 丙丁=朱雀(1), 戊=勾陈(2), 己=螣蛇(3), 庚辛=白虎(4), 壬癸=玄武(5)
const GAN_LIUSHEN_START = {"甲":0,"乙":0,"丙":1,"丁":1,"戊":2,"己":3,"庚":4,"辛":4,"壬":5,"癸":5};

// ─── 日期计算 ───
function getJDN(y, m, d) {
  // Julian Day Number
  const a = Math.floor((14 - m) / 12);
  const y2 = y + 4800 - a;
  const m2 = m + 12 * a - 3;
  return d + Math.floor((153 * m2 + 2) / 5) + 365 * y2 + Math.floor(y2 / 4) - Math.floor(y2 / 100) + Math.floor(y2 / 400) - 32045;
}

function getDayStem(date) {
  // 2000-01-01 JDN=2451545, 庚辰日, 天干index=6(庚)
  const jdn = getJDN(date.getFullYear(), date.getMonth() + 1, date.getDate());
  const idx = ((jdn - 2451545 + 6) % 10 + 10) % 10;
  return TIAN_GAN[idx];
}

function getDayBranch(date) {
  const DI_ZHI = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"];
  const jdn = getJDN(date.getFullYear(), date.getMonth() + 1, date.getDate());
  // 2000-01-01 = 庚辰, 辰=4
  const idx = ((jdn - 2451545 + 4) % 12 + 12) % 12;
  return DI_ZHI[idx];
}

// 旬空计算
function getXunKong(date) {
  const DI_ZHI = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"];
  const jdn = getJDN(date.getFullYear(), date.getMonth() + 1, date.getDate());
  const ganIdx = ((jdn - 2451545 + 6) % 10 + 10) % 10;
  const zhiIdx = ((jdn - 2451545 + 4) % 12 + 12) % 12;
  // 旬首地支 index = zhiIdx - ganIdx; 空亡 = 旬尾两个地支
  const xunStart = ((zhiIdx - ganIdx) % 12 + 12) % 12;
  const kong1 = DI_ZHI[(xunStart + 10) % 12];
  const kong2 = DI_ZHI[(xunStart + 11) % 12];
  return [kong1, kong2];
}

// 月建(简化: 根据当前月份推算地支)
function getMonthZhi(date) {
  const DI_ZHI = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"];
  // 简化: 农历正月=寅, 二月=卯... 这里用公历近似(差1-2天)
  // 立春约2/4, 惊蛰约3/6...
  const m = date.getMonth() + 1;
  const d = date.getDate();
  let monthIdx;
  if ((m===2 && d>=4) || (m===3 && d<6)) monthIdx = 2;  // 寅
  else if ((m===3 && d>=6) || (m===4 && d<5)) monthIdx = 3;  // 卯
  else if ((m===4 && d>=5) || (m===5 && d<6)) monthIdx = 4;  // 辰
  else if ((m===5 && d>=6) || (m===6 && d<6)) monthIdx = 5;  // 巳
  else if ((m===6 && d>=6) || (m===7 && d<7)) monthIdx = 6;  // 午
  else if ((m===7 && d>=7) || (m===8 && d<7)) monthIdx = 7;  // 未
  else if ((m===8 && d>=7) || (m===9 && d<8)) monthIdx = 8;  // 申
  else if ((m===9 && d>=8) || (m===10 && d<8)) monthIdx = 9;  // 酉
  else if ((m===10 && d>=8) || (m===11 && d<7)) monthIdx = 10; // 戌
  else if ((m===11 && d>=7) || (m===12 && d<7)) monthIdx = 11; // 亥
  else if ((m===12 && d>=7) || (m===1 && d<6)) monthIdx = 0;  // 子
  else monthIdx = 1; // 丑
  return DI_ZHI[monthIdx];
}

// ━━ 核心: 装卦 ━━
// lines: array of 6 values (6=老阴,7=少阳,8=少阴,9=老阳)
// returns complete 卦盘 object
function buildGuaPan(lines) {
  const now = new Date();
  const dayStem = getDayStem(now);
  const dayBranch = getDayBranch(now);
  const monthZhi = getMonthZhi(now);
  const xunKong = getXunKong(now);

  // Base and changed bits
  const baseBits = lines.map(v => (v === 7 || v === 9) ? 1 : 0);
  const changedBits = lines.map(v => { if (v === 6) return 1; if (v === 9) return 0; return (v === 7 || v === 9) ? 1 : 0; });
  const hasChanging = lines.some(v => v === 6 || v === 9);
  const changingPos = lines.map((v, i) => (v === 6 || v === 9) ? i : -1).filter(i => i >= 0);

  const baseKey = baseBits.join("");
  const changedKey = changedBits.join("");

  // Trigrams
  const lowerTri = baseKey.slice(0, 3);
  const upperTri = baseKey.slice(3, 6);

  // Hexagram names
  const HEXAGRAM_TABLE = window.HEXAGRAM_TABLE;
  const mainName = HEXAGRAM_TABLE[baseKey] || "未知";
  const changedName = hasChanging ? (HEXAGRAM_TABLE[changedKey] || "未知") : "";

  // 八宫
  const gongInfo = GONG_DATA[baseKey] || ["未知", "土", 1];
  const [gongName, gongWu, gongPos] = gongInfo;
  const [shi, ying] = SHI_YING[gongPos] || [3, 6];

  // 变卦八宫
  let changedGongName = "", changedGongWu = "";
  if (hasChanging) {
    const cg = GONG_DATA[changedKey] || ["未知", "土", 1];
    changedGongName = cg[0];
    changedGongWu = cg[1];
  }

  // 纳甲: 为每爻分配天干地支
  const lowerNJ = NAJIA[lowerTri];
  const upperNJ = NAJIA[upperTri];

  // 变卦纳甲
  const chLowerTri = changedKey.slice(0, 3);
  const chUpperTri = changedKey.slice(3, 6);
  const chLowerNJ = NAJIA[chLowerTri];
  const chUpperNJ = NAJIA[chUpperTri];

  // 六神
  const lsStart = GAN_LIUSHEN_START[dayStem] || 0;

  const YAO_POS = ["初爻", "二爻", "三爻", "四爻", "五爻", "上爻"];
  const YAO_LABELS = { 6: "老阴", 7: "少阳", 8: "少阴", 9: "老阳" };

  // Build line data
  const lineData = lines.map((val, i) => {
    const isInner = i < 3;
    const nj = isInner ? lowerNJ : upperNJ;
    const gan = isInner ? nj[0] : nj[1];
    const zhi = isInner ? nj[2][i] : nj[3][i - 3];
    const zhiWu = ZHI_WU[zhi];
    const liuqin = getLiuqin(gongWu, zhiWu);
    const liushen = LIUSHEN_ORDER[(lsStart + i) % 6];
    const isChanging = val === 6 || val === 9;
    const isYang = baseBits[i] === 1;

    // 变爻干支
    let changedGan = "", changedZhi = "", changedGanZhi = "", changedWu = "", changedLiuqin = "";
    if (isChanging) {
      const cInner = i < 3;
      const cnj = cInner ? chLowerNJ : chUpperNJ;
      changedGan = cInner ? cnj[0] : cnj[1];
      changedZhi = cInner ? cnj[2][i] : cnj[3][i - 3];
      changedGanZhi = changedGan + changedZhi;
      changedWu = ZHI_WU[changedZhi];
      changedLiuqin = getLiuqin(gongWu, changedWu);
    }

    // 空亡
    const isKong = xunKong.includes(zhi);

    // 世应标记
    let shiYing = "";
    if (i + 1 === shi) shiYing = "世";
    if (i + 1 === ying) shiYing = "应";

    return {
      pos: YAO_POS[i],
      val,
      yaoType: YAO_LABELS[val],
      isYang,
      changing: isChanging,
      gan,
      zhi,
      ganZhi: gan + zhi,
      wuxing: zhiWu,
      liuqin,
      liushen,
      shiYing,
      isKong,
      changedGan,
      changedZhi,
      changedGanZhi,
      changedWu,
      changedLiuqin,
    };
  });

  // Trigram names
  const TRIGRAM_NAMES = window.TRIGRAM_NAMES;

  // Changing summary
  let changingSummary = "";
  if (hasChanging) {
    changingSummary = changingPos.map(i => {
      const l = lineData[i];
      return `${l.pos}(${l.liuqin}${l.ganZhi}${l.wuxing}) 动→ ${l.changedLiuqin}${l.changedGanZhi}${l.changedWu}`;
    }).join("\n");
  }

  return {
    mainName,
    baseKey,
    upperTriName: TRIGRAM_NAMES[upperTri] || upperTri,
    lowerTriName: TRIGRAM_NAMES[lowerTri] || lowerTri,
    gongName,
    gongWu,
    gongPos,
    gongPosName: GONG_POS_NAME[gongPos],
    shi,
    ying,
    hasChanging,
    changedName,
    changedKey,
    changedGongName,
    changedGongWu,
    changingPositions: changingPos.map(i => YAO_POS[i]).join("、"),
    changingSummary,
    lines: lineData,
    dayStem,
    dayBranch,
    dayGanZhi: dayStem + dayBranch,
    monthZhi,
    xunKong,
    liushenStart: LIUSHEN_ORDER[lsStart],
    liushenOrder: LIUSHEN_ORDER.slice(lsStart).concat(LIUSHEN_ORDER.slice(0, lsStart)).join("→"),
    dateInfo: `${now.getFullYear()}年${now.getMonth()+1}月${now.getDate()}日 ${dayStem}${dayBranch}日 月建${monthZhi} 空亡${xunKong.join("")}`,
  };
}

// Export for use in HTML
window.buildGuaPan = buildGuaPan;
window.GONG_DATA = GONG_DATA;
