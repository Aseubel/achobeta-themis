package com.achobeta.themis.domain.laws.service.impl;

import com.achobeta.themis.domain.laws.model.entity.LawRegulation;
import com.achobeta.themis.domain.laws.repo.ILawRegulationRepository;
import com.achobeta.themis.domain.laws.service.ILawRegulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawRegulationService implements ILawRegulationService {

    private final ILawRegulationRepository lawRegulationRepository;

    @Override
    public List<LawRegulation> queryLawRegulationList() {
        return lawRegulationRepository.listLawRegulations();
    }
}
