package com.achobeta.themis.domain.laws.repo;

import com.achobeta.themis.domain.laws.model.entity.LawRegulation;

import java.util.List;

/**
 * 法律条文仓储层接口
 */
public interface ILawRegulationRepository {

    /**
     * 查询所有法律条文
     * @return 法律条文列表
     */
    List<LawRegulation> listLawRegulations();
}
