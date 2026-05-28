# Code Analyzer

代码知识库系统后端。上传 ZIP 或 Git Clone 代码仓库 → AST 解析切片 → 向量化存入 pgvector，为后续 AI 代码问答提供语义检索。

## 技术栈

- Java 17 / Spring Boot 3.3
- PostgreSQL + pgvector（向量存储）
- MyBatis-Plus 3.5（ORM）
- JavaParser 3.26（AST 代码解析）
- JGit 6.10（Git 操作，纯 Java，不依赖系统 git）
- Apache Tika（文件类型检测）
- Hutool（工具类）/ Lombok

## 项目结构

```
com.analyzer
├── common                  # 配置、异常、统一响应
├── infrastructure
│   ├── embedding           # 向量嵌入调用
│   ├── file                # 文件校验（大小/类型）
│   ├── persistence         # PO、Mapper、持久化 Service
│   └── vectorstore         # pgvector 读写
├── modules
│   ├── project             # 项目管理（上传/克隆/状态查询）
│   ├── parser
│   │   ├── pipeline        # 解析流水线，CodeChunk 在这里产生和消费
│   │   └── service         # ProjectParsingService（异步入口）
│   ├── knowledgebase       # 知识库（规划中）
│   └── chat                # AI 对话（规划中）
```

## 核心流程

```
上传 ZIP / Git URL
  → ProjectService 落库（状态 PARSING）
  → @Async ProjectParsingService.parseAsync()
      → ProjectParsingPipeline.execute()
          → JavaParser AST 切分 → CodeChunk（中间模型，不入库）
          → embedding → 向量写入 pgvector
      → 成功: 状态 → READY / 失败: 状态 → FAILED
```

## 关键设计决策

| 点               | 说明                                                   |
| ---------------- | ------------------------------------------------------ |
| CodeChunk 不入库 | 它是 pipeline 中间产物，最终以向量形式存入 vectorstore |
| 解析异步         | 上传/克隆后立即返回，后台解析完更新状态                |
| zip-slip 防护    | 解压时校验路径，防止目录穿越                           |
| 文件校验         | 10MB 大小限制 + Tika 检测类型 + 白名单                 |

## 本地运行

```bash
# 前置：PostgreSQL 15+ 并启用 pgvector
psql -c "CREATE EXTENSION IF NOT EXISTS vector;"

# 配置 application.yml 中的数据库连接和 upload-path

mvn spring-boot:run
```

## 当前 API

| 方法 | 路径                       | 说明                       |
| ---- | -------------------------- | -------------------------- |
| POST | `/api/project/upload`      | 上传 ZIP 创建项目          |
| POST | `/api/project/create`      | Git URL 创建项目（开发中） |
| GET  | `/api/project/{id}/chunks` | 查看解析结果（开发中）     |

## 待开发

- 前端
- Chat 模块（基于向量检索的代码问答）
- 知识库管理
- Git URL 导入接口完善