package com.achobeta.themis.domain.user.service.impl;

import com.achobeta.themis.common.component.MeiliSearchComponent;
import com.achobeta.themis.common.component.entity.QuestionTitleDocument;
import com.achobeta.themis.domain.user.service.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {
    private final MeiliSearchComponent meiliSearchComponent;
    private final String QUESTION_TITLE_DOCUMENTS = "question_title_documents";

    @Override
    public List<List<QuestionTitleDocument>> searchQuestionTitles() {
        List<List<QuestionTitleDocument>> secondaryTitleDocuments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            secondaryTitleDocuments.add(meiliSearchComponent.searchFilteredAndSortedDocuments(QUESTION_TITLE_DOCUMENTS,
                    new String[]{"primaryTag=" + i},
                    new String[]{"count:desc"},
                    10,
                    QuestionTitleDocument.class));
        }
        return secondaryTitleDocuments;
    }
}
