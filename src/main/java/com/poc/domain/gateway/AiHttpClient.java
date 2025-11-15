package com.poc.domain.gateway;

import com.poc.domain.exception.AiClientException;

public interface AiHttpClient {

    String sendWithRetry(String body) throws AiClientException;
}
