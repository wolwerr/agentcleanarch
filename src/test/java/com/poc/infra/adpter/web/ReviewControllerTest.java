package com.poc.infra.adpter.web;

import com.poc.application.useCase.ReviewProjectUseCase;
import com.poc.domain.entity.ReviewReport;
import com.poc.infra.adpter.web.request.PathRequest;
import com.poc.infra.adpter.web.validator.ReviewRequestValidator;
import com.poc.infra.exception.InvalidRequestException;
import com.poc.infra.util.RepoHelper;
import com.poc.infra.util.RepoHelper.PreparedRepo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ReviewControllerTest {

    private void setPathOnRequest(PathRequest req, String pathValue) throws Exception {
        Field f = PathRequest.class.getDeclaredField("path");
        f.setAccessible(true);
        f.set(req, pathValue);
    }

    @Test
    void review_returnsBadRequest_whenValidatorThrows() throws Exception {
        ReviewProjectUseCase useCase = Mockito.mock(ReviewProjectUseCase.class);
        ReviewRequestValidator validator = Mockito.mock(ReviewRequestValidator.class);
        Mockito.doThrow(new InvalidRequestException("O parâmetro 'path' é obrigatório."))
                .when(validator).validate(any());

        ReviewController controller = new ReviewController(useCase, validator);

        PathRequest req = new PathRequest();
        setPathOnRequest(req, null);

        Response resp = controller.review(req);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        assertNotNull(resp.getEntity());
        assertEquals("O parâmetro 'path' é obrigatório.", resp.getEntity());
        assertEquals(MediaType.APPLICATION_JSON, resp.getMediaType().toString());
    }

    @Test
    void review_returnsOk_withReviewReport_whenEverythingOk() throws Exception {
        ReviewProjectUseCase useCase = Mockito.mock(ReviewProjectUseCase.class);
        ReviewRequestValidator validator = Mockito.mock(ReviewRequestValidator.class);
        PreparedRepo prepared = Mockito.mock(PreparedRepo.class);

        Path resolved = Paths.get("C:/meus_projetos/projeto");
        Mockito.when(prepared.pathToUse()).thenReturn(resolved);

        ReviewReport report = Mockito.mock(ReviewReport.class);
        Mockito.when(useCase.review(resolved)).thenReturn(report);

        try (MockedStatic<RepoHelper> mocked = Mockito.mockStatic(RepoHelper.class)) {
            mocked.when(() -> RepoHelper.prepareRepository(any())).thenReturn(prepared);

            ReviewController controller = new ReviewController(useCase, validator);
            PathRequest req = new PathRequest();
            setPathOnRequest(req, "C:/meus_projetos/projeto");

            Response resp = controller.review(req);

            assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
            assertSame(report, resp.getEntity());
        }
    }

    @Test
    void review_returnsBadRequest_whenUseCaseThrows_andReturnsPlainTextMessage() throws Exception {
        ReviewProjectUseCase useCase = Mockito.mock(ReviewProjectUseCase.class);
        ReviewRequestValidator validator = Mockito.mock(ReviewRequestValidator.class);
        PreparedRepo prepared = Mockito.mock(PreparedRepo.class);

        Path resolved = Paths.get("C:/meus_projetos/projeto");
        Mockito.when(prepared.pathToUse()).thenReturn(resolved);

        Mockito.when(useCase.review(resolved)).thenThrow(new RuntimeException("erro interno"));

        try (MockedStatic<RepoHelper> mocked = Mockito.mockStatic(RepoHelper.class)) {
            mocked.when(() -> RepoHelper.prepareRepository(any())).thenReturn(prepared);

            ReviewController controller = new ReviewController(useCase, validator);
            PathRequest req = new PathRequest();
            setPathOnRequest(req, "C:/meus_projetos/projeto");

            Response resp = controller.review(req);

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
            assertEquals("erro interno", resp.getEntity());
            assertEquals(MediaType.TEXT_PLAIN, resp.getMediaType().toString());
        }
    }
}
