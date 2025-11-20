package com.achobeta.themis.domain.user.service;

import com.achobeta.themis.domain.user.model.vo.KnowledgeQueryRequestVO;
import com.achobeta.themis.domain.user.model.vo.KnowledgeQueryResponseVO;

/**
 * 知识库查询Service接口
 */
public interface IKnowledgeQueryService {
    
    /**
     * 查询知识库并返回相关法律文档和AI解析
     * @param userId 用户ID
     * @param request 查询请求
     * @return 查询响应
     */
    KnowledgeQueryResponseVO queryKnowledge(String userId, KnowledgeQueryRequestVO request) throws Exception;
}
