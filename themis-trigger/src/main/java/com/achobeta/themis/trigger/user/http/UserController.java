package com.achobeta.themis.trigger.user.http;

import com.achobeta.themis.api.user.client.UserClient;
import com.achobeta.themis.api.user.response.UserInfoResponse;
import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.domain.user.model.UserModel;
import com.achobeta.themis.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端http服务请求控制器
 *
 * @author Aseubel
 * @date 2025/10/27 上午2:18
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController implements UserClient {

    private final IUserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(
            @RequestParam("userId") Long userId) {
        try {
            UserModel model = userService.getUserInfo(userId);
            UserInfoResponse userInfo = ofUserInfoResponse(model);
            return ApiResponse.success(userInfo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    private UserInfoResponse ofUserInfoResponse(UserModel model) {
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(model.getUser().getId())
                .username(model.getUser().getUsername())
                .phone(model.getUser().getPhone())
                .build();
        return userInfo;
    }
}
