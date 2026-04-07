#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const data = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'data', 'jiaoshi.json'), 'utf-8'));
const esc = s => s.replace(/'/g, "''");

let sql = '-- 焦氏易林 D1 seed data (auto-generated from data/jiaoshi.json)\n\n';
let count = 0;

for (const [benKey, changes] of Object.entries(data.entries)) {
  for (const [bianKey, text] of Object.entries(changes)) {
    sql += `INSERT OR REPLACE INTO jiaoshi (ben_key, bian_key, text, created_at) VALUES ('${esc(benKey)}', '${esc(bianKey)}', '${esc(text)}', datetime('now'));\n`;
    count++;
  }
}

const outPath = path.join(__dirname, '..', 'worker', 'seed_jiaoshi.sql');
fs.writeFileSync(outPath, sql, 'utf-8');
console.log(`Written ${count} entries to ${outPath}`);
