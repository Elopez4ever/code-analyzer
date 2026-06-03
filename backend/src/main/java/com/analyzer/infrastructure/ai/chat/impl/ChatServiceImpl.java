package com.analyzer.infrastructure.ai.chat.impl;

import com.analyzer.infrastructure.ai.chat.ChatModelFactory;
import com.analyzer.infrastructure.ai.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatModelFactory chatModelFactory;

    @Override
    public String chat(String userMessage) {
        return chat(null, null, userMessage);
    }

    @Override
    public String chat(String modelKey, String systemPrompt, String userMessage) {
        ChatModel chatModel = resolveModel(modelKey);
        Prompt prompt = buildPrompt(systemPrompt, userMessage);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    @Override
    public Flux<String> chatStream(String modelKey, String systemPrompt, String userMessage) {
        ChatModel model = resolveModel(modelKey);
        Prompt prompt = buildPrompt(systemPrompt, userMessage);
        return model.stream(prompt)
                .mapNotNull(res -> {
                    var result = res.getResult();
                    if (result == null) return null;
                    var output = result.getOutput();
                    if (output == null) return null;
                    return output.getText();
                });
    }

    @Override
    public Set<String> availableModels() {
        return chatModelFactory.availableModels();
    }

    private ChatModel resolveModel(String modelKey) {
        if (StringUtils.hasText(modelKey)) {
            return chatModelFactory.getModel(modelKey);
        }
        return chatModelFactory.getDefaultModel();
    }

    private Prompt buildPrompt(String systemPrompt, String userMessage) {
        ArrayList<Message> messages = new ArrayList<>();
        if (StringUtils.hasText(systemPrompt)) {
            messages.add(new SystemMessage(systemPrompt));
        }
        messages.add(new UserMessage(userMessage));
        return new Prompt(messages);
    }
}
