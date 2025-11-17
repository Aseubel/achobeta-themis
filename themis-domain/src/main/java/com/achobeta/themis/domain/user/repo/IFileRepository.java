package com.achobeta.themis.domain.user.repo;

import com.achobeta.themis.domain.user.model.entity.FileRecord;

public interface IFileRepository {
    FileRecord findByConversationIdAndContentType(String conversationId, String contentType);

    void updateById(FileRecord fileRecord);

    void insert(FileRecord fileRecord);
}
