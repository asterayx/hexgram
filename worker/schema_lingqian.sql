-- 灵签表 (北帝玄天大帝灵签 51签)
CREATE TABLE IF NOT EXISTS lingqian (
  qian_num INTEGER PRIMARY KEY,    -- 签号 1-51
  qian_name TEXT NOT NULL,          -- 签名 (e.g. "宋太祖登基")
  qian_type TEXT NOT NULL,          -- 签文类型 (e.g. "吉胜无疑之兆")
  gua_xiang TEXT,                   -- 卦象 (e.g. "上上之卦")
  sheng_xiao TEXT,                  -- 生肖
  xi_wen TEXT,                      -- 戏文
  shi_yue TEXT,                     -- 诗曰
  nei_zhao TEXT,                    -- 内兆
  full_json TEXT NOT NULL,          -- 完整JSON (含所有分类详解)
  created_at TEXT DEFAULT (datetime('now'))
);
