package com.achobeta.themis.infrastructure.user.repo;

import com.achobeta.themis.domain.user.model.entity.User;
import com.achobeta.themis.domain.user.repo.IUserRepository;
import com.achobeta.themis.infrastructure.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author Aseubel
 * @date 2025/10/27 上午2:12
 */
@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

    private final UserMapper userMapper;

    @Override
    public User findUserByUserId(Long userId) {
        return userMapper.selectById(userId);
    }
}
