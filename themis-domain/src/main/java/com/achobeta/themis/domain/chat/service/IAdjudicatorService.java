package com.achobeta.themis.domain.chat.service;

public interface IAdjudicatorService {
    /**
     *  adjudication
     * @param userType
     * @param conversationId
     * @param question
     */
    void adjudicate(Integer userType, String conversationId, String question);
}
