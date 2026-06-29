# Code Analyzer — 代码知识平台

基于 **Spring Boot 3 模块化单体（Modular Monolith）** 构建的代码知识平台。通过对代码仓库进行解析、切片、向量化与语义检索，结合 LLM 实现 **RAG（检索增强生成）** 风格的代码问答与知识管理。

适合作为 Java 后端毕业设计、RAG 简历项目，或企业级代码智能平台的起点。每个模块职责明确，后续拆分为微服务成本极低。

---

## 一、总体架构

```
Client (Web)
        │
        ▼
 REST API / SSE / WebSocket Controller
        │
        ▼
 Application（业务编排层，演进中）
        │
┌────────────────────────────────────────────────────┐
│                   Modules                           │
│                                                     │
│  Project  Parser  KnowledgeBase  Retrieval  Chat   │
│  Task     User(规划中)                              │
└────────────────────────────────────────────────────┘
        │
        ▼
 Infrastructure（基础设施）
   AI  │  VectorStore  │  Persistence  │  WebSocket  │  File
        │
        ▼
 PostgreSQL + pgvector + Spring AI + MyBatis-Plus + JGit
```

系统定位：**Code Knowledge Platform**（代码知识平台），不是简单的聊天机器人。

---

## 二、分层说明

### 2.1 API 层（Controller）

- 参数校验（`@Validated`）
- DTO / VO 转换
- 返回统一响应 `Result<T>`
- WebSocket 推送解析进度
- **不写业务逻辑**

### 2.2 Application 层（演进中）

- 跨模块流程编排
- 例如：`ProjectApplicationService` 编排 `ProjectService → TaskService → Parser → KnowledgeBase`
- 模块之间尽量不直接互相调用，通过 Application 层解耦

### 2.3 Modules（领域模块）

每个模块对自己的领域生命周期负责，通过接口依赖基础设施。

### 2.4 Infrastructure（基础设施）

提供可替换的能力抽象：

| 能力 | 接口 | 实现 |
|------|------|------|
| AI Chat | `ChatService` | Spring AI OpenAI |
| Embedding | `EmbeddingService` | Spring AI OpenAI Embedding |
| 向量存储 | `VectorStoreService` | pgvector（`VectorStoreServiceImpl`） |
| 持久化 | MyBatis-Plus Mapper | PostgreSQL |
| WebSocket | `WebSocketEventService` | Spring WebSocket STOMP |
| 文件 | `FileValidationService` | Apache Tika |

业务模块永远调用接口，不依赖具体实现。

---

## 三、模块设计

### ① Project Module — 项目管理

**职责**：仓库生命周期管理（创建、查询、删除），不解析代码。

**Controller** `ProjectController`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/project/` | 分页查询项目列表 |
| `POST` | `/api/project/create` | 通过 Git URL 创建项目 |
| `POST` | `/api/project/upload` | 通过 ZIP 上传项目 |
| `POST` | `/api/project/delete` | 批量删除项目 |
| `POST` | `/api/project/modify` | 修改项目信息 |

**核心 Service**

- `ProjectService` — 项目 CRUD、Git 克隆编排、ZIP 解压编排
- `GitService` — `cloneRepository()`、`pullRepository()`、`checkoutBranch()`
- `ZipService` — ZIP 上传与解压

**数据表** `project`

| 字段 | 类型 | 说明 |
|------|------|------|
| project_id | TEXT PK | 项目 ID |
| name | TEXT | 项目名称 |
| git_url | TEXT | Git 仓库地址 |
| local_path | TEXT | 本地存储路径 |
| status | INT | 状态：0=待处理 1=解析中 2=已完成 -1=失败 -2=已删除 |
| method | INT | 上传方式：0=Git 1=ZIP |
| chunk_count | INT | 代码块数量 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

### ② Parser Module — 代码解析

**职责**：只负责 Repository → AST → Chunk 的纯解析流水线，**不碰数据库、不碰 AI**。

**核心流水线** `ParsePipeline`

```
Repository 目录
    ↓ 选择 ProjectParser（Maven/Gradle 项目 / 通用项目）
