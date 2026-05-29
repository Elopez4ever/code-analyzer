package com.analyzer.infrastructure.ai;

/**
 * AI 调用接口
 */
public interface AIClient {
    String chat(String prompt);
    String chat(String systemPrompt, String userPrompt);
}
