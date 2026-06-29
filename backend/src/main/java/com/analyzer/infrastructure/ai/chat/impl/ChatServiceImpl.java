package com.analyzer.infrastructure.ai.chat.impl;

import com.analyzer.infrastructure.ai.chat.ChatService;
import com.analyzer.infrastructure.ai.chat.ChatSpec;
import com.analyzer.infrastructure.ai.exception.AIServiceException;
import com.analyzer.infrastructure.ai.model.ModelInfo;
import com.analyzer.infrastructure.ai.model.ModelRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ModelRegistry modelRegistry;

    @Override
    public ChatSpec prompt() {
        return new DefaultChatSpec(this);
    }

    @Retry(name = "chat-service")
    @CircuitBreaker(name = "chat-service", fallbackMethod = "chatFallback")
    ChatResponse chat(ChatRequest request) {
        String resolvedModel = modelRegistry.resolveModel(request.getModel());
        long start = System.currentTimeMillis();

        try {
            var spec = chatClient.prompt();

            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
                spec = spec.system(request.getSystemPrompt());
            }

            spec = spec.options(buildOptions(resolvedModel, request));

            String content = spec.user(request.getUserMessage())
                    .call()
                    .content();

            long latency = System.currentTimeMillis() - start;
            log.debug("Chat 完成 | model={} | latency={}ms", resolvedModel, latency);

            return ChatResponse.builder()
                    .content(content)
                    .model(resolvedModel)
                    .latencyMs(latency)
                    .build();
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("Chat 失败 | model={} | latency={}ms | error={}", resolvedModel, latency, e.getMessage());
            throw new AIServiceException("Chat 调用失败: " + e.getMessage(), e, true);
        }
    }

    @CircuitBreaker(name = "chat-service")
    Flux<String> chatStream(ChatRequest request) {
        String resolvedModel = modelRegistry.resolveModel(request.getModel());

        try {
            var spec = chatClient.prompt();

            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
                spec = spec.system(request.getSystemPrompt());
            }

            spec = spec.options(buildOptions(resolvedModel, request));

            return spec.user(request.getUserMessage())
                    .stream()
                    .content()
                    .doOnSubscribe(s -> log.debug("Stream 开始 | model={}", resolvedModel))
                    .doOnComplete(() -> log.debug("Stream 完成 | model={}", resolvedModel))
                    .doOnError(e -> log.error("Stream 异常 | model={} | error={}", resolvedModel, e.getMessage()));

        } catch (Exception e) {
            log.error("Stream 初始化失败 | model={} | error={}", resolvedModel, e.getMessage());
            return Flux.error(new AIServiceException("Stream 初始化失败: " + e.getMessage(), e, true));
        }
    }

    // fallback 方法签名必须与 chat(ChatRequest) 一致，多一个 Throwable 参数
    private ChatResponse chatFallback(ChatRequest request, Throwable t) {
        log.warn("Chat 熔断降级 | model={} | cause={}", request.getModel(), t.getMessage());
        return ChatResponse.builder()
                .content("服务暂时不可用，请稍后重试")
                .model(request.getModel())
                .latencyMs(-1)
                .build();
    }

    private OpenAiChatOptions buildOptions(String model, ChatRequest request) {
        var builder = OpenAiChatOptions.builder().model(model);

        ModelInfo modelInfo = modelRegistry.getModelInfo(model).orElse(null);

        if (request.getTemperature() != null) {
            builder.temperature(request.getTemperature());
        } else if (modelInfo != null) {
            builder.temperature(modelInfo.getTemperature());
        }

        if (request.getMaxTokens() != null) {
            builder.maxTokens(request.getMaxTokens());
        } else if (modelInfo != null) {
            builder.maxTokens(modelInfo.getMaxTokens());
        }

        return builder.build();
    }
}