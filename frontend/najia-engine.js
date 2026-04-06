// ═══════════════════════════════════════════════
// 六爻纳甲装卦引擎 v1.0
// ═══════════════════════════════════════════════

const TIAN_GAN = "甲乙丙丁戊己庚辛壬癸";
const DI_ZHI = "子丑寅卯辰巳午未申酉戌亥";
const WU_XING_OF_ZHI = {子:"水",丑:"土",寅:"木",卯:"木",辰:"土",巳:"火",午:"火",未:"土",申:"金",酉:"金",戌:"土",亥:"水"};
const WU_XING_OF_GAN = {甲:"木",乙:"木",丙:"火",丁:"火",戊:"土",己:"土",庚:"金",辛:"金",壬:"水",癸:"水"};

// 六亲关系 (我→他): 同=兄弟, 生我=父母, 我生=子孙, 克我=官鬼, 我克=妻财
function getLiuQin(myWu, targetWu) {
  const table = {
    "金金":"兄弟","金水":"子孙","金木":"妻财","金火":"官鬼","金土":"父母",
    "木木":"兄弟","木火":"子孙","木土":"妻财","木金":"官鬼","木水":"父母",
    "水水":"兄弟","水木":"子孙","水火":"妻财","水土":"官鬼","水金":"父母",
    "火火":"兄弟","火土":"子孙","火金":"妻财","火水":"官鬼","火木":"父母",
    "土土":"兄弟","土金":"子孙","土水":"妻财","土木":"官鬼","土火":"父母"
  };
  return table[myWu + targetWu] || "?";
}

// ─── 纳甲规则 ───
// 八卦纳甲: [内卦天干, 内卦地支(初二三), 外卦天干, 外卦地支(四五六)]
const NAJIA_TABLE = {
  "111": {ganIn:"甲", zhiIn:["子","寅","辰"], ganOut:"壬", zhiOut:["午","申","戌"]},
  "110": {ganIn:"丁", zhiIn:["巳","卯","丑"], ganOut:"丁", zhiOut:["亥","酉","未"]},
  "101": {ganIn:"己", zhiIn:["卯","丑","亥"], ganOut:"己", zhiOut:["酉","未","巳"]},
  "100": {ganIn:"庚", zhiIn:["子","寅","辰"], ganOut:"庚", zhiOut:["午","申","戌"]},
  "011": {ganIn:"辛", zhiIn:["丑","亥","酉"], ganOut:"辛", zhiOut:["未","巳","卯"]},
  "010": {ganIn:"戊", zhiIn:["寅","辰","午"], ganOut:"戊", zhiOut:["申","戌","子"]},
  "001": {ganIn:"丙", zhiIn:["辰","午","申"], ganOut:"丙", zhiOut:["戌","子","寅"]},
  "000": {ganIn:"乙", zhiIn:["未","巳","卯"], ganOut:"癸", zhiOut:["丑","亥","酉"]},
};

// ─── 64卦宫位与世应 ───
// [宫名, 宫五行, 世爻位(1-6), 类型]
const PALACE_DATA = {
  // 乾宫(金)
  "111111":["乾","金",6,"本宫"],"011111":["乾","金",1,"一世"],"001111":["乾","金",2,"二世"],
  "000111":["乾","金",3,"三世"],"000011":["乾","金",4,"四世"],"000001":["乾","金",5,"五世"],
  "000101":["乾","金",4,"游魂"],"111101":["乾","金",3,"归魂"],
  // 兑宫(金)
  "110110":["兑","金",6,"本宫"],"010110":["兑","金",1,"一世"],"000110":["兑","金",2,"二世"],
  "001110":["兑","金",3,"三世"],"001010":["兑","金",4,"四世"],"001000":["兑","金",5,"五世"],
  "001100":["兑","金",4,"游魂"],"110100":["兑","金",3,"归魂"],
  // 离宫(火)
  "101101":["离","火",6,"本宫"],"001101":["离","火",1,"一世"],"011101":["离","火",2,"二世"],
  "010101":["离","火",3,"三世"],"010001":["离","火",4,"四世"],"010011":["离","火",5,"五世"],
  "010111":["离","火",4,"游魂"],"101111":["离","火",3,"归魂"],
  // 震宫(木)
  "100100":["震","木",6,"本宫"],"000100":["震","木",1,"一世"],"010100":["震","木",2,"二世"],
  "011100":["震","木",3,"三世"],"011000":["震","木",4,"四世"],"011010":["震","木",5,"五世"],
  "011110":["震","木",4,"游魂"],"100110":["震","木",3,"归魂"],
  // 巽宫(木)
  "011011":["巽","木",6,"本宫"],"111011":["巽","木",1,"一世"],"101011":["巽","木",2,"二世"],
  "100011":["巽","木",3,"三世"],"100111":["巽","木",4,"四世"],"100101":["巽","木",5,"五世"],
  "100001":["巽","木",4,"游魂"],"011001":["巽","木",3,"归魂"],
  // 坎宫(水)
  "010010":["坎","水",6,"本宫"],"110010":["坎","水",1,"一世"],"100010":["坎","水",2,"二世"],
  "101010":["坎","水",3,"三世"],"101110":["坎","水",4,"四世"],"101100":["坎","水",5,"五世"],
  "101000":["坎","水",4,"游魂"],"010000":["坎","水",3,"归魂"],
  // 艮宫(土)
  "001001":["艮","土",6,"本宫"],"101001":["艮","土",1,"一世"],"111001":["艮","土",2,"二世"],
  "110001":["艮","土",3,"三世"],"110101":["艮","土",4,"四世"],"110111":["艮","土",5,"五世"],
  "110011":["艮","土",4,"游魂"],"001011":["艮","土",3,"归魂"],
  // 坤宫(土)
  "000000":["坤","土",6,"本宫"],"100000":["坤","土",1,"一世"],"110000":["坤","土",2,"二世"],
  "111000":["坤","土",3,"三世"],"111100":["坤","土",4,"四世"],"111110":["坤","土",5,"五世"],
  "111010":["坤","土",4,"游魂"],"000010":["坤","土",3,"归魂"],
};

