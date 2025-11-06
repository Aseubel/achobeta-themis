package com.achobeta.themis.config;

import com.achobeta.themis.domain.user.service.IChatService;
import com.achobeta.themis.infrastructure.user.repo.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@EnableConfigurationProperties(AgentConfigProperties.class)
public class AgentConfig {
    @Autowired
    private AgentConfigProperties agentConfigProperties;
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Bean
    public IChatService chatService() throws IOException {
        // 读取 prompt.txt 文件
        ClassPathResource resource = new ClassPathResource("prompt.txt");
        String systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        log.info("成功加载系统提示词，长度: {} 字符", systemPrompt.length());

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
