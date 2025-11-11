package com.achobeta.themis.trigger.agent.http;

import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.agent.service.IAiChatService;
import com.achobeta.themis.common.component.entity.QuestionTitleDocument;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.domain.user.model.vo.ChatRequestVO;
import com.achobeta.themis.domain.user.service.IAdjudicatorService;
import com.achobeta.themis.domain.user.service.IChatService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    @Autowired
    @Qualifier("consulter")
    private IAiChatService consulterService;
    @Autowired
    @Qualifier("threadPoolExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final IAdjudicatorService adjudicatorService;

    private final IChatService chatService;

    @Autowired
    @Qualifier("redisChatMemoryStore")
    private ChatMemoryStore chatMemoryStore;
    
    /**
     * Ai问答聊天
     * @param request
     * @return
     */
    @PostMapping("/consult")
    public ApiResponse<String> chat(@Valid @RequestBody ChatRequestVO request) {
        try {
            threadPoolTaskExecutor.execute(() -> {
                log.info("异步处理问题分类");
                adjudicatorService.adjudicate(request.getUserType(), request.getConversationId(), request.getMessage());
            });
            threadPoolTaskExecutor.execute(() -> {
                log.info("异步保存对话记录");
                // TODO 异步保存对话记录
                //chatService.saveChatRecord(request.getId(), request.getConversationId(), request.getMessage());
            });
            List<String> response = consulterService.chat(request.getConversationId(), request.getMessage()).collectList().block();
            String responseStr = String.join("", response);
            return ApiResponse.success(responseStr);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ai问答聊天失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询对话记录
     */
    @GetMapping("/history")
    public ApiResponse<List<ChatHistoryVO>> history(
            @RequestParam("conversationId") @NotBlank(message = "对话ID不能为空") String conversationId
    ) {
        try {
            List<ChatMessage> messages = chatMemoryStore.getMessages(conversationId);
            List<ChatHistoryVO> history = messages.stream()
                    .map(this::toHistory)
                    .collect(Collectors.toList());
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("查询对话历史失败", e);
            return ApiResponse.error("查询对话历史失败: " + e.getMessage());
        }
    }

    /**
     * 删除对话记录
     */
    @DeleteMapping("/history")
    public ApiResponse<Void> resetHistory(
            @RequestParam("conversationId") @NotBlank(message = "对话ID不能为空") String conversationId
    ) {
        try {
            chatMemoryStore.deleteMessages(conversationId);
            log.info("已重置对话历史，conversationId: {}", conversationId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("重置对话历史失败", e);
            return ApiResponse.error("重置对话历史失败: " + e.getMessage());
        }
    }

    /**
     * 查询常问问题（二级标题）
     * @return
     */
    @GetMapping("/secondary-question-titles")
    public ApiResponse<List<List<QuestionTitleDocument>>> searchQuestionTitles() {
        try {
            List<List<QuestionTitleDocument>> questionTitleDocuments = chatService.searchQuestionTitles();
            return ApiResponse.success(questionTitleDocuments);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询常问问题失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatHistoryVO {
        private String role;
        private String content;
        private LocalDateTime timestamp;
    }

    private ChatHistoryVO toHistory(ChatMessage message) {
        return new ChatHistoryVO(
                message.type().name(),
                resolveContent(message),
                LocalDateTime.now()
        );
    }

    private String resolveContent(ChatMessage message) {
        try {
            Method textMethod = message.getClass().getMethod("text");
            Object value = textMethod.invoke(message);
            if (value instanceof String text) {
                return text;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        return message.toString();
    }

//    /**
//     * Ai改合同
//     */
//    @PostMapping("/stream")
//    public ApiResponse<String> chat(@Valid @RequestBody ChatRequestVO request) {
//        try {
//            List<String> response = chatService.chat(request.getConversationId(), request.getMessage()).collectList().block();
//
//            String responseStr = String.join("", response);
//            return ApiResponse.success(responseStr);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("用户登出失败", e);
//            return ApiResponse.error(e.getMessage());
//        }
//    }
    


}
