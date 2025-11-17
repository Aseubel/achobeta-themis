package com.achobeta.themis.domain.user.repo;

import com.achobeta.themis.domain.user.model.entity.QuestionDTO;

import java.util.List;

public interface ITestRepository {
     List<QuestionDTO> queryQuestions();

}
