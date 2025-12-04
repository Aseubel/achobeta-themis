package com.achobeta.themis.infrastructure.user.repo;

import com.achobeta.themis.common.component.MeiliSearchComponent;
import com.achobeta.themis.common.component.entity.LawDocument;
import com.achobeta.themis.common.util.IKPreprocessorUtil;
import com.achobeta.themis.domain.user.repo.IKnowledgeQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库查询Repository实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class KnowledgeQueryRepository implements IKnowledgeQueryRepository {
    
    private final MeiliSearchComponent meiliSearchComponent;
    
    @Override
    public List<LawDocument> searchLawDocuments(String query, Integer lawCategoryId, Integer limit) throws Exception {
        log.info("搜索法律文档，query={}, lawCategoryId={}, limit={}", query, lawCategoryId, limit);
        
        // 对查询进行分词
        String segmentedQuery = IKPreprocessorUtil.segment(query, true);
        log.info("分词后的查询: {}", segmentedQuery);
        
        // 调用MeiliSearch搜索
        List<LawDocument> documents = meiliSearchComponent.searchLawDocuments(
                segmentedQuery, 
                lawCategoryId, 
                limit != null && limit > 0 ? limit : 5,
                LawDocument.class
        );
        
        log.info("搜索到 {} 条法律文档", documents.size());
        return documents;
    }
}
