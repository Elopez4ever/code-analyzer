package com.analyzer.infrastructure.ai;

import cn.hutool.core.util.ObjectUtil;

import java.util.Map;
import java.util.Set;

/**
 * AIClient工厂 按配置名称获取对应示例
 */
public class AIClientFactory {

    private final Map<String ,AIClient> clients;

    private final String defaultClientName;

    public AIClientFactory(Map<String, AIClient> clients, String defaultClientName) {
        this.clients = clients;
        this.defaultClientName = defaultClientName;
    }

    public AIClient getClient(String clientName) {
        AIClient client = clients.get(clientName);
        if (ObjectUtil.isNull(client)) {
            throw new IllegalArgumentException();
        }
        return client;
    }

    public AIClient getDefault() {
        return getClient(defaultClientName);
    }
    public Set<String> availableClients() {
        return clients.keySet();
    }
}
