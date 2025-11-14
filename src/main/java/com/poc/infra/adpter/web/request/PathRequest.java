package com.poc.infra.adpter.web.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.RestForm;

@Schema(name = "PathRequest", description = "Parâmetros do formulário")
public class PathRequest {

    @RestForm
    @Schema(description = "URL ou caminho do repositório", example = "https://github.com/wolwerr/order")
    String path;

    public String getPath() { return path; }
}
