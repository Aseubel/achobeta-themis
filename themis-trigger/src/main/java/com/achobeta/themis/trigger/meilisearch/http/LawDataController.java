package com.achobeta.themis.trigger.meilisearch.http;

import com.achobeta.themis.domain.user.service.ILawDataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/law")
@RequiredArgsConstructor
public class LawDataController {
    
    private final ILawDataImportService lawDataImportService;
    
    /**
     * 手动触发从数据库导入法律数据到 Meilisearch
     * @return 导入的文档数量
     */
    @PostMapping("/import")
    public String importLawData() {
        try {
            int count = lawDataImportService.importLawDataFromDatabase();
            return "成功导入 " + count + " 条法律数据到 Meilisearch";
        } catch (Exception e) {
            log.error("导入法律数据失败", e);
            return "导入失败: " + e.getMessage();
        }
    }
}
