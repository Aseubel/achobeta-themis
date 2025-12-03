package com.achobeta.themis.domain.user.service;

public interface ILawDataImportService {
    /**
     * 从数据库导入法律数据到 Meilisearch
     * @return 导入的文档数量
     */
    int importLawDataFromDatabase() throws Exception;
}
