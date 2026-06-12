-- db.sql
-- Code Analyzer 数据库初始化脚本
-- 使用前先创建数据库: CREATE DATABASE code_analyzer;

-- 安装向量插件

CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================
-- 项目表
-- ============================================
CREATE TABLE IF NOT EXISTS project (
                                       project_id    TEXT PRIMARY KEY,
                                       name          TEXT NOT NULL,
                                       git_url       TEXT,
                                       local_path    TEXT,
                                       status        INT NOT NULL DEFAULT 0,
                                       method       INT NOT NULL DEFAULT 0,
                                       chunk_count   INT DEFAULT 0,
                                       created_at    TIMESTAMP DEFAULT NOW(),
                                       updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_project_project_id ON project (project_id);

-- ============================================
-- 代码块表
-- ============================================
-- noinspection SqlNoDataSourceInspectionForFile
-- noinspection SqlResolve
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
-- noinspection SqlNoDataSourceInspectionForFile
-- noinspection SqlResolve
CREATE INDEX IF NOT EXISTS idx_code_chunks_embedding
    ON code_chunks USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- 过滤索引
CREATE INDEX IF NOT EXISTS idx_code_chunks_project ON code_chunks (project_id);
CREATE INDEX IF NOT EXISTS idx_code_chunks_project_file ON code_chunks (project_id, file_path);
CREATE INDEX IF NOT EXISTS idx_code_chunks_project_type ON code_chunks (project_id, chunk_type);
CREATE INDEX IF NOT EXISTS idx_code_chunks_project_lang ON code_chunks (project_id, language);

INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-001', 'Spring Boot Demo', 'https://github.com/example/spring-boot-demo', '/data/repos/spring-boot-demo', 1, 0, 128, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-002', 'React Frontend', 'https://github.com/example/react-frontend', '/data/repos/react-frontend', 1, 1, 256, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-004', 'ML Model Service', 'https://github.com/example/ml-model-service', '/data/repos/ml-model-service', 0, 1, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-005', 'Auth Microservice', 'https://github.com/example/auth-service', '/data/repos/auth-service', 1, 0, 89, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-006', 'Config Center', NULL, '/data/repos/config-center', 0, 0, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-008', 'API Gateway', 'https://github.com/example/api-gateway', '/data/repos/api-gateway', 1, 0, 312, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-009', 'Notification Service', 'https://github.com/example/notification-service', '/data/repos/notification-service', 1, 1, 174, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-010', 'User Management', 'https://github.com/example/user-management', '/data/repos/user-management', 1, 0, 203, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-012', 'Inventory System', 'https://github.com/example/inventory-system', '/data/repos/inventory-system', 0, 0, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-013', 'Order Service', 'https://github.com/example/order-service', '/data/repos/order-service', 1, 1, 445, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-014', 'Search Engine', 'https://github.com/example/search-engine', '/data/repos/search-engine', 1, 0, 678, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-015', 'File Storage Service', 'https://github.com/example/file-storage', '/data/repos/file-storage', 0, 1, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-016', 'Cache Manager', NULL, '/data/repos/cache-manager', 1, 0, 56, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-018', 'Analytics Dashboard', 'https://github.com/example/analytics-dashboard', '/data/repos/analytics-dashboard', 1, 1, 521, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-019', 'CI/CD Pipeline', 'https://github.com/example/cicd-pipeline', NULL, 0, 0, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-020', 'Kubernetes Operator', 'https://github.com/example/k8s-operator', '/data/repos/k8s-operator', 1, 1, 298, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-021', 'GraphQL Server', 'https://github.com/example/graphql-server', '/data/repos/graphql-server', 1, 0, 187, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-023', 'Email Service', 'https://github.com/example/email-service', '/data/repos/email-service', 1, 0, 93, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-024', 'PDF Generator', NULL, '/data/repos/pdf-generator', 0, 1, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-025', 'Report Service', 'https://github.com/example/report-service', '/data/repos/report-service', 1, 0, 341, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-026', 'OAuth Provider', 'https://github.com/example/oauth-provider', '/data/repos/oauth-provider', 1, 1, 412, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-028', 'Service Mesh', 'https://github.com/example/service-mesh', '/data/repos/service-mesh', 0, 0, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-029', 'Tracing System', 'https://github.com/example/tracing-system', '/data/repos/tracing-system', 1, 1, 267, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-030', 'Monitoring Agent', 'https://github.com/example/monitoring-agent', '/data/repos/monitoring-agent', 1, 0, 199, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-017', 'Message Queue', 'https://github.com/example/message-queue', '/data/repos/message-queue', -1, 0, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-022', 'WebSocket Service', 'https://github.com/example/websocket-service', '/data/repos/websocket-service', -1, 1, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-027', 'Rate Limiter', 'https://github.com/example/rate-limiter', '/data/repos/rate-limiter', 0, 0, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-003', 'Data Pipeline', 'https://github.com/example/data-pipeline', '/data/repos/data-pipeline', 0, 0, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-007', 'Log Aggregator', 'https://github.com/example/log-aggregator', NULL, 1, 1, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
INSERT INTO "project" ("project_id", "name", "git_url", "local_path", "status", "method", "chunk_count", "created_at", "updated_at") VALUES ('proj-011', 'Payment Gateway', 'https://github.com/example/payment-gateway', '/data/repos/payment-gateway', 1, 1, 0, '2026-06-10 09:15:52.137034', '2026-06-10 09:15:52.137034');
