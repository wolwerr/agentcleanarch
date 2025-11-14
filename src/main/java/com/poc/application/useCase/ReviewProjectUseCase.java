package com.poc.application.useCase;

import com.poc.domain.gateway.AiAgentGateway;
import com.poc.domain.entity.ReviewReport;
import com.poc.infra.ProjectScanner;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.file.Path;

@ApplicationScoped
public class ReviewProjectUseCase {
    private final ProjectScanner scanner;
    private final AiAgentGateway aiAgentGateway;

    public ReviewProjectUseCase(ProjectScanner scanner, AiAgentGateway aiAgentGateway) {
        this.scanner = scanner;
        this.aiAgentGateway = aiAgentGateway;
    }

    public ReviewReport review(Path projectPath) throws Exception {
        String snapshot = scanner.scanJavaSources(projectPath, 200 * 1024);
        return aiAgentGateway.analyzeProjectSnapshot(snapshot);
    }
}
