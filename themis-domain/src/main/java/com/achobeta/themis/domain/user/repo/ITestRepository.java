package com.achobeta.themis.domain.user.repo;

import com.achobeta.themis.domain.user.model.entity.Questions;

import java.util.List;

public interface ITestRepository {
     List<Questions> queryQuestions();

}
