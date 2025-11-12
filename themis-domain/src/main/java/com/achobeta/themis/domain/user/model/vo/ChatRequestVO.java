package com.achobeta.themis.domain.user.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequestVO {
    @NotNull(message = "用户ID不能为空")
    private Long id;
    @NotNull(message = "用户类型不能为空")
    private Integer userType;
    @NotBlank(message = "对话ID不能为空")
    private String conversationId;
    @NotBlank(message = "用户提问不能为空")
    private String message;
}