#!/usr/bin/env node
/**
 * Parse 高岛易断 HTML files into structured JSON and D1 seed SQL.
 *
 * Input: data/gao_md/01.md ... 64.md (HTML files)
 * Output:
 *   - data/gaodao_full.json (structured JSON for all 64 hexagrams)
 *   - worker/seed_gaodao.sql (D1 INSERT statements)
 */

const fs = require('fs');
const path = require('path');

// 64卦序号 → 二进制卦码 + 卦名 mapping
// Binary: 从初爻到上爻, 1=阳, 0=阴
const GUA_MAP = {
  1:  { key: '111111', name: '乾为天' },
  2:  { key: '000000', name: '坤为地' },
  3:  { key: '010001', name: '水雷屯' },
  4:  { key: '100010', name: '山水蒙' },
  5:  { key: '010111', name: '水天需' },
  6:  { key: '111010', name: '天水讼' },
  7:  { key: '000010', name: '地水师' },
  8:  { key: '010000', name: '水地比' },
  9:  { key: '110111', name: '风天小畜' },
  10: { key: '111011', name: '天泽履' },
  11: { key: '000111', name: '地天泰' },
  12: { key: '111000', name: '天地否' },
  13: { key: '111101', name: '天火同人' },
  14: { key: '101111', name: '火天大有' },
  15: { key: '000100', name: '地山谦' },
  16: { key: '001000', name: '雷地豫' },
  17: { key: '011001', name: '泽雷随' },
  18: { key: '100110', name: '山风蛊' },
  19: { key: '000011', name: '地泽临' },
  20: { key: '110000', name: '风地观' },
  21: { key: '101001', name: '火雷噬嗑' },
  22: { key: '100101', name: '山火贲' },
  23: { key: '100000', name: '山地剥' },
  24: { key: '000001', name: '地雷复' },
  25: { key: '111001', name: '天雷无妄' },
  26: { key: '100111', name: '山天大畜' },
  27: { key: '100001', name: '山雷颐' },
  28: { key: '011110', name: '泽风大过' },
  29: { key: '010010', name: '坎为水' },
  30: { key: '101101', name: '离为火' },
  31: { key: '011100', name: '泽山咸' },
  32: { key: '001110', name: '雷风恒' },
  33: { key: '111100', name: '天山遁' },
  34: { key: '001111', name: '雷天大壮' },
  35: { key: '101000', name: '火地晋' },
  36: { key: '000101', name: '地火明夷' },
  37: { key: '110101', name: '风火家人' },
  38: { key: '101011', name: '火泽睽' },
  39: { key: '010100', name: '水山蹇' },
  40: { key: '001010', name: '雷水解' },
  41: { key: '100011', name: '山泽损' },
  42: { key: '110001', name: '风雷益' },
  43: { key: '011111', name: '泽天夬' },
  44: { key: '111110', name: '天风姤' },
  45: { key: '011000', name: '泽地萃' },
  46: { key: '000110', name: '地风升' },
  47: { key: '011010', name: '泽水困' },
  48: { key: '010110', name: '水风井' },
  49: { key: '011101', name: '泽火革' },
  50: { key: '101110', name: '火风鼎' },
  51: { key: '001001', name: '震为雷' },
  52: { key: '100100', name: '艮为山' },
  53: { key: '110100', name: '风山渐' },
  54: { key: '001011', name: '雷泽归妹' },
  55: { key: '001101', name: '雷火丰' },
  56: { key: '101100', name: '火山旅' },
  57: { key: '110110', name: '巽为风' },
  58: { key: '011011', name: '兑为泽' },
  59: { key: '110010', name: '风水涣' },
  60: { key: '010011', name: '水泽节' },
  61: { key: '110011', name: '风泽中孚' },
  62: { key: '001100', name: '雷山小过' },
  63: { key: '010101', name: '水火既济' },
  64: { key: '101010', name: '火水未济' }
};

