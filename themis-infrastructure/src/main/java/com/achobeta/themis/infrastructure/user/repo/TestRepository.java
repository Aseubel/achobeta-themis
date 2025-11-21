package com.achobeta.themis.infrastructure.user.repo;

import com.achobeta.themis.domain.user.model.entity.Questions;
import com.achobeta.themis.domain.user.repo.ITestRepository;
import com.achobeta.themis.infrastructure.user.mapper.TestMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TestRepository implements ITestRepository {
    private final TestMapper testMapper;

    @Override
    public List<Questions> queryQuestions() {

        // 查找id在30 - 150之间的问题
        return testMapper.selectList(new LambdaQueryWrapper<Questions>()
                .between(Questions::getId, 30, 150));
    }
}
