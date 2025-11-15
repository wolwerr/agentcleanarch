package com.poc.domain.gateway;

import com.poc.domain.exception.ProjectScanException;

import java.nio.file.Path;

public interface ProjectScannerGateway {

    String scanJavaSources(Path projectPath, int maxBytes) throws ProjectScanException;
}
