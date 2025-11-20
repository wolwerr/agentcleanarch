package com.poc.domain.gateway;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AiAgentGatewayTest {

    @Test
    void isInterface_and_hasAnalyzeProjectSnapshotWithExpectedSignature() throws Exception {
        Class<?> iface = Class.forName("com.poc.domain.gateway.AiAgentGateway");
        assertTrue(iface.isInterface(), "AiAgentGateway deve ser uma interface");

        Method method = iface.getMethod("analyzeProjectSnapshot", String.class);
        Class<?> returnType = method.getReturnType();
        assertEquals(Class.forName("com.poc.domain.entity.ReviewReport"), returnType, "Tipo de retorno inesperado");

        Class<?>[] declared = method.getExceptionTypes();
        boolean hasAgent = false, hasIo = false, hasInterrupted = false, hasPrompt = false;
        for (Class<?> ex : declared) {
            String name = ex.getName();
            if (name.equals("com.poc.domain.exception.AgentAnalysisException")) hasAgent = true;
            if (name.equals("java.io.IOException")) hasIo = true;
            if (name.equals("java.lang.InterruptedException")) hasInterrupted = true;
            if (name.equals("com.poc.infra.exception.PromptBuildException")) hasPrompt = true;
        }

        assertTrue(hasAgent, "Falta AgentAnalysisException nas exceções declaradas");
        assertTrue(hasIo, "Falta IOException nas exceções declaradas");
        assertTrue(hasInterrupted, "Falta InterruptedException nas exceções declaradas");
        assertTrue(hasPrompt, "Falta PromptBuildException nas exceções declaradas");

        // opcional: garantir que não haja exceções inesperadas além das previstas
        assertEquals(4, declared.length, "Número de exceções declaradas diferente do esperado: " + Arrays.toString(declared));
    }
}
