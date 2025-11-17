package com.achobeta.themis.domain.user.model.entity;

import com.achobeta.themis.common.annotation.FieldDesc;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("questions")
public class QuestionDTO {
    @FieldDesc(name = "问题ID")
    @TableId(value = "question_id", type = IdType.AUTO)
    private int questionId;
    @FieldDesc(name = "问题内容")
    private String questionContent;
    @FieldDesc(name = "创建时间")
    private LocalDateTime createTime;
    @FieldDesc(name = "更新时间")
    private LocalDateTime updateTime;
}