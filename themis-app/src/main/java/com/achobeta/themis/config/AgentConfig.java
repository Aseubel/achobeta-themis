package com.achobeta.themis.config;

import com.achobeta.themis.domain.user.service.IChatService;
import com.achobeta.themis.infrastructure.user.repo.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AgentConfigProperties.class)
public class AgentConfig {
    @Autowired
    private AgentConfigProperties agentConfigProperties;
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Bean
    public IChatService chatService() {
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(agentConfigProperties.getBaseUrl())
                .apiKey(agentConfigProperties.getApiKey())
                .modelName(agentConfigProperties.getModel())
                .build();

        return AiServices.builder(IChatService.class)
                .streamingChatModel(model)
                .chatMemoryProvider(conversationId ->
                        MessageWindowChatMemory.builder()
                                .chatMemoryStore(redisChatMemoryStore)
                                .id(conversationId)
                                .maxMessages(100)
                                .build()
                )
                .build();
    }
}
