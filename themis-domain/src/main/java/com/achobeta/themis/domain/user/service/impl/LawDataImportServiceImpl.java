package com.achobeta.themis.domain.user.service.impl;

import com.achobeta.themis.domain.user.repo.ILawDataImportRepository;
import com.achobeta.themis.domain.user.service.ILawDataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawDataImportServiceImpl implements ILawDataImportService {
    
    private final ILawDataImportRepository lawDataImportRepository;
    
    @Override
    public int importLawDataFromDatabase() throws Exception {
        return lawDataImportRepository.importLawDataFromDatabase();
    }
}
