package com.achobeta.themis.infrastructure.user.repo;

import cn.hutool.core.util.ObjectUtil;
import com.achobeta.themis.common.redis.service.IRedisService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final IRedisService redissonService;

    @Override
    public List<ChatMessage> getMessages(Object conversationId) {
        String json = redissonService.getValue(conversationId.toString());
        if (ObjectUtil.isEmpty(json)) {
            return List.of();
        }
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object conversationId, List<ChatMessage> list) {
        String json = ChatMessageSerializer.messagesToJson(list);
        redissonService.setValue(conversationId.toString(), json, Duration.ofMinutes(10).toMillis());
    }

    @Override
    public void deleteMessages(Object conversationId) {
        redissonService.remove(conversationId.toString());
    }
}
