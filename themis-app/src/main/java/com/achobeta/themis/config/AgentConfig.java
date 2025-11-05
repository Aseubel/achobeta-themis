package com.achobeta.themis.config;

import com.achobeta.themis.domain.user.service.IChatService;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
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

    @Bean
    public IChatService chatService() throws IOException {
        // 读取 prompt.txt 文件
        ClassPathResource resource = new ClassPathResource("prompt.txt");
        String systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        log.info("成功加载系统提示词，长度: {} 字符", systemPrompt.length());
        
        // 创建系统消息
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(agentConfigProperties.getBaseUrl())
                .apiKey(agentConfigProperties.getApiKey())
                .modelName(agentConfigProperties.getModel())
                .build();
        InMemoryChatMemoryStore memoryStore = new InMemoryChatMemoryStore();
        return AiServices.builder(IChatService.class)
                .streamingChatModel(model)
                .chatMemoryProvider(conversationId -> {
                    MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                            .chatMemoryStore(memoryStore)
                            .id(conversationId)
                            .maxMessages(100)
                            .build();
                    // 如果内存中没有系统消息，则添加
                    boolean hasSystemMessage = memory.messages().stream()
                            .anyMatch(msg -> msg instanceof SystemMessage);
                    if (!hasSystemMessage) {
                        memory.add(systemMessage);
                    }
                    return memory;
                })
                .build();
    }
}
