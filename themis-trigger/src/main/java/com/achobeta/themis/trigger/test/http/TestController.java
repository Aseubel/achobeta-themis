package com.achobeta.themis.trigger.test.http;

import com.achobeta.themis.api.user.client.UserClient;
import com.achobeta.themis.api.user.response.UserInfoResponse;
import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.domain.user.model.entity.QuestionDTO;
import com.achobeta.themis.domain.user.model.vo.AuthResponseVO;
import com.achobeta.themis.domain.user.model.vo.LoginRequestVO;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Aseubel
 * @date 2025/10/28 下午9:03
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final UserClient userClient;
    private final com.achobeta.themis.domain.user.service.IUserService userService;
    private final com.achobeta.themis.domain.user.service.ITestService testService;
    private final com.achobeta.themis.domain.user.service.IAdjudicatorService adjudicatorService;

    @GetMapping("/getUserInfo")
    public ApiResponse<UserInfoResponse> getUserInfo(@RequestParam("userId") Long userId) {
        log.info("getUserInfo, userId: {}", userId);
        ApiResponse<UserInfoResponse> response = userClient.getUserInfo(userId);
        log.info("getUserInfo, response: {}", response);
        return response;
    }

    @GetMapping("/login-and-do-the-adding")
    public ApiResponse<String> login() {
        try {
            // 从数据库中查找questions
            List<QuestionDTO> questionDTOList = testService.queryQuestions();

            for(int i = 0; i < questionDTOList.size(); i++){
                QuestionDTO questionDTO = questionDTOList.get(i);
                adjudicatorService.adjudicate(1, UUID.randomUUID().toString(), questionDTO.getQuestionContent());
                adjudicatorService.adjudicate(2, UUID.randomUUID().toString(), questionDTO.getQuestionContent());
            }
            return ApiResponse.success("成 功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
