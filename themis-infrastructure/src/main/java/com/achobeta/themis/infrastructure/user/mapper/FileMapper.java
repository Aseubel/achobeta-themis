package com.achobeta.themis.infrastructure.user.mapper;

import com.achobeta.themis.domain.user.model.entity.FileRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<FileRecord> {
}
