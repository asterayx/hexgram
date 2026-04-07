#!/usr/bin/env node
/**
 * Generate D1 seed SQL for 黄金策 from data/huangjince.json
 */
const fs = require('fs');
const path = require('path');

const data = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'data', 'huangjince.json'), 'utf-8'));
const esc = s => s.replace(/'/g, "''");

let sql = '-- 黄金策 D1 seed data (auto-generated from data/huangjince.json)\n\n';

for (const cat of data.categories) {
  sql += `INSERT OR REPLACE INTO huangjince (category, label, text, created_at, updated_at) VALUES (\n`;
  sql += `  '${esc(cat.key)}',\n`;
  sql += `  '${esc(cat.label)}',\n`;
  sql += `  '${esc(cat.text)}',\n`;
  sql += `  datetime('now'),\n`;
  sql += `  datetime('now')\n`;
  sql += `);\n\n`;
}

const outPath = path.join(__dirname, '..', 'worker', 'seed_huangjince.sql');
fs.writeFileSync(outPath, sql, 'utf-8');
console.log(`Written ${data.categories.length} categories to ${outPath}`);
