package com.achobeta.themis.domain.user.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFileService {
    /**
     * 生成文件下载url
     * @param isChange
     * @param conversationId
     * @param contentType
     * @param content
     * @return
     */
     Resource generateDownloadResource(Boolean isChange, String conversationId, String contentType, String content);

    String contentTransform(String content);
}