// ─── 六神排列 ───
// 日干 → 初爻起何神，然后青龙→朱雀→勾陈→螣蛇→白虎→玄武 顺排
const LIUSHEN_ORDER = ["青龙","朱雀","勾陈","螣蛇","白虎","玄武"];
const LIUSHEN_START = {甲:0,乙:0,丙:1,丁:1,戊:2,己:3,庚:4,辛:4,壬:5,癸:5};

function getLiuShen(dayGan) {
  const start = LIUSHEN_START[dayGan] || 0;
  return [0,1,2,3,4,5].map(i => LIUSHEN_ORDER[(start + i) % 6]);
}

// ─── 日期计算 ───
// 基准: 2000-01-01 = 甲子序号54 (戊午日, stem=4 branch=6)
// Actually: 2000-01-07 is 甲子日. Let me use a known reference.
// 1900-01-01 = 甲戌日, index=10. But let's use a more practical reference.
// Reference: 2024-01-01 = 甲子index for that day.
// I'll compute: 2000-01-01 is 戊午 = index 54.

function daysSince2000(y, m, d) {
  const ref = new Date(2000, 0, 1);
  const target = new Date(y, m - 1, d);
  return Math.floor((target - ref) / 86400000);
}

function getDayGanZhi(y, m, d) {
  const days = daysSince2000(y, m, d);
  const idx = ((54 + days) % 60 + 60) % 60;
  return {
    gan: TIAN_GAN[idx % 10],
    zhi: DI_ZHI[idx % 12],
    idx: idx
  };
}

// 月建 (节气近似): 用公历月份近似推断
// 立春约2/4 → 寅月, 惊蛰约3/6 → 卯月, ... 
function getMonthZhi(y, m, d) {
  // 节气大约在每月4-8日交节
  const jieqi = [
    [2,4],[3,6],[4,5],[5,6],[6,6],[7,7],
    [8,8],[9,8],[10,8],[11,7],[12,7],[1,6]
  ];
  // 寅=2月, 卯=3月, ... 丑=1月
  const monthBranches = ["丑","寅","卯","辰","巳","午","未","申","酉","戌","亥","子"];
  
  let solarMonth = m;
  // 简化: 如果日期在当月节气之前，算上个月
  const jq = jieqi.find(j => j[0] === m);
  if (jq && d < jq[1]) {
    solarMonth = m - 1;
    if (solarMonth < 1) solarMonth = 12;
  }
  // 寅月=2, 卯月=3, ..., 丑月=1
  const idx = (solarMonth - 1 + 12) % 12; // 0=Jan, 1=Feb, ...
  return monthBranches[idx];
}

// 空亡
function getKongWang(dayIdx) {
  const xunStart = dayIdx - (dayIdx % 10);
  const startBranch = xunStart % 12;
  const k1 = (startBranch + 10) % 12;
  const k2 = (startBranch + 11) % 12;
  return [DI_ZHI[k1], DI_ZHI[k2]];
}

