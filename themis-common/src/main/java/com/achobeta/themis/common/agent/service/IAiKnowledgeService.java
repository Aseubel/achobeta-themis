package com.achobeta.themis.common.agent.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * <p>
 * 描述：
 * </p>
 *
 * @Author: ZGjie20
 * @version: 1.0.0
 */

/**
 *
 * */
public interface IAiKnowledgeService {
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