function stripHtml(html) {
  // Remove HTML tags, decode entities, clean up
  return html
    .replace(/<[^>]+>/g, '')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

function parseFile(filePath, num) {
  const html = fs.readFileSync(filePath, 'utf-8');
  const info = GUA_MAP[num];
  if (!info) throw new Error(`No mapping for hexagram ${num}`);

  // Split by h3 to get yao sections
  // h2 = main hexagram title + judgment
  // h3 = individual yao sections

  const h2Match = html.match(/<h2[^>]*>(.*?)<\/h2>/);
  const title = h2Match ? stripHtml(h2Match[1]) : info.name;

  // Split into sections by h3
  const h3Pattern = /<h3[^>]*>.*?<\/h3>/g;
  const h3Matches = [...html.matchAll(h3Pattern)];

  // Get judgment text (everything between h2 and first h3)
  const h2End = html.indexOf('</h2>') + 5;
  const firstH3 = h3Matches.length > 0 ? h3Matches[0].index : html.length;
  const judgmentHtml = html.substring(h2End, firstH3);
  const judgment = stripHtml(judgmentHtml);

  // Parse each yao section
  const yaos = [];
  for (let i = 0; i < h3Matches.length; i++) {
    const start = h3Matches[i].index;
    const end = (i + 1 < h3Matches.length) ? h3Matches[i + 1].index : html.indexOf('</body>');
    const sectionHtml = html.substring(start, end);
    const yaoTitle = stripHtml(h3Matches[i][0]);
    const yaoContent = stripHtml(sectionHtml);
    yaos.push(yaoContent);
  }

  // Also check for 用九/用六 section after last h3
  // It may be part of the last yao section or standalone

  return {
    gua_key: info.key,
    gua_name: info.name,
    judgment: judgment,
    yao_0: yaos[0] || '', // 初爻
    yao_1: yaos[1] || '', // 二爻
    yao_2: yaos[2] || '', // 三爻
    yao_3: yaos[3] || '', // 四爻
    yao_4: yaos[4] || '', // 五爻
    yao_5: yaos[5] || '', // 上爻
  };
}

function escapeSQL(str) {
  return str.replace(/'/g, "''");
}

function main() {
  const srcDir = path.join(__dirname, '..', 'data', 'gao_md');
  const results = [];

  for (let num = 1; num <= 64; num++) {
    const file = path.join(srcDir, `${String(num).padStart(2, '0')}.md`);
    if (!fs.existsSync(file)) {
      console.error(`Missing: ${file}`);
      continue;
    }
    try {
      const data = parseFile(file, num);
      results.push(data);
      console.log(`Parsed ${num}: ${data.gua_name} (${data.gua_key}) - judgment: ${data.judgment.length} chars, yaos: ${[data.yao_0, data.yao_1, data.yao_2, data.yao_3, data.yao_4, data.yao_5].map(y => y.length).join(',')}`);
    } catch (e) {
      console.error(`Error parsing ${num}: ${e.message}`);
    }
  }

  // Write JSON
  const jsonPath = path.join(__dirname, '..', 'data', 'gaodao_full.json');
  fs.writeFileSync(jsonPath, JSON.stringify(results, null, 2), 'utf-8');
  console.log(`\nJSON written to ${jsonPath} (${results.length} hexagrams)`);

  // Write SQL seed
  const sqlPath = path.join(__dirname, '..', 'worker', 'seed_gaodao.sql');
  let sql = '-- 高岛易断 D1 seed data (auto-generated)\n';
  sql += '-- Source: https://github.com/zhclassmates/Nothing/tree/main/gao_md\n\n';

  for (const r of results) {
    sql += `INSERT OR REPLACE INTO gaodao (gua_key, gua_name, judgment, yao_0, yao_1, yao_2, yao_3, yao_4, yao_5, created_at, updated_at) VALUES (\n`;
    sql += `  '${escapeSQL(r.gua_key)}',\n`;
    sql += `  '${escapeSQL(r.gua_name)}',\n`;
    sql += `  '${escapeSQL(r.judgment)}',\n`;
    sql += `  '${escapeSQL(r.yao_0)}',\n`;
    sql += `  '${escapeSQL(r.yao_1)}',\n`;
    sql += `  '${escapeSQL(r.yao_2)}',\n`;
    sql += `  '${escapeSQL(r.yao_3)}',\n`;
    sql += `  '${escapeSQL(r.yao_4)}',\n`;
    sql += `  '${escapeSQL(r.yao_5)}',\n`;
    sql += `  datetime('now'),\n`;
    sql += `  datetime('now')\n`;
    sql += `);\n\n`;
  }

  fs.writeFileSync(sqlPath, sql, 'utf-8');
  console.log(`SQL written to ${sqlPath}`);

  // Stats
  const totalChars = results.reduce((s, r) => s + r.judgment.length + r.yao_0.length + r.yao_1.length + r.yao_2.length + r.yao_3.length + r.yao_4.length + r.yao_5.length, 0);
  console.log(`Total: ${results.length} hexagrams, ${totalChars} chars`);
}

main();
