package com.achobeta.themis.trigger.user.http;

import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.domain.user.model.vo.ChatRequestVO;
import com.achobeta.themis.domain.user.service.IChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final IChatService chatService;

    /**
     * 聊天
     * @param request
     * @return
     */
    @PostMapping("/stream")
    public ApiResponse<String> chat(@Valid @RequestBody ChatRequestVO request) {
        try {
            List<String> response = chatService.chat(request.getConversationId(), request.getMessage()).collectList().block();
            String responseStr = String.join("", response);
            return ApiResponse.success(responseStr);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