扫描源文件列表 → List<SourceFile>
    ↓ 按语言路由 Chunker
CodeChunker.chunk(file, projectId)
    ↓
输出 List<CodeChunk>
```

**关键组件**

| 组件 | 职责 |
|------|------|
| `ProjectParser`（接口） | 从项目目录提取源文件列表 |
| `MavenGradleProjectParser` | 识别 Maven/Gradle 项目结构 |
| `GenericProjectParser` | 通用项目（按扩展名扫描） |
| `FileLanguageDetector` | 文件语言检测 |
| `ChunkerRouter` | 按语言路由到对应 Chunker |
| `JavaCodeChunker` | Java 方法级切片 |
| `LineGroupChunker` | 通用行分组切片 |
| `SlidingWindowChunker` | 滑动窗口切片 |

**领域对象**

- `CodeChunk` — 代码块（id, projectId, content, language, strategy, role, startLine, endLine, metadata, keywords）
- `SourceFile` — 源文件描述
- `CodeRole` — 代码角色（CLASS / METHOD / FUNCTION / STATEMENT）
- `ChunkStrategy` — 切片策略（JAVA_METHOD / LINE_GROUP / SLIDING_WINDOW）

**支持语言**（可扩展）

Java · XML（MyBatis/POM）· YAML · Properties · SQL · Dockerfile · Markdown · HTML · JSON · Shell

---

### ③ KnowledgeBase Module — 知识库

**整个项目核心**。负责 Chunk → Enrich → Embedding → Vector → Storage。

**核心流水线** `KnowledgeBaseService.ingest()`

```
List<CodeChunk>（来自 Parser）
    ↓ EnricherPipeline.enrich()
  元数据增强 + 摘要生成 + 关键词提取
    ↓ EmbeddingService.embedBatch()
  文本向量化（OpenAI text-embedding-3-small / 可替换）
    ↓ VectorStoreService.storeBatch()
  写入 pgvector（HNSW 索引）
```

**关键组件**

| 组件 | 职责 |
|------|------|
| `EnricherPipeline` | 编排多个 Enricher 的执行顺序 |
| `MetadataEnricher` | 提取类名、方法名、包路径等结构信息 |
| `SummaryEnricher` | 用 LLM 生成代码片段摘要 |
| `KeywordEnricher` | 提取关键词 |
| `EmbeddingService` | 调用 OpenAI Embedding API |
| `VectorStoreService`（接口） | 向量存储与检索 |

**数据表**

- `code_chunks` — 代码块（含 `embedding vector(1536)`、`metadata JSONB`、`keywords JSONB`）
- 向量索引使用 **HNSW**（`vector_cosine_ops`），参数 `m=16, ef_construction=64`

---

### ④ Retrieval Module — 检索召回（规划中）

**职责**：负责 RAG 召回。Chat 不直接查数据库，通过 Retrieval 统一入口。

```
RetrievalService.retrieve(query)
    ├── VectorRetriever.searchVector()     ← 第一版：直接 pgvector 余弦相似度
    ├── KeywordRetriever (可选)            ← 后续：PostgreSQL FTS 或 Elasticsearch
    └── HybridRetriever.merge() (第二版)   ← 混合召回 + Rerank
```

> 当前阶段：向量检索能力已在 `VectorStoreService.search()` 中实现，可直接使用。Retrieval 模块作为独立领域模块的拆分在后继迭代中进行。

---

### ⑤ Chat Module — 对话（构建中）

**职责**：用户问答入口，编排上下文、召回、Prompt 构建、LLM 调用、SSE 流式输出。

**规划架构**

```
ChatController
    ↓
ChatApplicationService
    ↓
