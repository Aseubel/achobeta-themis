package com.achobeta.themis.domain.user.service.impl;

import com.achobeta.themis.common.agent.service.IAiAdjudicatorService;
import com.achobeta.themis.common.component.MeiliSearchComponent;
import com.achobeta.themis.common.component.entity.QuestionTitleDocument;
import com.achobeta.themis.common.util.IKPreprocessor;
import com.achobeta.themis.domain.user.service.IAdjudicatorService;
import com.achobeta.themis.common.agent.service.IAiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdjudicatorServiceImpl implements IAdjudicatorService {
    private final String QUESTION_TITLE_DOCUMENTS = "question_title_documents";

    private final MeiliSearchComponent meiliSearchComponent;

    @Autowired
    @Qualifier("adjudicator")
    private IAiAdjudicatorService adjudicatorAgentService;

    @Override
    public void adjudicate(Integer userType, String conversationId, String question) {
        List<QuestionTitleDocument> questionTitleDocuments = null;
        try {
            questionTitleDocuments = meiliSearchComponent.fuzzySearchFromQuestionTitle(QUESTION_TITLE_DOCUMENTS, IKPreprocessor.segment(question, true), new String[]{"title_segmented"}, 1, QuestionTitleDocument.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!questionTitleDocuments.isEmpty()) {
            QuestionTitleDocument questionTitleDocument = questionTitleDocuments.getFirst();
            meiliSearchComponent.updateCount(QUESTION_TITLE_DOCUMENTS, questionTitleDocument.getId(), questionTitleDocument.getCount() + 1);
        } else {
            // TODO: ai 版本有问题，后期再优化
            // 查找问题上下文并打包为一个字符串
//            String context = "";
//            String chatContext = "用户类型" + userType + ";\n 当前问题：" + question + ";\n 讨论问题的上下文信息：" + context;
//            String adjudicate = adjudicatorAgentService.chat("adjudicate_" + conversationId, chatContext);
//            System.out.println(adjudicate);
            String adjudicate = adjudicatorAgentService.chat("adjudicate_" + conversationId, "UserType" + userType + ";\n 当前问题：" + question);
            // 提取 primaryTag
            int primaryTag = Integer.parseInt(adjudicate.replaceAll("\\D+", ""));
            try {
                meiliSearchComponent.addDocuments(QUESTION_TITLE_DOCUMENTS, List.of(QuestionTitleDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .title(question)
                        .titleSegmented(IKPreprocessor.segment(question, true))
                        .primaryTag(primaryTag)
                        .count(1)
                        .createTime(LocalDateTime.now())
                        .build()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

