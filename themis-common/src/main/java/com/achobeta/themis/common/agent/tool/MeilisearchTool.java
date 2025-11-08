package com.achobeta.themis.common.agent.tool;

import com.achobeta.themis.common.component.MeiliSearchComponent;
import com.achobeta.themis.common.component.entity.QuestionTitleDocument;
import com.meilisearch.sdk.Client;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Component
public class MeilisearchTool {
    @Autowired
    @Qualifier("meiliSearchUtils")
    private MeiliSearchComponent meiliSearchUtils;
    private final String QUESTION_TITLE_DOCUMENTS = "question_title_documents";

    @Tool(value = "添加文档到Meilisearch索引，用于存储问题标题文档。" +
            "- title：字符串类型，提炼出的当前用户问题的问题标签；" +
            "- primaryTag：整数类型，问题分类标签（父标签），范围1-15；" +
            "缺失任何参数或格式错误会导致添加失败，请务必检查。")
    public void addDocument(@P("问题标签（提炼出的当前用户问题的问题标签）") String title,
                            @P("问题分类标签（父标签）") int primaryTag) {
        QuestionTitleDocument document = QuestionTitleDocument.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .primaryTag(primaryTag)
                .count(1)
                .createTime(LocalDateTime.now())
                .build();
        meiliSearchUtils.addDocuments(QUESTION_TITLE_DOCUMENTS, Collections.singletonList(document));
    }
}
