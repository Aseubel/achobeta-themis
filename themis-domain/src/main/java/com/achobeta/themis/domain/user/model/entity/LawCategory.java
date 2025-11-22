package com.achobeta.themis.domain.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "law_categories", autoResultMap = true)
public class LawCategory {
    @TableId(value = "law_id", type = IdType.AUTO)
    private Integer lawId;
    
    private String lawName;
    
    @TableField("category_type")
    private Integer categoryType;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> relatedRegulationIds;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
