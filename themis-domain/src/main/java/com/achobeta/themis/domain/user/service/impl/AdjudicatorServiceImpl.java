package com.achobeta.themis.domain.user.service.impl;

import com.achobeta.themis.common.component.MeiliSearchComponent;
import com.achobeta.themis.common.component.entity.QuestionTitleDocument;
import com.achobeta.themis.domain.user.service.IAdjudicatorService;
import com.achobeta.themis.common.agent.service.IAiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdjudicatorServiceImpl implements IAdjudicatorService {
    private final String QUESTION_TITLE_DOCUMENTS = "question_title_documents";

    private final MeiliSearchComponent meiliSearchComponent;

    @Autowired
    @Qualifier("adjudicator")
    private IAiChatService adjudicatorAgentService;

    @Override
    public void adjudicate(String userType, String conversationId, String question) {
        // 使用meiliSearch模糊查询问题分类
        QuestionTitleDocument questionTitleDocument = meiliSearchComponent.semanticSearchFromQuestionTitle(question);

        //如果查询到问题分类，判断问题分类的阈值是否在设定的阈值内
        if (questionTitleDocument != null) {
            //如果在，将count加1
            meiliSearchComponent.updateCount(QUESTION_TITLE_DOCUMENTS, questionTitleDocument.getId(), questionTitleDocument.getCount() + 1);
        } else {
            //如果不在，调用ai使用工具包将问题新增为一个新分类
            // TODO：查找问题上下文并打包为一个字符串
            String context = "";
            String chatContext = "用户类型" + userType + ";\n 当前问题：" + question + ";\n 讨论问题的上下文信息：" + context;
            adjudicatorAgentService.chat(conversationId, chatContext);
        }
    }
}
