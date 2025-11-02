package com.achobeta.themis.domain.user.service;

import java.time.Duration;

/**
 * 验证码服务接口
 * @author AckenieoT
 * @date 2025/10/30 上午10:20
 */

public interface IVerifyCodeService {
    /**
     * 生成并存储验证码
     * @param phone
     * @param duration
     * @return
     */
    String generateAndStoreCode(String phone, Duration duration);

    /**
     * 发送验证码
     * @param phone
     * @param code
     */
    void sendVerifyCode(String phone, String code);
}
