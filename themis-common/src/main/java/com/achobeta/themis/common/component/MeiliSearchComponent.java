package com.achobeta.themis.common.component;

import com.achobeta.themis.common.component.entity.QuestionTitleDocument;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.Hybrid;
import com.meilisearch.sdk.model.Results;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.Settings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("meiliSearchUtils")
@Slf4j
public class MeiliSearchComponent implements CommandLineRunner {

    @Autowired
    private Client meiliSearchClient;

    private final String QUESTION_TITLE_DOCUMENTS = "question_title_documents";


    /**
     * 向指定索引添加文档列表
     * @param indexName 索引名称
     * @param docs 要添加的文档列表
     * @param <T> 文档类型，必须为 QuestionTitleDocument
     * @throws MeilisearchException 如果添加过程中发生错误
     */
    public <T> void addDocuments(String indexName, List<T> docs) throws MeilisearchException {
        if (docs == null || docs.isEmpty()) {
            log.info("没有要写入索引 {} 的文档", indexName);
            return;
        }
        try {
            Index index = meiliSearchClient.index(indexName);
            String json = JSON.toJSONString(docs);
            index.addDocuments(json);
            log.info("已向索引 {} 添加 {} 条文档", indexName, docs.size());
        }
        catch (MeilisearchException e) {
            log.error("向索引 {} 添加文档失败", indexName, e);
            throw e;
        }
    }


    /**
     * 根据文档字段撒选文档，并排序返回符合条件的文档列表
     * @param indexName 索引名称
     * @param filter 筛选条件，例如 "primaryTag = 1"
     * @param sort 排序字段，例如 "count:desc"
     * @param limit 限制返回的文档数量，例如 10
     * @return 符合条件的文档列表
     * @throws MeilisearchException 如果查询过程中发生错误
     */
    public <T> List<T> searchFilteredAndSortedDocuments(String indexName, String[] filter, String[] sort, Integer limit, Class<T> clazz) throws MeilisearchException {
        if (indexName == null || indexName.trim().isEmpty()) {
            throw new IllegalArgumentException("索引名不能为 null 或空字符串");
        }

        try {
            Index index = meiliSearchClient.index(indexName);

            // 使用空查询，仅依赖 filter 与 sort
            SearchRequest req = new SearchRequest("");
            if (filter != null && filter.length > 0) {
                req.setFilter(filter);
            }
            if (sort != null && sort.length > 0) {
                req.setSort(sort);
            }
            // 设置默认 limit 为 100
            int limitInt = 100;
            if (limit != null && limit > 0) {
                limitInt = limit;
            }
            req.setLimit(limitInt);

            SearchResult raw = (SearchResult) index.search(req);
            if (raw == null || raw.getHits() == null || raw.getHits().isEmpty()) {
                return Collections.emptyList();
            }

            // 将 hits 转为 JSON 再反序列化为 List<T>，调用方可自行转换为具体类型
            String json = JSON.toJSONString(raw.getHits());
            return JSON.parseArray(json, clazz);
        }
        catch (MeilisearchException e) {
            log.error("查询索引 {} 失败，filter={}, sort={}, limit={}", indexName, filter, sort, limit, e);
            throw e;
        }
    }

    /**
     * 更新文档的 count 字段
     * @param indexName 索引名称
     * @param id 文档ID
     * @param count 新的 count 值
     * @throws MeilisearchException 如果更新过程中发生错误
     */
    public void updateCount(String indexName, Object id, Number count) throws MeilisearchException {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为 null");
        }
        if (count == null) {
            throw new IllegalArgumentException("count 不能为 null");
        }
        if (indexName == null || indexName.trim().isEmpty()) {
            throw new IllegalArgumentException("索引名不能为 null 或空字符串");
        }

        Map<String, Object> updateDoc = new HashMap<>(2);
        updateDoc.put("id", id);
        updateDoc.put("count", count);
        String updateDocJson = JSON.toJSONString(updateDoc);
        Index index = meiliSearchClient.index(indexName);
        index.updateDocuments(updateDocJson, "id");
    }

    /**
     * 执行语义搜索（混合关键词+向量搜索）
     * @param query 搜索关键词
     * @return 符合条件的文档列表
     */
    public QuestionTitleDocument semanticSearchFromQuestionTitle(String query) throws MeilisearchException {
        Index index = meiliSearchClient.index(QUESTION_TITLE_DOCUMENTS);

        SearchRequest req = new SearchRequest(query);
        /*Hybrid hybrid = Hybrid.builder()
                .semanticRatio(0.9)
                .embedder("default")
                .build();
        req.setHybrid(hybrid);*/

        SearchResult raw = (SearchResult) index.search(req);
        if (raw == null || raw.getHits() == null || raw.getHits().isEmpty()) {
            return null;
        }
        Object first = raw.getHits().get(0);
        String jsonFirst = JSON.toJSONString(first);
        return JSON.parseObject(jsonFirst, QuestionTitleDocument.class);
    }

    /**
     * 检查索引是否存在
     * @param indexName 索引名称
     * @return 如果索引存在则返回true，否则返回false
     * @throws MeilisearchException 如果检查过程中发生错误
     */
    public boolean isIndexExists(String indexName) throws MeilisearchException {
        Results<Index> indexes = meiliSearchClient.getIndexes(); // 获取所有索引
        if (indexes == null || indexes.getResults() == null) {
            return false;
        }
        for (Index index : indexes.getResults()) {
            if (index.getUid().equals(indexName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化问题排名索引
     * 配置索引字段为可搜索、可过滤、可排序
     * @throws IOException 如果初始化过程中发生IO错误
     */
    public void initQuestionRankingIndex() throws IOException {
        try {
            if (isIndexExists(QUESTION_TITLE_DOCUMENTS)) {
                log.info("索引 {} 已存在，无需重复创建", QUESTION_TITLE_DOCUMENTS);
                return;
            }
        }
        catch (MeilisearchException e) {
            throw new RuntimeException(e);
        }
        try {
            Index index = meiliSearchClient.index(QUESTION_TITLE_DOCUMENTS);
            Settings settings = new Settings();
            settings.setSearchableAttributes(new String[]{ "title" });
            settings.setFilterableAttributes(new String[]{ "primaryTag", "count", "create_time" });
            settings.setSortableAttributes(new String[]{ "count", "create_time" });
            index.updateSettings(settings);
            log.info("索引 {} 创建并配置成功", QUESTION_TITLE_DOCUMENTS);
        }
        catch (MeilisearchException e) {
            log.error("创建索引失败", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        initQuestionRankingIndex();
    }
}