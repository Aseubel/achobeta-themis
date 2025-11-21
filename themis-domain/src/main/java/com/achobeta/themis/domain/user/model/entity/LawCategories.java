package com.achobeta.themis.domain.user.model.entity;

import com.achobeta.themis.common.annotation.FieldDesc;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("law_categories")
public class LawCategories {
    @TableId(value = "law_id", type = IdType.AUTO)
    @FieldDesc(name = "法律分类ID")
    private Long lawId;
    @FieldDesc(name = "法律分类名称")
    private String lawName;
    @FieldDesc(name = "关联法条ID列表")
    private String relatedRegulationIds;
    @FieldDesc(name = "创建时间")
    private LocalDateTime createTime;
    @FieldDesc(name = "更新时间")
    private LocalDateTime updateTime;
}