package com.achobeta.themis.domain.user.service;

import java.util.List;

public interface IFileReviewHistoryService {
    /**
     * 保存文件审查记录
     * @param userId 用户ID
     * @param reviewRecord 审查记录
     */
    void saveReviewRecord(Long userId, ReviewRecord reviewRecord);
    
    /**
     * 获取用户的所有文件审查记录
     * @param userId 用户ID
     * @return 审查记录列表
     */
    List<ReviewRecord> getUserReviewRecords(Long userId);
    
    /**
     * 获取指定的审查记录详情
     * @param userId 用户ID
     * @param recordId 记录ID
     * @return 审查记录
     */
    ReviewRecord getReviewRecord(Long userId, String recordId);
    
    /**
     * 删除审查记录
     * @param userId 用户ID
     * @param recordId 记录ID
     */
    void deleteReviewRecord(Long userId, String recordId);
    
    /**
     * 文件审查记录
     */
    class ReviewRecord {
        private String recordId;
        private String fileName;
        private String filePath;
        private String reviewContent;
        private Long createTime;
        private Long updateTime;
        
        public ReviewRecord() {}
        
        public ReviewRecord(String recordId, String fileName, String filePath, String reviewContent, Long createTime, Long updateTime) {
            this.recordId = recordId;
            this.fileName = fileName;
            this.filePath = filePath;
            this.reviewContent = reviewContent;
            this.createTime = createTime;
            this.updateTime = updateTime;
        }
        
        // Getters and Setters
        public String getRecordId() {
            return recordId;
        }
        
        public void setRecordId(String recordId) {
            this.recordId = recordId;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        
        public String getReviewContent() {
            return reviewContent;
        }
        
        public void setReviewContent(String reviewContent) {
            this.reviewContent = reviewContent;
        }
        
        public Long getCreateTime() {
            return createTime;
        }
        
        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }
        
        public Long getUpdateTime() {
            return updateTime;
        }
        
        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }
    }
}

