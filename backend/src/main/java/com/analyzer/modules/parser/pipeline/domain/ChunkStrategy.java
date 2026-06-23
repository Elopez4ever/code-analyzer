package com.analyzer.modules.parser.pipeline.domain;

/**
 * 切分策略
 */
public enum ChunkStrategy {
    /** 专用 chunker 按语义切分 */
    SEMANTIC,
    /** 按空行/注释分段 */
    LINE_GROUP,
    /** 滑动窗口兜底 */
    SLIDING_WINDOW,
    /** 文件足够小，整体入库 */
    WHOLE_FILE
}
