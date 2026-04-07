-- 易学经典文献缓存表
-- 执行: wrangler d1 execute hexgram-classics --file=schema.sql

-- 高岛易断缓存: 按卦码(6位二进制)索引
CREATE TABLE IF NOT EXISTS gaodao (
  gua_key    TEXT PRIMARY KEY,   -- 例: "111111"
  gua_name   TEXT NOT NULL,      -- 例: "乾"
  judgment   TEXT NOT NULL,      -- 卦断
  yao_0      TEXT NOT NULL DEFAULT '',  -- 初爻断
  yao_1      TEXT NOT NULL DEFAULT '',  -- 二爻断
  yao_2      TEXT NOT NULL DEFAULT '',  -- 三爻断
  yao_3      TEXT NOT NULL DEFAULT '',  -- 四爻断
  yao_4      TEXT NOT NULL DEFAULT '',  -- 五爻断
  yao_5      TEXT NOT NULL DEFAULT '',  -- 上爻断
  created_at TEXT DEFAULT (datetime('now')),
  updated_at TEXT DEFAULT (datetime('now'))
);

-- 黄金策缓存: 按事类索引
CREATE TABLE IF NOT EXISTS huangjince (
  category   TEXT PRIMARY KEY,   -- 例: "求财", "婚姻"
  label      TEXT NOT NULL,      -- 显示名
  text       TEXT NOT NULL,      -- 断语全文
  created_at TEXT DEFAULT (datetime('now')),
  updated_at TEXT DEFAULT (datetime('now'))
);

-- 焦氏易林缓存: 按本卦+变卦索引
CREATE TABLE IF NOT EXISTS jiaoshi (
  ben_key    TEXT NOT NULL,      -- 本卦码 例: "111111"
  bian_key   TEXT NOT NULL,      -- 变卦码 例: "000000"
  text       TEXT NOT NULL,      -- 占辞
  created_at TEXT DEFAULT (datetime('now')),
  PRIMARY KEY (ben_key, bian_key)
);

-- 查询索引
CREATE INDEX IF NOT EXISTS idx_jiaoshi_ben ON jiaoshi(ben_key);
