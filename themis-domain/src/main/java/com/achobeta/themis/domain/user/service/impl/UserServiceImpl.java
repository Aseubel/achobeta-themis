package com.achobeta.themis.domain.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.achobeta.themis.common.exception.BusinessException;
import com.achobeta.themis.domain.user.model.UserModel;
import com.achobeta.themis.domain.user.model.entity.User;
import com.achobeta.themis.domain.user.repo.IUserRepository;
import com.achobeta.themis.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Aseubel
 * @date 2025/10/27 上午2:20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService{

    private final IUserRepository userRepository;

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
}
