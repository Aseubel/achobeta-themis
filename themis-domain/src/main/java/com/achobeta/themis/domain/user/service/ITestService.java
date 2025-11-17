package com.achobeta.themis.domain.user.service;

import com.achobeta.themis.domain.user.model.entity.QuestionDTO;

import java.util.List;

public interface ITestService {
    List<QuestionDTO> queryQuestions();

}
