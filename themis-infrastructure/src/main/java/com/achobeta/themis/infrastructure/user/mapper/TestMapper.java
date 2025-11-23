package com.achobeta.themis.infrastructure.user.mapper;

import com.achobeta.themis.domain.user.model.entity.Questions;
import com.achobeta.themis.domain.user.model.entity.QuestionsForDataInserted;
import com.achobeta.themis.domain.user.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper extends BaseMapper<QuestionsForDataInserted> {
}