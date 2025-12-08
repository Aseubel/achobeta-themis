package com.achobeta.themis.domain.laws.service;

import com.achobeta.themis.domain.laws.model.entity.LawRegulation;

import java.util.List;

/**
 * 法律条文服务接口
 */
public interface ILawRegulationService {

    /**
     * 查询法律条文列表
     * @return 法律条文列表
     */
    List<LawRegulation> queryLawRegulationList();
}
