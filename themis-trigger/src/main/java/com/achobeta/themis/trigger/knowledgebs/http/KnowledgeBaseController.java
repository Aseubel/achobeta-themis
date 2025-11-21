package com.achobeta.themis.trigger.knowledgebs.http;

import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.annotation.LoginRequired;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.domain.user.model.vo.KnowledgeBaseQueryResponseVO;
import com.achobeta.themis.domain.user.service.IKnowledgeBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@LoginRequired
@Slf4j
@Validated
@RestController
@RequestMapping("/api/knowledgebs")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final IKnowledgeBase knowledgeBaseService;

    /**
     * 搜索问题
     * @param question
     * @return 知识库问题响应VO列表
     */
    @LoginRequired
    @GetMapping("/query")
    public ApiResponse<List<KnowledgeBaseQueryResponseVO>> queryKnowledgeBase(@RequestParam("question") String question) {
        log.info("queryKnowledgeBase, question: {}", question);
        try {
            return ApiResponse.success(knowledgeBaseService.query(question));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("知识库查询失败", e);
            throw e;
        }
    }

    /**
     * 查找topic
     * @return 所有topic列表
     */
    @GetMapping("/topic")
    public ApiResponse<List<String>> queryTopics() {
        log.info("queryTopics");
        try {
            return ApiResponse.success(knowledgeBaseService.queryTopics());
        } catch (Exception e) {
            log.error("查询topic失败", e);
            throw e;
        }
    }


    /**
     * 查询常见场景
     * @return 所有常见场景列表
     */
     @GetMapping("/case")
    public ApiResponse<List<String>> queryCaseBackgrounds() {
        log.info("queryCaseBackgrounds");
        try {
            return ApiResponse.success(knowledgeBaseService.queryCaseBackgrounds());
        } catch (Exception e) {
            log.error("查询常见场景失败", e);
            throw e;
        }
    }

    /**
     * 查找用户搜索历史记录
     * @return 用户搜索历史记录列表
     */
    @LoginRequired
    @GetMapping("/history")
    public ApiResponse<List<String>> querySearchHistory() {
        log.info("querySearchHistory");
        try {
            return ApiResponse.success(knowledgeBaseService.querySearchHistory());
        } catch (Exception e) {
            log.error("查询用户搜索历史记录失败", e);
            throw e;
        }
    }



}
