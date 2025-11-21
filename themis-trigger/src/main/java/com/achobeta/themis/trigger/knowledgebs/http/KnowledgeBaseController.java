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
     * 查询知识库问题
     * @param question
     * @return
     */
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



}
