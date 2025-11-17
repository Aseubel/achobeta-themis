package com.achobeta.themis.common.util;


import com.achobeta.themis.common.exception.BusinessException;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信工具类
 * @author AckenieoT
 * @date 2025/10/30 上午10:20
 */
@Slf4j
@Component
public class AliSmsUtil {
    // 阿里云访问密钥ID
    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    // 阿里云访问密钥Secret
    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    // 短信签名
    @Value("${aliyun.sms.sign-name}")
    private String signName;

    // 短信模板ID
    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    // 产品名称:云通信短信API产品
    private final String product = "Dysmsapi";

    // 产品域名
    private final String domain = "dysmsapi.aliyuncs.com";


    public void sendSms(String phone, String code) {
        try {
            // 创建短信发送请求
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                    accessKeyId, accessKeySecret);
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
            DefaultAcsClient acsClient = new DefaultAcsClient(profile);

            // 短信模板参数
            String templateParam = String.format("{\"code\":\"%s\"}", code);

            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam(templateParam);

            // 发送短信
            SendSmsResponse response = acsClient.getAcsResponse(request);
            log.info("短信发送成功，短信ID：{}", response.getBizId());

        } catch (Exception e) {
            log.error("短信发送失败，异常信息：{}", e.getMessage());
            throw new BusinessException("验证码发送失败");
        }
    }
}
