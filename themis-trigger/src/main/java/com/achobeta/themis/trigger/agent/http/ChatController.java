package com.achobeta.themis.trigger.agent.http;

import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.component.entity.QuestionTitleDocument;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.domain.user.model.vo.ChatRequestVO;
import com.achobeta.themis.domain.user.service.IAdjudicatorService;
import com.achobeta.themis.common.agent.service.IAiChatService;
import com.achobeta.themis.domain.user.service.IChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * TODO
     * 查询对话记录
     */

    /**
     * TODO
     * 删除对话记录
     */

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
