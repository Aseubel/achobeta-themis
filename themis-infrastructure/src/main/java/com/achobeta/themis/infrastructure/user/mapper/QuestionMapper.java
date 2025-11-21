package com.achobeta.themis.infrastructure.user.mapper;

import com.achobeta.themis.domain.user.model.entity.Questions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Questions> {
}
