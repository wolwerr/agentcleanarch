package com.poc.infra.adpter.web.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.RestForm;

@Schema(name = "PathRequest", description = "Parâmetros do formulário")
public class PathRequest {

    @RestForm
    @Schema(description = "URL do Github ou caminho do repositório no PC", example = "https://github.com/wolwerr/order ou C:\\meus_projetos\\projeto")
    String path;

    public String getPath() { return path; }
}
