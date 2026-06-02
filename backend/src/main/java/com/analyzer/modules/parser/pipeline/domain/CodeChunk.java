package com.analyzer.modules.parser.pipeline.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class CodeChunk {
    private String id;
    private String projectId;
    private String filePath;

    /**
     * 贯穿 pipeline 的路由标识
     * 文件分类阶段: 判断该文件是否支持、归到哪一类
     * 文件拆分阶段: 决定用哪个 chunker 把文件拆成 chunk
     * enricher 阶段: 决定用哪个 extractor 提取 metadata
     */
    private FileLanguage language;

    /**
     * 表示这个 chunk 在语义上是什么粒度的东西
     * 同一个文件拆出来的多个 chunk 类型可能不同
     * e.g.
     * OrderService.java -> CLASS, METHOD, METHOD
     * OrderMapper.xml   -> SQL_STATEMENT, SQL_STATEMENT
     * pom.xml           -> CONFIG_BLOCK
     * 用于: enricher 按 chunkType 选不同规则集、检索时按类型过滤、展示时区分粒度
     */
    private CodeChunkType chunkType;

    private String content;
    private int startLine;
    private int endLine;

    // enricher 填充需要填充的部分

    /**
     * 结构化属性, 存放该 chunk 结构化事实
     * 不同的文件类型存的 key 不同
     * e.g.
     * Java   -> className, methodName, packageName, returnType
     * pom    -> groupId, artifactId, version
     * mapper -> namespace, statementId, statementType, tables
     * yaml   -> configScope, profile
     * 用于精确定位：按类名查方法 按表名查 SQL 按 artifactId 查依赖
     */
    private Map<String, String> metadata;

    /**
     * 搜索标签, 描述该 chunk 的技术特征和行为模式
     * 由 KeywordEnricher 根据规则匹配生成，描述该 chunk 的技术特征和行为模式
     * e.g. ["cache", "transactional", "spring-data", "type:method"]
     * 用于模糊检索：用户问"哪里用了缓存"时按 keyword 过滤命中
     */
    private List<String> keywords;

    /**
     * 自然语言摘要, 用一句话描述该 chunk 做了什么。
     * e.g. "根据客户ID查询订单列表，带缓存和只读事务"
     * 用于向量化嵌入和语义搜索：用户用自然语言提问时与 summary 做相似度匹配
     */
    private String summary;
}
