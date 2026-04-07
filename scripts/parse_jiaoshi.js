#!/usr/bin/env node
/**
 * Parse 焦氏易林 full text (4096 entries) into D1 seed SQL.
 *
 * Format: "本卦之变卦\n占辞文字\n\n" repeated 4096 times
 *卦名 → 二进制卦码 mapping needed.
 */

const fs = require('fs');
const path = require('path');

// 卦名 → 二进制卦码 (初爻→上爻, 1=阳 0=阴)
const NAME_TO_KEY = {
  '乾': '111111', '坤': '000000', '屯': '010001', '蒙': '100010',
  '需': '010111', '讼': '111010', '师': '000010', '比': '010000',
  '小畜': '110111', '履': '111011', '泰': '000111', '否': '111000',
  '同人': '111101', '大有': '101111', '谦': '000100', '豫': '001000',
  '随': '011001', '蛊': '100110', '临': '000011', '观': '110000',
  '噬嗑': '101001', '贲': '100101', '剥': '100000', '复': '000001',
  '无妄': '111001', '大畜': '100111', '颐': '100001', '大过': '011110',
  '坎': '010010', '离': '101101', '咸': '011100', '恒': '001110',
  '遁': '111100', '大壮': '001111', '晋': '101000', '明夷': '000101',
  '家人': '110101', '睽': '101011', '蹇': '010100', '解': '001010',
  '损': '100011', '益': '110001', '夬': '011111', '姤': '111110',
  '萃': '011000', '升': '000110', '困': '011010', '井': '010110',
  '革': '011101', '鼎': '101110', '震': '001001', '艮': '100100',
  '渐': '110100', '归妹': '001011', '丰': '001101', '旅': '101100',
  '巽': '110110', '兑': '011011', '涣': '110010', '节': '010011',
  '中孚': '110011', '小过': '001100', '既济': '010101', '未济': '101010',
};

// All valid hexagram names for strict matching
const VALID_NAMES = new Set(Object.keys(NAME_TO_KEY));

function parseTitle(title) {
  // "乾之坤" → { ben: "乾", bian: "坤" }
  // Must match exactly: known_name之known_name
  const m = title.match(/^(.+)之(.+)$/);
  if (!m) return null;
  const ben = m[1].trim();
  const bian = m[2].trim();
  // Both sides must be valid hexagram names
  if (!VALID_NAMES.has(ben) || !VALID_NAMES.has(bian)) return null;
  return { ben, bian };
}

function main() {
  const raw = fs.readFileSync(path.join(__dirname, '..', 'data', 'jiaoshi_raw.txt'), 'utf-8');
  const lines = raw.split('\n');

  const entries = [];
  let i = 0;
  while (i < lines.length) {
    const line = lines[i].trim();
    if (!line) { i++; continue; }

    // Check if this is a title line (X之Y)
    const parsed = parseTitle(line);
    if (parsed) {
      // Collect poem lines until blank line
      const poemLines = [];
      i++;
      while (i < lines.length && lines[i].trim() !== '') {
        // Skip if this looks like another title (next entry without blank separator)
        if (parseTitle(lines[i].trim())) break;
        poemLines.push(lines[i].trim());
        i++;
      }

      const benKey = NAME_TO_KEY[parsed.ben];
      const bianKey = NAME_TO_KEY[parsed.bian];

      if (!benKey) {
        console.error(`Unknown 本卦: "${parsed.ben}" at line ~${i}`);
        continue;
      }
      if (!bianKey) {
        console.error(`Unknown 变卦: "${parsed.bian}" at line ~${i}`);
        continue;
      }

      entries.push({
        ben_key: benKey,
        bian_key: bianKey,
        ben_name: parsed.ben,
        bian_name: parsed.bian,
        text: poemLines.join('\n'),
      });
    } else {
      i++;
    }
  }

  console.log(`Parsed ${entries.length} entries`);

  // Verify we have ~4096
  // Check for duplicates
  const seen = new Set();
  let dupes = 0;
  for (const e of entries) {
    const k = `${e.ben_key}_${e.bian_key}`;
    if (seen.has(k)) {
      dupes++;
      console.error(`Duplicate: ${e.ben_name}之${e.bian_name} (${k})`);
    }
    seen.add(k);
  }
  if (dupes > 0) console.log(`${dupes} duplicates found`);

  // Write JSON
  const jsonPath = path.join(__dirname, '..', 'data', 'jiaoshi_full.json');
  fs.writeFileSync(jsonPath, JSON.stringify(entries, null, 2), 'utf-8');
  console.log(`JSON: ${jsonPath}`);

  // Write SQL
  const esc = s => s.replace(/'/g, "''");
  const sqlPath = path.join(__dirname, '..', 'worker', 'seed_jiaoshi.sql');
  let sql = '-- 焦氏易林 D1 seed data (auto-generated, 4096 entries)\n';
  sql += '-- Source: https://github.com/eanzhao/jiaoshiyilin\n\n';

  for (const e of entries) {
    sql += `INSERT OR REPLACE INTO jiaoshi (ben_key, bian_key, text, created_at) VALUES ('${esc(e.ben_key)}', '${esc(e.bian_key)}', '${esc(e.text)}', datetime('now'));\n`;
  }

  fs.writeFileSync(sqlPath, sql, 'utf-8');
  console.log(`SQL: ${sqlPath} (${entries.length} INSERTs)`);

  // Stats
  const totalChars = entries.reduce((s, e) => s + e.text.length, 0);
  console.log(`Total chars: ${totalChars}`);

  // Check coverage per ben_gua
  const benCounts = {};
  for (const e of entries) {
    benCounts[e.ben_name] = (benCounts[e.ben_name] || 0) + 1;
  }
  const incomplete = Object.entries(benCounts).filter(([_, c]) => c !== 64);
  if (incomplete.length > 0) {
    console.log(`Incomplete 本卦: ${incomplete.map(([n, c]) => `${n}(${c})`).join(', ')}`);
  } else {
    console.log('All 64 本卦 have exactly 64 entries each — complete coverage!');
  }
}

main();
