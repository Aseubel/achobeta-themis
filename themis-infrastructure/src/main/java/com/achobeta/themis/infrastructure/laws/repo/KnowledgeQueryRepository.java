package com.achobeta.themis.infrastructure.laws.repo;

import com.achobeta.themis.common.component.MeiliSearchComponent;
import com.achobeta.themis.common.component.entity.LawDocument;
import com.achobeta.themis.common.util.IKPreprocessorUtil;
import com.achobeta.themis.domain.laws.model.entity.LawCategory;
import com.achobeta.themis.domain.laws.model.entity.LawRegulation;
import com.achobeta.themis.domain.laws.repo.IKnowledgeQueryRepository;
import com.achobeta.themis.domain.laws.repo.ILawCategoryRepository;
import com.achobeta.themis.domain.laws.repo.ILawRegulationRepository;
import com.achobeta.themis.infrastructure.laws.mapper.LawCategoryMapper;
import com.achobeta.themis.infrastructure.laws.mapper.LawRegulationMapper;
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
public class KnowledgeQueryRepository implements IKnowledgeQueryRepository, ILawCategoryRepository, ILawRegulationRepository {
    
    private final MeiliSearchComponent meiliSearchComponent;
    private final LawCategoryMapper lawCategoryMapper;
    private final LawRegulationMapper lawRegulationMapper;
    
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

    @Override
    public List<LawCategory> listLawCategories() {
        return lawCategoryMapper.selectList(null);
    }

    @Override
    public LawCategory getById(Long id) {
        return null;
    }

    @Override
    public void save(LawCategory lawCategory) {

    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public List<LawRegulation> listLawRegulations() {
        return lawRegulationMapper.selectList(null);
    }
}
