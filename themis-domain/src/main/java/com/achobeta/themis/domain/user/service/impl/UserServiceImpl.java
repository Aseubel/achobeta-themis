package com.achobeta.themis.domain.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.common.redis.service.RedissonService;
import com.achobeta.themis.common.util.JwtUtil;
import com.achobeta.themis.domain.user.model.vo.AuthResponseVO;
import com.achobeta.themis.domain.user.model.vo.ForgetPasswdRequestVO;
import com.achobeta.themis.domain.user.model.vo.LoginRequestVO;
import com.achobeta.themis.domain.user.model.UserModel;
import com.achobeta.themis.domain.user.model.entity.User;
import com.achobeta.themis.domain.user.repo.IUserRepository;
import com.achobeta.themis.domain.user.service.IUserService;
import com.achobeta.themis.domain.user.service.IVerifyCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

/**
 * @author Aseubel
 * @date 2025/10/27 上午2:20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService{

    private final IUserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final RedissonService redissonService;

    private final IVerifyCodeService verifyCodeService;

    private final BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder(10);

    @Override
    public UserModel getUserByUserId(UserModel userModel) {
        User user = userRepository.findUserByUserId(userModel.getUser().getId());
        UserModel result = UserModel.builder()
                .user(user)
                .build();
        return result;
    }

    @Override
    public UserModel getUserInfo(Long userId) {
        if (ObjectUtil.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userRepository.findUserByUserId(userId);
        UserModel result = UserModel.builder()
                .user(user)
                .build();
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AuthResponseVO login(LoginRequestVO request) {
        String password = bCryptEncoder.encode(request.getPassword());
        User user = userRepository.findUserByPhone(request.getPhone());

        if (ObjectUtil.isEmpty(user)) {
            // 为新用户添加账号
            user = User.builder()
                    .username(UUID.randomUUID().toString().substring(0, 8))
                    .password(password)
                    .phone(request.getPhone())
                    .userType(request.getUserType())
                    .build();
            userRepository.save(user);
            user.setUserId(String.format("%06d", user.getId()));
            userRepository.update(user);
        } else {
            // 校验密码
            if (!bCryptEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException("密码错误");
            }
        }
        // 生成双token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        String refreshTokenKey = "refresh_token:" + user.getId();
        redissonService.setValue(refreshTokenKey, refreshToken, jwtUtil.getRefreshTokenExpiration());

        return AuthResponseVO.builder()
                .userId(user.getId().toString())
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponseVO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效");
        }
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效");
        }
        String refreshTokenKey = "refresh_token:" + jwtUtil.getUserIdFromToken(refreshToken);
        String storedRefreshToken = redissonService.getValue(refreshTokenKey);
        if (ObjectUtil.isEmpty(storedRefreshToken)) {
            throw new BusinessException("刷新令牌无效");
        }
        if (!storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException("刷新令牌无效");
        }
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String accessToken = jwtUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);
        redissonService.setValue(refreshTokenKey, newRefreshToken, jwtUtil.getRefreshTokenExpiration());
        return AuthResponseVO.builder()
                .userId(userId.toString())
                .username(username)
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void forgetPassword(ForgetPasswdRequestVO request) {
        // 校验验证码是否正确
        String verifyCodeKey = "verify_code:" + request.getPhone();
        String storedVerifyCode = redissonService.getValue(verifyCodeKey);
        if (ObjectUtil.isEmpty(storedVerifyCode)) {
            throw new BusinessException("验证码已过期");
        }
        if (!storedVerifyCode.equals(request.getVerifyCode())) {
            throw new BusinessException("验证码错误");
        }
        // 校验手机号是否存在
        User user = userRepository.findUserByPhone(request.getPhone());
        if (ObjectUtil.isEmpty(user)) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(bCryptEncoder.encode(request.getNewPassword()));
        userRepository.update(user);
        redissonService.remove(verifyCodeKey);
    }

    @Override
    public void sendVerifyCode(String phone) {
        // 校验手机号是否存在
        User user = userRepository.findUserByPhone(phone);
        if (ObjectUtil.isEmpty(user)) {
            throw new BusinessException("手机号不存在");
        }
        String code = verifyCodeService.generateAndStoreCode(phone, Duration.ofMinutes(1));
        // 发送验证码
        log.info("发送验证码：{}", code);


        // 控制台打印验证码
        System.out.println("\n\n\n\n-------------------------\n"
                + "验证码：" + code + "\n"
                + "-------------------------\n");


        verifyCodeService.sendVerifyCode(phone, code);
    }

    @Override
    public void logout(String refreshToken) {
        // 校验刷新令牌是否有效
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效");
        }
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效");
        }
        String refreshTokenKey = "refresh_token:" + jwtUtil.getUserIdFromToken(refreshToken);
        redissonService.remove(refreshTokenKey);
    }
}
