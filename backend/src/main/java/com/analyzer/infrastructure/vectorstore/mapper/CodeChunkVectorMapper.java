package com.analyzer.infrastructure.vectorstore.mapper;

import com.analyzer.infrastructure.vectorstore.entity.CodeChunkVector;
import com.analyzer.infrastructure.vectorstore.model.VectorSearchResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CodeChunkVectorMapper extends BaseMapper<CodeChunkVector> {
    void batchUpsert(@Param("list") List<CodeChunkVector> list);
    List<VectorSearchResult> searchSimilar(
            @Param("queryVector") String queryVector,
            @Param("projectId") String projectId,
            @Param("topK") int topK
    );
    List<VectorSearchResult> searchWithFilter(
            @Param("queryVector") String queryVector,
            @Param("projectId") String projectId,
            @Param("topK") int topK,
            @Param("language") String language,
            @Param("chunkType") String chunkType,
            @Param("filePath") String filePath,
            @Param("metadataFilters") Map<String, String> metadataFilters
    );
    void deleteByProjectId(@Param("projectId") String projectId);
    void deleteByProjectAndFile(@Param("projectId") String projectId, @Param("filePath") String filePath);
}