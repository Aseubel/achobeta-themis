package com.achobeta.themis.domain.user.service.impl;

import cn.hutool.json.JSONUtil;
import com.achobeta.themis.common.agent.service.IAiAdjudicatorService;
import com.achobeta.themis.common.component.MeiliSearchComponent;
import com.achobeta.themis.common.component.entity.KnowledgeBaseQuestionDocument;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.common.redis.service.IRedisService;
import com.achobeta.themis.common.util.IKPreprocessorUtil;
import com.achobeta.themis.domain.user.model.entity.KnowledgeBaseReviewDTO;
import com.achobeta.themis.domain.user.model.entity.QuestionRegulationRelations;
import com.achobeta.themis.domain.user.model.entity.Questions;
import com.achobeta.themis.domain.user.model.vo.KnowledgeBaseQueryResponseVO;
import com.achobeta.themis.domain.user.repo.IKnowledgeBaseRepository;
import com.achobeta.themis.domain.user.service.IKnowledgeBase;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.achobeta.themis.common.Constant.KNOWLEDGE_BASE_INSERT_SYSTEM_PROMPT;
import static com.achobeta.themis.common.Constant.KNOWLEDGE_BASE_QUESTION_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements IKnowledgeBase {
    private final IRedisService redissonService;

    private final MeiliSearchComponent meiliSearchComponent;

    private final IKnowledgeBaseRepository knowledgeBaseRepository;

    @Autowired
    @Qualifier("threadPoolExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    @Qualifier("adjudicator")
    private IAiAdjudicatorService adjudicatorAgentService;


    /**
     * 查询知识库问题
     * @param userQuestion 用户问题
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<KnowledgeBaseQueryResponseVO> query(String userQuestion) {
        Long questionId = null;
        // 先查MySQL的问题表
        Questions question = knowledgeBaseRepository.findQuestionByUserQuestionContent(userQuestion);
        if (question != null) {
            questionId = question.getId();
        } else {
            // 无结果时，meilisearch查问题
            List<KnowledgeBaseQuestionDocument> questionDocuments = null;
            try {
                questionDocuments = meiliSearchComponent.fuzzySearchFromQuestionTitle(KNOWLEDGE_BASE_QUESTION_DOCUMENTS, IKPreprocessorUtil.segment(userQuestion, true), new String[]{"question_segmented"}, 1, KnowledgeBaseQuestionDocument.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!questionDocuments.isEmpty()) {
                // 有数据
                KnowledgeBaseQuestionDocument questionDocument = questionDocuments.getFirst();
                meiliSearchComponent.updateCount(KNOWLEDGE_BASE_QUESTION_DOCUMENTS, questionDocument.getId(), questionDocument.getCount() + 1);
                questionId = questionDocument.getQuestionId();
            } else {
                // 无结果时，询问ai并插入数据 #TODO
                // TODO meilisearch 查询相关连的法条原文及其id （控制数量）
                // List<LawCategories> lawCategories = meiliSearchComponent.fuzzySearchFromLawCategories(KNOWLEDGE_BASE_LAW_CATEGORIES, IKPreprocessorUtil.segment(userQuestion, true), new String[]{"law_name"}, 1, LawCategories.class);
                // TODO 将关联的法条id和原文一起写到提示词中 ,人ai返回下方json
                String prompt = buildAdjudicatePrompt(userQuestion, KNOWLEDGE_BASE_INSERT_SYSTEM_PROMPT);
                // TODO 系统提示词要改
                String response = adjudicatorAgentService.chat(UUID.randomUUID().toString(), prompt);
                // 异步插入数据
                threadPoolTaskExecutor.execute(() -> {
                    insertKnowledgeBaseData(userQuestion, response);
                });
                // 根据ai的信息封装返回结果
                return parseJsonAndSearchDataToKnowledgeBaseQueryResponseVO(response, userQuestion);
            }
        }
        List<Long> regulationIds = knowledgeBaseRepository.findRegulationIdsByQuestionId(questionId);
        Long questionIdForSearch = questionId;
        return regulationIds.stream().map(regulationId ->{
            KnowledgeBaseReviewDTO knowledgeBaseReviewDTO = knowledgeBaseRepository.findKnowledgeBaseReviewDetailsById(regulationId, questionIdForSearch);
                        return knowledgeBaseBaseReviewToKnowledgeBaseQueryResponseVO(knowledgeBaseReviewDTO);
        }).collect(Collectors.toList());
    }

    /**
     * 查找topic
     * @return 所有topic列表
     */
    @Override
    public List<String> queryTopics() {
        List<KnowledgeBaseQuestionDocument> documents = meiliSearchComponent.searchFilteredAndSortedDocuments(KNOWLEDGE_BASE_QUESTION_DOCUMENTS,
                null,
                new String[]{"topic"},
                10,
                KnowledgeBaseQuestionDocument.class);
        return documents.stream().map(KnowledgeBaseQuestionDocument::getTopic).collect(Collectors.toList());
    }

    /**
     * 查询常见场景
     * @return 所有常见场景列表
     */
    @Override
    public List<String> queryCaseBackgrounds() {
        List<KnowledgeBaseQuestionDocument> documents = meiliSearchComponent.searchFilteredAndSortedDocuments(KNOWLEDGE_BASE_QUESTION_DOCUMENTS,
                null,
                new String[]{"case_background"},
                10,
                KnowledgeBaseQuestionDocument.class);
        return documents.stream().map(KnowledgeBaseQuestionDocument::getCaseBackground).collect(Collectors.toList());
    }

    /**
     * 异步插入知识库数据
     * @param userQuestion
     * @param response
     */
    @Transactional(rollbackFor = Exception.class)
    protected void insertKnowledgeBaseData(String userQuestion, String response) {
        // 解析数据到Questions表
        Questions questions = parseJsonToQuestions(response, userQuestion);
        Long questionId = knowledgeBaseRepository.saveQuestions(questions);
        // 异步解析数据到Questions法律关联表
        List<QuestionRegulationRelations> questionRegulationRelations = parseJsonToQuestionRegulationRelations(response, questionId);
        if (questionRegulationRelations != null)
            questionRegulationRelations.forEach(knowledgeBaseRepository::saveQuestionRegulationRelations);
        else {
            log.info("问题{}未关联任何法律", questionId);
            throw new BusinessException("问题未关联任何法律");
        }
        // 异步解析数据到KnowledgeBaseQuestionDocument表
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                KnowledgeBaseQuestionDocument knowledgeBaseQuestionDocuments = parseJsonToKnowledgeBaseQuestionDocuments(response, questionId, userQuestion);
                if (knowledgeBaseQuestionDocuments != null) {
                    // 事务提交成功后，执行MeiliSearch操作
                    meiliSearchComponent.addDocuments(KNOWLEDGE_BASE_QUESTION_DOCUMENTS,
                            List.of(knowledgeBaseQuestionDocuments));
                }
            }
        });
    }

    /**
     * 解析json字符串到知识库查询响应VO
     * @param JSONStr
     * @param userQuestion
     * @return
     */
    private List<KnowledgeBaseQueryResponseVO> parseJsonAndSearchDataToKnowledgeBaseQueryResponseVO(String JSONStr, String userQuestion) {
        JSONObject jsonObject = JSONObject.parseObject(JSONStr);
        JSONArray regulationArray = jsonObject.getJSONArray("regulationList");
        if (regulationArray == null || regulationArray.isEmpty()) {
            log.info("问题{}未关联任何法律", userQuestion);
            return null;
        }
        return regulationArray.stream()
                .map(regulation -> JSONObject.parseObject(regulation.toString()))
                .map(regulation -> {
                    Long regulationID = regulation.getLong("regulationID");
                    // 根据法律id查询法律名称、法律内容、条款号、总条款数、发布年份
                    KnowledgeBaseReviewDTO knowledgeBaseReviewDTO = knowledgeBaseRepository.findKnowledgeBaseReviewDetailsById(regulationID, null);
                    return KnowledgeBaseQueryResponseVO.builder()
                            .lawName(knowledgeBaseReviewDTO.getLawName())
                            .regulationContent(knowledgeBaseReviewDTO.getOriginalText())
                            .aiTranslateContent(regulation.getString("aiTranslation"))
                            .relevantCases(regulation.getJSONArray("relevantCases").toList(KnowledgeBaseQueryResponseVO.RelevantCases.class))
                            .relevantQuestions(regulation.getJSONArray("relevantQuestions").toList(String.class))
                            .articleNumber(knowledgeBaseReviewDTO.getArticleNumber())
                            .totalArticles(knowledgeBaseReviewDTO.getTotalArticles())
                            .issueYear(knowledgeBaseReviewDTO.getIssueYear())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 将知识库审核实体转换为知识库查询响应VO
     * @param knowledgeBaseReviewDTO
     * @return
     */
    private KnowledgeBaseQueryResponseVO knowledgeBaseBaseReviewToKnowledgeBaseQueryResponseVO(KnowledgeBaseReviewDTO knowledgeBaseReviewDTO) {
        return KnowledgeBaseQueryResponseVO.builder()
                .lawName(knowledgeBaseReviewDTO.getLawName())
                .regulationContent(knowledgeBaseReviewDTO.getOriginalText())
                .aiTranslateContent(knowledgeBaseReviewDTO.getAiTranslation())
                .relevantCases(JSONUtil.toList(knowledgeBaseReviewDTO.getRelevantCases(), KnowledgeBaseQueryResponseVO.RelevantCases.class))
                .relevantQuestions(JSONUtil.toList(knowledgeBaseReviewDTO.getRelevantQuestions(), String.class))
                .articleNumber(knowledgeBaseReviewDTO.getArticleNumber())
                .totalArticles(knowledgeBaseReviewDTO.getTotalArticles())
                .issueYear(knowledgeBaseReviewDTO.getIssueYear())
                .build();
    }

    /** 期望ai返回的json字符串格式
     * {
     *      "topic": "",
     *      "caseBackground": "",
     *      "regulationList": [
     *          {
     *              "regulationID": 1,
     *              "aiTranslation": "",
     *              "relevantCases": [
     *                  {
     *                      "caseContent": "",
     *                      "caseLink": ""
     *                  },
     *                  {
     *                      "caseContent": "",
     *                      "caseLink": ""
     *                  }
     *              ],
     *              "relevantQuestions": [
     *                  "相关问题1",
     *                  "相关问题2"
     *              ]
     *          },
     *          {
     *              "regulationID": 2,
     *              "aiTranslation": "",
     *              "relevantCases": [
     *                    {
     *                        "caseContent": "",
     *                        "caseLink": ""
     *                    }
     *                ],
     *              "relevantQuestions": [
     *                   "相关问题1",
     *                   "相关问题2"
     *              ]
     *          }
     *      ]
     *
     * }
     */

    /**
     * 构建审核提示
     * @param userQuestion
     * @param systemPrompt
     * @return
     */
    private String buildAdjudicatePrompt(String userQuestion, String systemPrompt) {
        return String.format("%s\n用户问题：%s", systemPrompt, userQuestion);
    }

    /**
     * 解析json字符串到问题实体
     * @param JSONStr
     * @param userQuestion
     * @return
     */
    private Questions parseJsonToQuestions(String JSONStr, String userQuestion) {
        // 从json解析topic，caseBackground
        JSONObject jsonObject = JSONObject.parseObject(JSONStr);
        String topic = jsonObject.getString("topic");
        String caseBackground = jsonObject.getString("caseBackground");
        return Questions.builder()
                .questionContent(userQuestion)
                .topic(topic)
                .caseBackground(caseBackground)
                .build();
    }

    /**
     * 解析json字符串到知识库问题文档
     * @param JSONStr
     * @param questionId
     * @param userQuestion
     * @return
     */
    private KnowledgeBaseQuestionDocument parseJsonToKnowledgeBaseQuestionDocuments(String JSONStr, Long questionId, String userQuestion) {
        JSONObject jsonObject = JSONObject.parseObject(JSONStr);
        String questionSegmented = null;
        try {
            questionSegmented = IKPreprocessorUtil.segment(userQuestion, true);
        } catch (Exception e) {
            log.error("问题{}分词失败", questionId, e);
            throw new RuntimeException(e);
        }
        return KnowledgeBaseQuestionDocument.builder()
                .id(UUID.randomUUID().toString())
                .questionId(questionId)
                .question(userQuestion)
                .questionSegmented(questionSegmented)
                .topic(jsonObject.getString("topic"))
                .caseBackground(jsonObject.getString("caseBackground"))
                .count(1)
                .build();
    }

    /**
     * 解析json字符串到问题法规关联关系列表
     * @param JSONStr
     * @param questionId
     * @return
     */
    private List<QuestionRegulationRelations> parseJsonToQuestionRegulationRelations(String JSONStr, Long questionId) {
        JSONObject jsonObject = JSONObject.parseObject(JSONStr);
        JSONArray regulationArray = jsonObject.getJSONArray("regulationList");
        if (regulationArray == null || regulationArray.isEmpty()) {
            log.info("问题{}未关联任何法律", questionId);
            return null;
        }
        return regulationArray.stream()
                .map(obj -> {
                    JSONObject regulation = (JSONObject) obj;
                    return QuestionRegulationRelations.builder()
                            .questionId(questionId)
                            .regulationId(regulation.getLong("regulationID"))
                            .aiTranslation(regulation.getString("aiTranslation"))
                            .relevantCases(regulation.getJSONArray("relevantCases").toJSONString())
                            .relevantQuestions(regulation.getJSONArray("relevantQuestions").toJSONString())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

