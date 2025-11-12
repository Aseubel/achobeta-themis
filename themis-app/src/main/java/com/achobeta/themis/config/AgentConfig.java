package com.achobeta.themis.config;

import com.achobeta.themis.common.agent.service.IAiAdjudicatorService;
import com.achobeta.themis.common.agent.tool.MeilisearchTool;
import com.achobeta.themis.common.agent.service.IAiChatService;
import com.achobeta.themis.infrastructure.user.repo.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.SystemMessage;
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
    @Autowired
    private MeilisearchTool meilisearchTool;



    @Bean("consulter")
    public IAiChatService consulterService() throws IOException {
        // 读取 prompt-consulter.txt 文件
        ClassPathResource resource = new ClassPathResource("prompt-consulter.txt");
        String systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        log.info("成功加载系统提示词，长度: {} 字符", systemPrompt.length());

        // 创建系统消息
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(agentConfigProperties.getBaseUrl())
                .apiKey(agentConfigProperties.getApiKey())
                .modelName(agentConfigProperties.getModel())
                .build();

        return AiServices.builder(IAiChatService.class)
                .streamingChatModel(model)
                .chatMemoryProvider(conversationId ->{
                      MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                              .chatMemoryStore(redisChatMemoryStore)
                              .id(conversationId)
                              .maxMessages(100)
                              .build();

                      boolean hasSystemMessage = memory.messages().stream()
                              .anyMatch(msg -> msg instanceof SystemMessage);
                      if (!hasSystemMessage) {
                          memory.add(systemMessage);
                      }
                      return memory;
                })
                .build();
    }


    @Bean("adjudicator")
    public IAiAdjudicatorService adjudicatorService() throws IOException {
        ClassPathResource resource = new ClassPathResource("prompt-adjudicator.txt");
        String systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        log.info("成功加载系统提示词，长度: {} 字符", systemPrompt.length());
        log.error(systemPrompt);
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(agentConfigProperties.getBaseUrl())
                .apiKey(agentConfigProperties.getApiKey())
                .modelName(agentConfigProperties.getModel())
                .logRequests(true)
                .logResponses(true)
                .build();
        return AiServices.builder(IAiAdjudicatorService.class)
                .chatModel(model)
                .chatMemoryProvider(conversationId ->{
                    MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                            .chatMemoryStore(redisChatMemoryStore)
                            .id(conversationId)
                            .maxMessages(2)
                            .build();
                    boolean hasSystemMessage = memory.messages().stream()
                            .anyMatch(msg -> msg instanceof SystemMessage);
                    if (!hasSystemMessage) {
                        memory.add(systemMessage);
                    }
                    return memory;
                })
                .tools(meilisearchTool)
                .build();
        }

}
