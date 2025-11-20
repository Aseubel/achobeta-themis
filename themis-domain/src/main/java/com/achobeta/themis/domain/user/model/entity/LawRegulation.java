package com.achobeta.themis.domain.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("law_regulations")
public class LawRegulation {
    @TableId(value = "regulation_id", type = IdType.AUTO)
    private Integer regulationId;
    
    private Integer lawCategoryId;
    
    private String issueYear;
    
    private Integer articleNumber;
    
    private String originalText;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