ChatService
    ├── ConversationService    — 会话管理（创建、重命名、删除）
    ├── MessageService         — 消息存储（user / assistant）
    ├── HistoryService         — 历史摘要 + 最近消息，控制 Token 窗口
    ├── ContextService         — History → Retriever → Project → System Context
    └── PromptService          — 构建 Prompt（Explain / Review / Refactor 模板）
```

**数据表**（规划）

- `conversation` — 会话（id, project_id, title, summary, model_name, created_at）
- `message` — 消息（id, conversation_id, role, content, prompt_tokens, completion_tokens, created_at）

**当前状态**

- `ChatService` 接口已定义，`ChatSpec` 已建模
- 基础设施层 `ChatService` / `EmbeddingService` / `ModelRegistry` 已就绪
- Chat 模块 Controller + 完整对话链路正在开发中

---

### ⑥ Task Module — 异步任务中心

**职责**：异步解析任务的生命周期管理，通过 Spring Events + WebSocket 推送进度。

**流程**

```
TaskService.submit(projectId, localPath)
    → 发布 TaskStartedEvent
    → TaskExecutor 监听事件
        → ParsePipeline.execute()
        → KnowledgeBaseService.ingest()
        → WebSocket 推送进度
        → TaskService.markDone()
```

**关键组件**

| 组件 | 职责 |
|------|------|
| `TaskService` | 任务创建、状态更新、完成/失败标记 |
| `TaskRecord` | 任务记录（taskId, projectId, status, stage, progress, message） |
| `TaskStartedEvent` | Spring 事件，触发异步解析 |
| `ProgressTracker` / `ProgressTrackerFactory` | 进度追踪及 WebSocket 推送 |

**进度推送**

通过 WebSocket STOMP 协议实时推送解析进度：

```
/app/project/{projectId}/progress → ProgressMessage（stage, progress, message）
```

---

### ⑦ File Module — 文件管理

**职责**：文件上传校验、解压、存储。

**关键组件**

| 组件 | 职责 |
|------|------|
| `FileValidationService` | 文件大小校验、Content-Type 检测（Apache Tika） |
| `ZipService` | ZIP 解压与结构校验 |

---

## 四、项目结构

```
backend/
├── pom.xml
├── db.sql                              # 数据库初始化 DDL + 示例数据
├── README.md
└── src/main/java/com/analyzer/
    ├── App.java                        # Spring Boot 启动类
    ├── DataSourceValidator.java        # 启动时数据源校验
    │
    ├── common/                         # 通用层
    │   ├── config/                     # 配置类
    │   │   ├── AIConfig.java           # AI 模型配置
    │   │   ├── AppConfigProperties.java
    │   │   ├── AsyncConfig.java        # 异步任务线程池
    │   │   ├── CorsConfig.java
    │   │   ├── ParserConfigProperties.java  # 代码解析配置
    │   │   └── SwaggerConfig.java
    │   ├── constant/
    │   │   └── CommonConstants.java
    │   ├── result/
    │   │   ├── Result.java             # 统一响应体
    │   │   └── exception/              # 全局异常处理
    │   └── utils/
    │       └── GitUtils.java
    │
    ├── infrastructure/                 # 基础设施层
    │   ├── ai/
    │   │   ├── chat/                   # ChatService 接口 + ChatSpec
    │   │   ├── embedding/              # EmbeddingService
    │   │   ├── exception/              # AIServiceException
    │   │   └── model/                  # ModelRegistry / ModelProperties
    │   ├── file/
    │   │   └── FileValidationService.java
    │   ├── persistence/                # 持久化（MyBatis-Plus）
    │   │   ├── config/                 # MybatisPlusConfig
    │   │   ├── mapper/                 # ProjectMapper
    │   │   ├── po/                     # ProjectPO
    │   │   └── service/                # ProjectPersistenceService
    │   ├── vectorstore/                # 向量存储
    │   │   ├── VectorStoreService.java    # 接口
    │   │   ├── impl/VectorStoreServiceImpl.java
    │   │   ├── entity/CodeChunkVector.java
    │   │   ├── mapper/CodeChunkVectorMapper.java
    │   │   ├── handler/                # JSONB / Vector TypeHandler
    │   │   └── model/                  # VectorSearchRequest / Result
    │   └── websocket/                  # WebSocket 进度推送
    │       ├── config/WebSocketConfig.java
    │       ├── WebSocketEventService.java
    │       └── entity/ProgressMessage.java
    │
    └── modules/                        # 领域模块
        ├── project/                    # 项目管理
        │   ├── ProjectController.java
        │   ├── model/                  # DTO / VO
        │   ├── service/
        │   │   ├── ProjectService.java
        │   │   ├── GitService.java
        │   │   └── ZipService.java
        │   └── progress/               # 进度追踪
        │       ├── ProgressTracker.java
        │       └── ProgressTrackerFactory.java
        │
        ├── parser/                     # 代码解析
        │   └── pipeline/
        │       ├── ParsePipeline.java
        │       ├── ProjectParser.java  # 接口
        │       ├── CodeChunker.java
        │       ├── chunker/            # 各语言 Chunker 实现
        │       ├── parser/             # ProjectParser 实现
        │       │   └── detector/       # 语言检测
        │       └── domain/             # CodeChunk / SourceFile / ...
        │
        ├── knowledgebase/              # 知识库
        │   ├── KnowledgeBaseService.java
        │   ├── ChunkEnricher.java      # 接口
        │   ├── EnricherPipeline.java
        │   └── enricher/               # Metadata / Summary / Keyword Enricher
        │       ├── extractor/          # 结构提取器
        │       └── rule/               # 提取规则
        │
        ├── chat/                       # 对话（构建中）
        │
        └── task/                       # 异步任务
            ├── service/TaskService.java
            ├── domain/                 # TaskRecord / TaskStatus / TaskStage
            └── event/                  # TaskStartedEvent
