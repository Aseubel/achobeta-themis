package com.achobeta.themis.trigger.test.http;

import com.achobeta.themis.api.user.client.UserClient;
import com.achobeta.themis.api.user.response.UserInfoResponse;
import com.achobeta.themis.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/getUserInfo")
    public ApiResponse<UserInfoResponse> getUserInfo(@RequestParam("userId") Long userId) {
        log.info("getUserInfo, userId: {}", userId);
        ApiResponse<UserInfoResponse> response = userClient.getUserInfo(userId);
        log.info("getUserInfo, response: {}", response);
        return response;
    }
}
