package com.achobeta.themis.config;

import com.achobeta.themis.domain.user.service.IChatService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AgentConfigProperties.class)
public class AgentConfig {
    @Autowired
    private AgentConfigProperties agentConfigProperties;

    @Bean
    public IChatService chatService() {
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(agentConfigProperties.getBaseUrl())
                .apiKey(agentConfigProperties.getApiKey())
                .modelName(agentConfigProperties.getModel())
                .build();
        InMemoryChatMemoryStore memoryStore = new InMemoryChatMemoryStore();
        return AiServices.builder(IChatService.class)
                .streamingChatModel(model)
                .chatMemoryProvider(conversationId ->
                        MessageWindowChatMemory.builder()
                                .chatMemoryStore(memoryStore)
                                .id(conversationId)
                                .maxMessages(100)
                                .build()
                )
                .build();
    }
}