```

---

## 五、技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.3.0 |
| JDK | Java | 17 |
| AI | Spring AI (OpenAI Compatible) | 1.0.0 |
| 数据库 | PostgreSQL | — |
| 向量检索 | pgvector | 0.1.6 |
| ORM | MyBatis-Plus | 3.5.7 |
| Git | JGit | 6.10.0 |
| Java 解析 | JavaParser | 3.26.2 |
| 文件检测 | Apache Tika | 1.28.5 |
| 工具 | Hutool | 5.8.28 |
| 文档 | SpringDoc OpenAPI | 2.5.0 |
| 流式推送 | WebSocket (STOMP) | — |
| 高可用 | Resilience4j (Retry + CircuitBreaker) | 2.2.0 |
| IDE | Lombok | — |

---

## 六、快速开始

### 6.1 环境要求

- JDK 17+
- Maven 3.8+
- PostgreSQL 15+（需安装 `pgvector` 扩展）
- （可选）Redis

### 6.2 数据库初始化

```bash
# 1. 创建数据库
createdb code_analyzer

# 2. 执行初始化脚本
psql -d code_analyzer -f db.sql
```

### 6.3 配置文件

复制并修改 `src/main/resources/application-local.example.yml`：

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

必填配置项：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/code_analyzer
    username: postgres
    password: <YOUR_DB_PASSWORD>
  ai:
    openai:
      api-key: <YOUR_OPENAI_API_KEY>
      base-url: https://api.openai.com
      embedding:
        options:
          model: text-embedding-3-small
          dimensions: 1536

app:
  project:
    upload-path: <YOUR_UPLOAD_DIR>   # 如 D:/data/repos

analyzer:
  ai:
    default-chat-model: gpt-4o
    models:
      gpt-4o:
        provider: openai
        api-key: <YOUR_OPENAI_API_KEY>
        base-url: https://api.openai.com/v1
        model-name: gpt-4o
```

### 6.4 启动

