package com.poc.application.useCase;

import com.poc.domain.entity.ReviewReport;
import com.poc.domain.gateway.AiAgentGateway;
import com.poc.domain.gateway.ProjectScannerGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewProjectUseCaseTest {

    @Mock
    ProjectScannerGateway projectScannerGateway;

    @Mock
    AiAgentGateway aiAgentGateway;

    @InjectMocks
    ReviewProjectUseCase useCase;

    @Captor
    ArgumentCaptor<Integer> intCaptor;

    @Test
    void review_success_returnsReport() throws Exception {
        Path projectPath = Path.of("dummy");
        String snapshot = "project snapshot";
        ReviewReport expectedReport = mock(ReviewReport.class);

        when(projectScannerGateway.scanJavaSources(projectPath, 200 * 1024)).thenReturn(snapshot);
        when(aiAgentGateway.analyzeProjectSnapshot(snapshot)).thenReturn(expectedReport);

        ReviewReport result = useCase.review(projectPath);

        assertSame(expectedReport, result);
        verify(projectScannerGateway, times(1)).scanJavaSources(projectPath, 200 * 1024);
        verify(aiAgentGateway, times(1)).analyzeProjectSnapshot(snapshot);
        verifyNoMoreInteractions(projectScannerGateway, aiAgentGateway);
    }

    @Test
    void review_whenScannerThrows_exceptionPropagated() throws Exception {
        Path projectPath = Path.of("dummy");
        when(projectScannerGateway.scanJavaSources(any(), anyInt())).thenThrow(new RuntimeException("scan failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.review(projectPath));
        assertEquals("scan failed", ex.getMessage());
        verify(projectScannerGateway, times(1)).scanJavaSources(projectPath, 200 * 1024);
        verifyNoInteractions(aiAgentGateway);
    }

    @Test
    void review_whenAiAgentThrows_exceptionPropagated() throws Exception {
        Path projectPath = Path.of("dummy");
        String snapshot = "snapshot";
        when(projectScannerGateway.scanJavaSources(projectPath, 200 * 1024)).thenReturn(snapshot);
        when(aiAgentGateway.analyzeProjectSnapshot(snapshot)).thenThrow(new IllegalStateException("ai failed"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> useCase.review(projectPath));
        assertEquals("ai failed", ex.getMessage());
        verify(projectScannerGateway, times(1)).scanJavaSources(projectPath, 200 * 1024);
        verify(aiAgentGateway, times(1)).analyzeProjectSnapshot(snapshot);
    }

    @Test
    void review_usesCorrectScanLimitArgument() throws Exception {
        Path projectPath = Path.of("dummy");
        String snapshot = "snapshot";
        ReviewReport expected = mock(ReviewReport.class);

        when(projectScannerGateway.scanJavaSources(eq(projectPath), intCaptor.capture())).thenReturn(snapshot);
        when(aiAgentGateway.analyzeProjectSnapshot(snapshot)).thenReturn(expected);

        ReviewReport result = useCase.review(projectPath);

        assertSame(expected, result);
        assertEquals(200 * 1024, intCaptor.getValue().intValue());
    }
}
