package com.poc.application.useCase;

import com.poc.domain.entity.ReviewReport;
import com.poc.domain.gateway.AiAgentGateway;
import com.poc.domain.gateway.ProjectScannerGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;

@ApplicationScoped
public class ReviewProjectUseCase {

    private final ProjectScannerGateway projectScannerGateway;
    private final AiAgentGateway aiAgentGateway;

    @Inject
    public ReviewProjectUseCase(ProjectScannerGateway projectScannerGateway,
                                AiAgentGateway aiAgentGateway) {
        this.projectScannerGateway = projectScannerGateway;
        this.aiAgentGateway = aiAgentGateway;
    }

    public ReviewReport review(Path projectPath) throws Exception {
        String snapshot = projectScannerGateway.scanJavaSources(projectPath, 200 * 1024);
        return aiAgentGateway.analyzeProjectSnapshot(snapshot);
    }
}
