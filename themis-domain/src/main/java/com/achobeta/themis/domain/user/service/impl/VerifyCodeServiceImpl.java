package com.achobeta.themis.domain.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.achobeta.themis.common.redis.service.RedissonService;
import com.achobeta.themis.common.util.AliSmsUtil;
import com.achobeta.themis.domain.user.service.IVerifyCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 验证码服务实现类
 * @author AckenieoT
 * @date 2025/10/30 上午10:20
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class VerifyCodeServiceImpl implements IVerifyCodeService {
    private final RedissonService redissonService;

    private final AliSmsUtil aliSmsUtil;

    @Override
    public String generateAndStoreCode(String phone, Duration duration) {
        // 生成验证码
        String verifyCode = RandomUtil.randomNumbers(6);
        log.info("生成验证码：{}", verifyCode);
        String verifyCodeKey = "verify_code:" + phone;
        redissonService.setValue(verifyCodeKey, verifyCode, duration.toMillis());
        return verifyCode;
    }

    @Override
    public void sendVerifyCode(String phone, String code) {
         log.info("发送验证码：{} 到手机号：{}", code, phone);
         aliSmsUtil.sendSms(phone, code);
    }
}
