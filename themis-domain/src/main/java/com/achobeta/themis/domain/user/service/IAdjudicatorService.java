package com.achobeta.themis.domain.user.service;

import java.util.List;

public interface IAdjudicatorService {
    void adjudicate(Integer userType, String conversationId, String question);
}
