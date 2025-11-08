package com.achobeta.themis.domain.user.service;

import com.achobeta.themis.common.component.entity.QuestionTitleDocument;

import java.util.List;

public interface IChatService {
    /**
     * 搜索问题标题
     * @return
     */
    List<List<QuestionTitleDocument>> searchQuestionTitles();

}
