-- ============================================
-- 数字分身系统数据库表结构
-- ============================================

-- 表 1: 数字分身（digital_souls）
CREATE TABLE IF NOT EXISTS digital_souls (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  version INTEGER NOT NULL DEFAULT 1,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,

  -- JSON 存储复杂数据结构
  foundation TEXT NOT NULL,      -- SoulFoundation (JSON)
  memories TEXT NOT NULL,         -- SoulMemories (JSON)
  metadata TEXT NOT NULL          -- SoulMetadata (JSON)
);

CREATE INDEX IF NOT EXISTS idx_souls_user_id ON digital_souls(user_id);
CREATE INDEX IF NOT EXISTS idx_souls_updated ON digital_souls(updated_at);

-- ============================================

-- 表 2: 记忆片段（memory_fragments）
CREATE TABLE IF NOT EXISTS memory_fragments (
  id TEXT PRIMARY KEY,
  soul_id TEXT NOT NULL,
  timestamp INTEGER NOT NULL,
  type TEXT NOT NULL,

  context TEXT NOT NULL,          -- 上下文 (JSON)
  user_action TEXT NOT NULL,      -- 用户行为 (JSON)
  extraction TEXT NOT NULL,       -- AI 提取结果 (JSON)
  feedback TEXT,                  -- 用户反馈 (JSON)

  FOREIGN KEY (soul_id) REFERENCES digital_souls(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fragments_soul_id ON memory_fragments(soul_id);
CREATE INDEX IF NOT EXISTS idx_fragments_timestamp ON memory_fragments(timestamp);
CREATE INDEX IF NOT EXISTS idx_fragments_type ON memory_fragments(type);

-- ============================================

-- 表 3: 模式（patterns）
CREATE TABLE IF NOT EXISTS patterns (
  id TEXT PRIMARY KEY,
  soul_id TEXT NOT NULL,
  type TEXT NOT NULL,
  description TEXT NOT NULL,
  confidence REAL NOT NULL,
  frequency INTEGER NOT NULL DEFAULT 1,
  first_seen INTEGER NOT NULL,
  last_seen INTEGER NOT NULL,

  instances TEXT NOT NULL,        -- PatternInstance[] (JSON)
  related_patterns TEXT,          -- 相关模式 ID (JSON array)
  prediction TEXT,                -- 预测能力 (JSON)

  FOREIGN KEY (soul_id) REFERENCES digital_souls(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_patterns_soul_id ON patterns(soul_id);
CREATE INDEX IF NOT EXISTS idx_patterns_type ON patterns(type);
CREATE INDEX IF NOT EXISTS idx_patterns_confidence ON patterns(confidence);

-- ============================================

-- 表 4: 对话记录（conversations）
CREATE TABLE IF NOT EXISTS conversations (
  id TEXT PRIMARY KEY,
  soul_id TEXT NOT NULL,
  started_at INTEGER NOT NULL,
  ended_at INTEGER,
  title TEXT,
  message_count INTEGER DEFAULT 0,

  FOREIGN KEY (soul_id) REFERENCES digital_souls(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_conversations_soul_id ON conversations(soul_id);
CREATE INDEX IF NOT EXISTS idx_conversations_started ON conversations(started_at);

-- ============================================

-- 表 5: 消息（messages）
CREATE TABLE IF NOT EXISTS messages (
  id TEXT PRIMARY KEY,
  conversation_id TEXT NOT NULL,
  role TEXT NOT NULL,             -- 'user' | 'assistant' | 'system'
  content TEXT NOT NULL,
  timestamp INTEGER NOT NULL,
  metadata TEXT,                  -- 元数据 (JSON)

  FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages(timestamp);

-- ============================================

-- 表 6: 反馈记录（feedback）
CREATE TABLE IF NOT EXISTS feedback (
  id TEXT PRIMARY KEY,
  soul_id TEXT NOT NULL,
  target_type TEXT NOT NULL,      -- 'value' | 'trait' | 'pattern'
  target_id TEXT NOT NULL,
  accuracy INTEGER NOT NULL,      -- 1-5 评分
  corrections TEXT,               -- 修正内容 (JSON)
  user_notes TEXT,
  created_at INTEGER NOT NULL,

  FOREIGN KEY (soul_id) REFERENCES digital_souls(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_feedback_soul_id ON feedback(soul_id);
CREATE INDEX IF NOT EXISTS idx_feedback_target ON feedback(target_type, target_id);

-- ============================================

-- 表 7: 版本历史（soul_versions）
CREATE TABLE IF NOT EXISTS soul_versions (
  id TEXT PRIMARY KEY,
  soul_id TEXT NOT NULL,
  version INTEGER NOT NULL,
  created_at INTEGER NOT NULL,

  foundation TEXT NOT NULL,       -- 当时的画像 (JSON)
  changes TEXT NOT NULL,          -- 变更内容 (JSON)
  confidence REAL NOT NULL,
  snapshot_id TEXT,               -- 完整快照 ID

  FOREIGN KEY (soul_id) REFERENCES digital_souls(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_versions_soul_id ON soul_versions(soul_id);
CREATE INDEX IF NOT EXISTS idx_versions_version ON soul_versions(version);

-- ============================================
-- 初始化默认数据
-- ============================================

-- 创建默认分身（如果不存在）
INSERT OR IGNORE INTO digital_souls (
  id,
  user_id,
  version,
  created_at,
  updated_at,
  foundation,
  memories,
  metadata
) VALUES (
  'default',
  'default-user',
  1,
  strftime('%s', 'now') * 1000,
  strftime('%s', 'now') * 1000,
  '{"values":[],"personality":[],"thinkingPatterns":[],"decisionPrinciples":[],"mentalModels":[]}',
  '{"interactions":[],"behaviors":[],"focusAreas":[],"workHabits":{},"languageStyle":{}}',
  '{"confidenceScores":{},"evolutionHistory":[],"lastUpdate":' || (strftime('%s', 'now') * 1000) || ',"dataSources":{},"metrics":{"stability":{"score":0,"details":{}},"consistency":{"score":0,"validationCases":0,"matchRate":0},"completeness":{"score":0,"coverage":{},"gaps":[]},"accuracy":{"score":0,"totalSimulations":0,"correctPredictions":0,"trend":[]}}}'
);
