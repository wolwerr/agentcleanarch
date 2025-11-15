package com.poc.infra.adpter.web;

import com.poc.application.useCase.ReviewProjectUseCase;
import com.poc.domain.entity.ReviewReport;
import com.poc.infra.adpter.web.request.PathRequest;
import com.poc.infra.util.RepoHelper;
import com.poc.infra.util.RepoHelper.PreparedRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@jakarta.ws.rs.Path("/api/review")
@ApplicationScoped
public class ReviewController {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewProjectUseCase useCase;

    @Inject
    public ReviewController(ReviewProjectUseCase useCase) {
        this.useCase = useCase;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
            responseCode = "200",
            description = "Relatório de avaliação",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ReviewReport.class))
    )
    public Response review(PathRequest request) {
        String caminhoProjeto = request == null ? null : request.getPath();
        if (caminhoProjeto == null || caminhoProjeto.isBlank()) {
            LOG.warn("Parâmetro 'path' ausente ou vazio na requisição");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parâmetro 'path' ausente no corpo da requisição.")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        LOG.info("Iniciando análise do projeto, path={}", caminhoProjeto);

        try (PreparedRepo prepared = RepoHelper.prepareRepository(caminhoProjeto)) {
            Path caminhoUsado = prepared.pathToUse();
            LOG.debug("Caminho resolvido para análise: {}", caminhoUsado);

            ReviewReport relatorio = useCase.review(caminhoUsado);

            LOG.info("Análise concluída com sucesso para path={}", caminhoUsado);
            return Response.ok(relatorio).build();

        } catch (Exception e) {
            LOG.error("Erro ao processar análise para path={}: {}", caminhoProjeto, e.getMessage(), e);
            String msg = e.getMessage() == null ? "Erro desconhecido ao processar a análise." : e.getMessage();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
}
