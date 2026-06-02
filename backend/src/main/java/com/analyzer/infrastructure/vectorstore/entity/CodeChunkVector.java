package com.analyzer.infrastructure.vectorstore.entity;

import com.analyzer.infrastructure.vectorstore.handler.JsonbListTypeHandler;
import com.analyzer.infrastructure.vectorstore.handler.JsonbMapTypeHandler;
import com.analyzer.infrastructure.vectorstore.handler.VectorTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@TableName(value = "code_chunk", autoResultMap = true)
public class CodeChunkVector {
    @TableId(type = IdType.INPUT)
    private String id;
    private String projectId;
    /** 源文件相对路径 */
    private String filePath;
    /** 文件语言: JAVA, TYPESCRIPT, XML, YAML... */
    private String language;
    /** chunk 类型: CLASS, METHOD, SQL_STATEMENT... */
    private String chunkType;
    private Integer startLine;
    private Integer endLine;
    /** 原始代码内容 */
    private String content;
    /** 自然语言摘要（用于展示和嵌入） */
    private String summary;
    @TableField(typeHandler = VectorTypeHandler.class)
    private float[] embedding;
    /** 结构化属性: className, methodName, packageName 等 */
    @TableField(typeHandler = JsonbMapTypeHandler.class)
    private Map<String, String> metadata;
    /** 搜索标签: ["cache", "transactional", "spring-data"] */
    @TableField(typeHandler = JsonbListTypeHandler.class)
    private List<String> keywords;
    private LocalDateTime createdAt;
}
