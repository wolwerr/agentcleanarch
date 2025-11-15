package com.poc.domain.gateway;

import com.poc.domain.entity.ReviewReport;
import com.poc.domain.exception.AgentAnalysisException;
import com.poc.infra.exception.PromptBuildException;

import java.io.IOException;

public interface AiAgentGateway {
    ReviewReport analyzeProjectSnapshot(String projectSnapshot) throws AgentAnalysisException, IOException, InterruptedException, PromptBuildException;
}
