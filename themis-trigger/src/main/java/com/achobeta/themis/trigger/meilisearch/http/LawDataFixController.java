package com.achobeta.themis.trigger.meilisearch.http;

import com.achobeta.themis.common.component.entity.LawDocument;
import com.achobeta.themis.common.util.IKPreprocessorUtil;
import com.achobeta.themis.domain.user.model.entity.LawCategory;
import com.achobeta.themis.domain.user.model.entity.LawRegulation;
import com.achobeta.themis.infrastructure.user.mapper.LawCategoryMapper;
import com.achobeta.themis.infrastructure.user.mapper.LawRegulationMapper;
import com.alibaba.fastjson.JSON;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.Settings;
import com.meilisearch.sdk.model.Task;
import com.meilisearch.sdk.model.TaskInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/law/fix")
@RequiredArgsConstructor
public class LawDataFixController {
    
    private final Client meiliSearchClient;
    private final LawCategoryMapper lawCategoryMapper;
    private final LawRegulationMapper lawRegulationMapper;
    
    /**
     * 检查索引配置
     */
    @GetMapping("/check-settings")
    public Map<String, Object> checkSettings() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Index index = meiliSearchClient.index("law_documents");
            Settings settings = index.getSettings();
            
            // 直接获取主键
            String primaryKey = index.getPrimaryKey();
            
            result.put("主键", primaryKey != null ? primaryKey : "未设置");
            result.put("可搜索字段", settings.getSearchableAttributes());
            result.put("可过滤字段", settings.getFilterableAttributes());
            result.put("可排序字段", settings.getSortableAttributes());
            result.put("状态", "成功");
            
        } catch (Exception e) {
            log.error("检查设置失败", e);
            result.put("状态", "失败");
            result.put("错误", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 重新创建索引（删除旧的，创建新的）
     */
    @PostMapping("/recreate-index")
    public Map<String, Object> recreateIndex() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 删除旧索引
            try {
                meiliSearchClient.deleteIndex("law_documents");
                Thread.sleep(1000); // 等待删除完成
                log.info("已删除旧索引");
            } catch (Exception e) {
                log.info("旧索引不存在或删除失败: {}", e.getMessage());
            }
            
            // 创建新索引，指定主键为 id
            TaskInfo createTask = meiliSearchClient.createIndex("law_documents", "id");
            meiliSearchClient.waitForTask(createTask.getTaskUid());
            log.info("已创建新索引，主键: id");
            
            // 配置索引
            Index index = meiliSearchClient.index("law_documents");
            Settings settings = new Settings();
            
            settings.setSearchableAttributes(new String[]{ "originalTextSegmented", "lawName" });
            settings.setFilterableAttributes(new String[]{ "lawCategoryId", "articleNumber", "issueYear" });
            settings.setSortableAttributes(new String[]{ "articleNumber" });
            
            TaskInfo settingsTask = index.updateSettings(settings);
            meiliSearchClient.waitForTask(settingsTask.getTaskUid());
            
            result.put("状态", "成功");
            result.put("消息", "索引已重新创建并配置完成");
            result.put("主键", "id");
            
        } catch (Exception e) {
            log.error("重新创建索引失败", e);
            result.put("状态", "失败");
            result.put("错误", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 使用正确的主键重新导入数据
     */
    @PostMapping("/reimport-with-fix")
    public Map<String, Object> reimportWithFix() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始重新导入数据...");
            
            // 1. 查询数据
            List<LawCategory> categories = lawCategoryMapper.selectList(null);
            List<LawRegulation> regulations = lawRegulationMapper.selectList(null);
            
            if (categories == null || categories.isEmpty() || regulations == null || regulations.isEmpty()) {
                result.put("状态", "失败");
                result.put("错误", "数据库中没有数据");
                return result;
            }
            
            // 2. 创建分类映射
            Map<Integer, LawCategory> categoryMap = new HashMap<>();
            for (LawCategory category : categories) {
                categoryMap.put(category.getLawId(), category);
            }
            
            // 3. 转换数据
            List<LawDocument> lawDocuments = new ArrayList<>();
            for (LawRegulation regulation : regulations) {
                LawCategory category = categoryMap.get(regulation.getLawCategoryId());
                if (category == null) {
                    continue;
                }
                
                String segmentedText = IKPreprocessorUtil.segment(regulation.getOriginalText(), true);
                
                LawDocument document = LawDocument.builder()
                        .id(regulation.getRegulationId())
                        .lawName(category.getLawName())
                        .lawCategoryId(regulation.getLawCategoryId())
                        .articleNumber(regulation.getArticleNumber())
                        .originalText(regulation.getOriginalText())
                        .originalTextSegmented(segmentedText)
                        .issueYear(regulation.getIssueYear())
                        .relatedRegulationIds(category.getRelatedRegulationIds())
                        .createTime(regulation.getCreateTime())
                        .build();
                
                lawDocuments.add(document);
            }
            
            log.info("准备导入 {} 条数据", lawDocuments.size());
            
            // 4. 批量添加到 Meilisearch
            Index index = meiliSearchClient.index("law_documents");
            String json = JSON.toJSONString(lawDocuments);
            
            TaskInfo taskInfo = index.addDocuments(json, "id"); // 明确指定主键
            log.info("文档已提交，taskUid={}", taskInfo.getTaskUid());
            
            meiliSearchClient.waitForTask(taskInfo.getTaskUid());
            
            Task task = meiliSearchClient.getTask(taskInfo.getTaskUid());
            log.info("任务完成: status={}", task.getStatus());
            
            if ("failed".equals(task.getStatus())) {
                var error = task.getError();
                result.put("状态", "失败");
                result.put("错误", error != null ? error.getMessage() : "未知错误");
                return result;
            }
            
            // 5. 验证结果
            var stats = index.getStats();
            result.put("状态", "成功");
            result.put("导入数量", lawDocuments.size());
            result.put("索引文档总数", stats.getNumberOfDocuments());
            
        } catch (Exception e) {
            log.error("重新导入失败", e);
            result.put("状态", "失败");
            result.put("错误", e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
