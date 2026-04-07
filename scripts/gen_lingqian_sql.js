#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, '..', 'data', 'lingqian');
const esc = s => (s || '').replace(/'/g, "''");

let sql = '-- 北帝玄天大帝灵签 D1 seed data (51签)\n';
sql += '-- Source: https://github.com/LeoonLiang/xuanwu-fozu-lingqian\n\n';

for (let i = 1; i <= 51; i++) {
  const file = path.join(srcDir, `${String(i).padStart(2, '0')}.json`);
  const data = JSON.parse(fs.readFileSync(file, 'utf-8'));
  const intro = data['签文简介'] || {};

  sql += `INSERT OR REPLACE INTO lingqian (qian_num, qian_name, qian_type, gua_xiang, sheng_xiao, xi_wen, shi_yue, nei_zhao, full_json) VALUES (\n`;
  sql += `  ${i},\n`;
  sql += `  '${esc(data['签名'])}',\n`;
  sql += `  '${esc(data['签文类型'])}',\n`;
  sql += `  '${esc(data['卦象'])}',\n`;
  sql += `  '${esc(data['生肖'])}',\n`;
  sql += `  '${esc(intro['戏文'])}',\n`;
  sql += `  '${esc(intro['诗曰'])}',\n`;
  sql += `  '${esc(intro['内兆'])}',\n`;
  sql += `  '${esc(JSON.stringify(data))}'\n`;
  sql += `);\n\n`;
}

const outPath = path.join(__dirname, '..', 'worker', 'seed_lingqian.sql');
fs.writeFileSync(outPath, sql, 'utf-8');
console.log(`Written 51 entries to ${outPath}`);
