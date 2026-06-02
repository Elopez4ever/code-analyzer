-- V1__create_code_chunks_table.sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS code_chunks (
    id          TEXT PRIMARY KEY,
    project_id  TEXT NOT NULL,
    file_path   TEXT NOT NULL,
    language    TEXT NOT NULL,
    chunk_type  TEXT NOT NULL,
    start_line  INT NOT NULL DEFAULT 0,
    end_line    INT NOT NULL DEFAULT 0,
    content     TEXT NOT NULL,
    summary     TEXT,
    embedding   vector(1536),
    metadata    JSONB DEFAULT '{}',
    keywords    JSONB DEFAULT '[]',
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 向量索引 (HNSW)
CREATE INDEX IF NOT EXISTS idx_code_chunks_embedding
    ON code_chunks USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- 过滤索引
CREATE INDEX IF NOT EXISTS idx_code_chunks_project ON code_chunks (project_id);
CREATE INDEX IF NOT EXISTS idx_code_chunks_project_file ON code_chunks (project_id, file_path);
CREATE INDEX IF NOT EXISTS idx_code_chunks_project_type ON code_chunks (project_id, chunk_type);
CREATE INDEX IF NOT EXISTS idx_code_chunks_project_lang ON code_chunks (project_id, language);