package com.achobeta.themis.domain.user.service;

import java.time.Duration;

/**
 * 验证码服务接口
 * @author AckenieoT
 * @date 2025/10/30 上午10:20
 */

public interface IVerifyCodeService {
    String generateAndStoreCode(String phone, Duration duration);

    void sendVerifyCode(String phone, String code);
}
