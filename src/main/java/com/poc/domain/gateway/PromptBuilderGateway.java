package com.poc.domain.gateway;

import com.poc.infra.exception.PromptBuildException;

public interface PromptBuilderGateway {

    String buildRequestBody(String model, String systemContent, String userContent) throws PromptBuildException;
}
