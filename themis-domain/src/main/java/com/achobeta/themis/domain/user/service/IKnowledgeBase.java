package com.achobeta.themis.domain.user.service;

import com.achobeta.themis.domain.user.model.vo.KnowledgeBaseQueryResponseVO;

import java.util.List;

public interface IKnowledgeBase {
     /**
      * 查询知识库问题
      * @param userQuestion 用户问题
      * @return 知识库问题响应VO
      */
    List<KnowledgeBaseQueryResponseVO> query(String userQuestion);
}
