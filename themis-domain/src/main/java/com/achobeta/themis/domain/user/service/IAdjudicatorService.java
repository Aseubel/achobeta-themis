package com.achobeta.themis.domain.user.service;

import java.util.List;

public interface IAdjudicatorService {
    /**
     *  adjudication
     * @param userType
     * @param conversationId
     * @param question
     */
    void adjudicate(Integer userType, String conversationId, String question);
}
