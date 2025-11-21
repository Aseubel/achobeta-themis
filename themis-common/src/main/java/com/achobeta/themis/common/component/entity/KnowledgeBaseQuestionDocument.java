package com.achobeta.themis.common.component.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeBaseQuestionDocument {
    private String id;
    // 数据库问题ID
    private Long questionId;
    private String question;
    private String questionSegmented;
    private Integer count;
}