// ─── 装卦主函数 ───
// lines: array of 6 values (6=老阴,7=少阳,8=少阴,9=老阳)
// date: {year, month, day} or null (use today)
function zhuangGua(lines, date) {
  const now = date || (function(){ const d=new Date(); return {year:d.getFullYear(),month:d.getMonth()+1,day:d.getDate()}; })();
  
  // 日干支
  const dayGZ = getDayGanZhi(now.year, now.month, now.day);
  const monthZhi = getMonthZhi(now.year, now.month, now.day);
  const kongWang = getKongWang(dayGZ.idx);
  const liuShen = getLiuShen(dayGZ.gan);
  
  // 本卦与变卦
  const baseBits = lines.map(v => (v===7||v===9) ? 1 : 0);
  const changedBits = lines.map(v => {
    if (v===6) return 1;
    if (v===9) return 0;
    return (v===7||v===9) ? 1 : 0;
  });
  const hasChanging = lines.some(v => v===6 || v===9);
  
  const baseKey = baseBits.join("");
  const changeKey = changedBits.join("");
  
  // 宫位
  const palace = PALACE_DATA[baseKey];
  if (!palace) return null;
  const [palaceName, palaceWu, shiPos, palaceType] = palace;
  const yingPos = shiPos <= 3 ? shiPos + 3 : shiPos - 3;
  
  // 纳甲装卦
  const lowerTri = baseKey.slice(0, 3);
  const upperTri = baseKey.slice(3, 6);
  const lowerNJ = NAJIA_TABLE[lowerTri];
  const upperNJ = NAJIA_TABLE[upperTri];
  
  // 变卦纳甲
  let changeLowerNJ = null, changeUpperNJ = null;
  if (hasChanging) {
    const cLower = changeKey.slice(0, 3);
    const cUpper = changeKey.slice(3, 6);
    changeLowerNJ = NAJIA_TABLE[cLower];
    changeUpperNJ = NAJIA_TABLE[cUpper];
  }
  
  // 构建六爻数据
  const yaoData = [];
  for (let i = 0; i < 6; i++) {
    const isLower = i < 3;
    const nj = isLower ? lowerNJ : upperNJ;
    const localIdx = isLower ? i : i - 3;
    const gan = isLower ? nj.ganIn : nj.ganOut;
    const zhi = isLower ? nj.zhiIn[localIdx] : nj.zhiOut[localIdx];
    const wu = WU_XING_OF_ZHI[zhi];
    const liuqin = getLiuQin(palaceWu, wu);
    const isDong = lines[i] === 6 || lines[i] === 9;
    const isYin = baseBits[i] === 0;
    
    const yao = {
      pos: i + 1,
      gan: gan,
      zhi: zhi,
      ganZhi: gan + zhi,
      wu: wu,
      liuqin: liuqin,
      liushen: liuShen[i],
      dong: isDong,
      isYin: isYin,
      isShi: (i + 1) === shiPos,
      isYing: (i + 1) === yingPos,
      isKong: kongWang.includes(zhi),
      yaoType: lines[i], // 6,7,8,9
    };
    
    // 变爻信息
    if (isDong) {
      const cNJ = isLower ? changeLowerNJ : changeUpperNJ;
      const cZhi = isLower ? cNJ.zhiIn[localIdx] : cNJ.zhiOut[localIdx];
      const cGan = isLower ? cNJ.ganIn : cNJ.ganOut;
      yao.bianGan = cGan;
      yao.bianZhi = cZhi;
      yao.bianGanZhi = cGan + cZhi;
      yao.bianWu = WU_XING_OF_ZHI[cZhi];
      yao.bianQin = getLiuQin(palaceWu, yao.bianWu);
    }
    
    yaoData.push(yao);
  }
  
  // 卦名
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
  
  return {
    date: `${now.year}-${String(now.month).padStart(2,'0')}-${String(now.day).padStart(2,'0')}`,
    dayGan: dayGZ.gan,
    dayZhi: dayGZ.zhi,
    dayGanZhi: dayGZ.gan + dayGZ.zhi,
    monthZhi: monthZhi,
    kongWang: kongWang,
    hexName: HEXAGRAM_TABLE[baseKey] || "?",
    changeName: hasChanging ? (HEXAGRAM_TABLE[changeKey] || "?") : null,
    palace: palaceName,
    palaceWu: palaceWu,
    palaceType: palaceType,
    shi: shiPos,
    ying: yingPos,
    hasChanging: hasChanging,
    lines: yaoData,
    baseKey: baseKey,
    changeKey: hasChanging ? changeKey : null,
  };
}

// ─── 格式化输出（文本版） ───
function formatNajiaChart(gua) {
  const YAO_POS = ["初","二","三","四","五","上"];
  let t = "";
  
  t += `${gua.palace}宫（${gua.palaceWu}）  ${gua.hexName}卦`;
  if (gua.changeName) t += ` → ${gua.changeName}卦`;
  t += `  （${gua.palaceType}）\n`;
  t += `日建：${gua.dayGanZhi}  月建：${gua.monthZhi}月  空亡：${gua.kongWang.join("")}\n\n`;
  
  // 从上爻到初爻显示
  for (let i = 5; i >= 0; i--) {
    const y = gua.lines[i];
    const yinYang = y.isYin ? "▅▅ ▅▅" : "▅▅▅▅▅";
    const shiYing = y.isShi ? "世" : (y.isYing ? "应" : "  ");
    const dong = y.dong ? "○" : " ";
    const kong = y.isKong ? "(空)" : "";
    
    let bianStr = "";
    if (y.dong) {
      bianStr = ` → ${y.bianGanZhi}${y.bianWu} ${y.bianQin}`;
    }
    
    t += `${y.liushen}  ${y.liuqin}  ${y.ganZhi}${y.wu}  ${yinYang} ${dong} ${shiYing} ${kong}${bianStr}\n`;
  }
  
  return t;
}

// Export for use
if (typeof window !== 'undefined') {
  window.NajiaEngine = { zhuangGua, formatNajiaChart, getLiuQin, getDayGanZhi, getMonthZhi, getKongWang, getLiuShen, PALACE_DATA, WU_XING_OF_ZHI };
}
if (typeof module !== 'undefined') {
  module.exports = { zhuangGua, formatNajiaChart };
}