```bash
mvn spring-boot:run
```

启动后访问：

- API 文档：http://localhost:8080/swagger-ui.html
- API JSON：http://localhost:8080/v3/api-docs

---

## 七、核心数据流

### 7.1 导入项目 → 知识入库

```
POST /api/project/create  或  POST /api/project/upload
        │
        ▼
ProjectService.saveFromGit() / saveFromZip()
        │
        ├── GitService.cloneRepository() / ZipService.unzip()
        ├── 持久化 project 记录（status=PARSING）
        ├── WebSocket 推送进度
        └── TaskService.submit()
                │
                ▼ Spring Event: TaskStartedEvent
        TaskExecutor（异步）
                │
                ▼
        ParsePipeline.execute()
            ├── ProjectParser.parse() → List<SourceFile>
            └── ChunkerRouter.chunk() → List<CodeChunk>
                │
                ▼
        KnowledgeBaseService.ingest()
            ├── EnricherPipeline.enrich()  → 元数据 + 摘要 + 关键词
            ├── EmbeddingService.embedBatch() → 向量化
            └── VectorStoreService.storeBatch() → pgvector
                │
                ▼
        TaskService.markDone()
        WebSocket → 客户端推送完成通知
```

### 7.2 代码问答（规划流程）

```
POST /chat
        │
        ▼
ChatApplicationService
        │
        ▼
ChatService
    ├── HistoryService        → 加载历史 + 摘要
    ├── ContextService        → 构建上下文
    │       └── RetrievalService.retrieve(query)
    │               └── VectorRetriever.searchVector()  ← pgvector
    ├── PromptService         → 构建 System + User Prompt
    └── ChatClient (Spring AI) → SSE 流式输出
```

---

## 八、设计决策

### 为什么是模块化单体而不是微服务？

1. **项目定位**：毕业设计 / 简历项目 / MVP 阶段，模块化单体足以验证完整链路
2. **低运维成本**：只需一个 PostgreSQL + 一个 Spring Boot 进程
3. **易于演进**：模块间通过接口解耦，拆分为微服务时只需：
   - 将 `infrastructure` 接口改为 RPC/HTTP 调用
   - 每个模块独立部署
   - 无需重写业务逻辑

### 为什么第一版不用 Elasticsearch？

- pgvector 的 HNSW 索引在百万级向量下性能足够
- PostgreSQL 的 `tsvector` + GIN 索引可覆盖关键词全文检索
- 不引入额外基础设施，降低学习与运维成本
- 后续需要 ES 时，只需为 `KeywordRetriever` 增加一个 `ElasticsearchKeywordRetriever` 实现

### 为什么 Parser 不直接写数据库？

- Parser 只输出 `List<CodeChunk>`，职责单一
- 可独立测试、独立替换解析引擎
- 解耦了解析逻辑与存储逻辑，便于后续支持新的解析策略（如 Tree-sitter、ANTLR）

### 为什么分离 KnowledgeBase 和 Retrieval？

- KnowledgeBase 负责"写入"（入库），Retrieval 负责"读出"（召回）
- 检索策略（向量 / 关键词 / 混合）独立演进，不影响入库流程
- 符合 CQRS 思路

---

## 九、路线图

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 项目导入（Git / ZIP）、代码解析、向量入库、进度推送 | ✅ 已完成 |
| Phase 2 | Chat 对话链路（会话管理、消息历史、上下文构建、SSE 流式输出） | 🚧 构建中 |
| Phase 3 | Retrieval 模块独立拆分（VectorRetriever + KeywordRetriever） | 📋 规划中 |
| Phase 4 | User 模块（注册登录、JWT、用户偏好） | 📋 规划中 |
| Phase 5 | 混合召回 + Rerank + Prompt 模板体系 | 📋 规划中 |
| Phase 6 | Docker Compose 一键部署 | 📋 规划中 |

---

## 十、许可证

仅供学习与个人项目使用。
