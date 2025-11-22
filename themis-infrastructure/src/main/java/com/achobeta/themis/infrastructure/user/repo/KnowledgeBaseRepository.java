package com.achobeta.themis.infrastructure.user.repo;


import com.achobeta.themis.domain.user.model.entity.*;
import com.achobeta.themis.domain.user.repo.IKnowledgeBaseRepository;
import com.achobeta.themis.infrastructure.user.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class KnowledgeBaseRepository implements IKnowledgeBaseRepository {
    private final QuestionMapper questionMapper;
    private final QuestionRegulationRelationsMapper questionRegulationRelationsMapper;
    private final LawRegulationsMapper lawRegulationsMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final LawCategoriesMapper lawCategoriesMapper;
    private final SearchHistoryMapper searchHistoryMapper;



    /**
     * 根据用户问题内容查询问题
     * @param userQuestionContent
     * @return
     */
    @Override
    public Questions findQuestionByUserQuestionContent(String userQuestionContent) {
        return questionMapper.selectOne(new LambdaQueryWrapper<Questions>()
                .like(Questions::getQuestionContent, userQuestionContent));
    }

    /**
     * 保存问题
     * @param questions
     * @return
     */
    @Override
    public Long saveQuestions(Questions questions) {
        questionMapper.insert(questions);
        return questions.getId();
    }

    /**
     * 保存问题法规关联关系
     * @param questionRegulationRelations
     */
    @Override
    public void saveQuestionRegulationRelations(QuestionRegulationRelations questionRegulationRelations) {
        questionRegulationRelationsMapper.insert(questionRegulationRelations);
    }

    /**
     * 根据法规ID和问题ID查询知识库审核详情
     * @param regulationID
     * @param userQuestionId
     * @return
     */
    @Override
    public KnowledgeBaseReviewDTO findKnowledgeBaseReviewDetailsById(Long regulationID, Long userQuestionId) {
        KnowledgeBaseReviewDTO knowledgeBaseReviewDTO = new KnowledgeBaseReviewDTO();
        LawRegulations lawRegulations = lawRegulationsMapper.selectOne(new LambdaQueryWrapper<LawRegulations>()
                .eq(LawRegulations::getRegulationId, regulationID));
        LawCategories lawCategories = lawCategoriesMapper.selectOne(new LambdaQueryWrapper<LawCategories>()
                .eq(LawCategories::getLawId, lawRegulations.getLawCategoryId()));
        QuestionRegulationRelations questionRegulationRelations = questionRegulationRelationsMapper.selectOne(new LambdaQueryWrapper<QuestionRegulationRelations>()
                .eq(QuestionRegulationRelations::getQuestionId, userQuestionId)
                .eq(QuestionRegulationRelations::getRegulationId, regulationID));
        knowledgeBaseReviewDTO.setLawName(lawCategories.getLawName())
                .setOriginalText(lawRegulations.getOriginalText())
                .setArticleNumber(lawRegulations.getArticleNumber())
                .setTotalArticles(lawCategories.getRelatedRegulationIds().size())
                .setIssueYear(lawRegulations.getIssueYear());
        if (userQuestionId != null) {
            knowledgeBaseReviewDTO.setAiTranslation(questionRegulationRelations.getAiTranslation())
                    .setRelevantCases(questionRegulationRelations.getRelevantCases())
                    .setRelevantQuestions(questionRegulationRelations.getRelevantQuestions());
        }
        return knowledgeBaseReviewDTO;
    }

    /**
     * 根据问题ID查询关联的法规ID列表
     * @param questionId
     * @return
     */
    @Override
    public List<Long> findRegulationIdsByQuestionId(Long questionId) {
        return questionRegulationRelationsMapper.selectList(new LambdaQueryWrapper<QuestionRegulationRelations>()
                .eq(QuestionRegulationRelations::getQuestionId, questionId))
                .stream()
                .map(QuestionRegulationRelations::getRegulationId)
                .toList();
    }

    @Override
    public void saveSearchHistory(String userQuestion, Long userId) {
        searchHistoryMapper.insert(KnowledgeBaseSearchHistory.builder()
                .userId(userId)
                .userQuestion(userQuestion)
                .build()
        );

    }

    @Override
    public List<String> findSearchHistoryByUserId(Long currentUserId, int limit) {
        return searchHistoryMapper.selectList(new LambdaQueryWrapper<KnowledgeBaseSearchHistory>()
                .eq(KnowledgeBaseSearchHistory::getUserId, currentUserId)
                .orderByDesc(KnowledgeBaseSearchHistory::getCreateTime)
                .apply("LIMIT {0}", limit))
                .stream()
                .map(KnowledgeBaseSearchHistory::getUserQuestion)
                .toList();
    }
}
