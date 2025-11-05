package com.achobeta.themis.domain.user.model.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequestVO {
    @NotBlank(message = "对话ID不能为空")
    private String conversationId;
    @NotBlank(message = "消息内容不能为空")
    private String message;
}