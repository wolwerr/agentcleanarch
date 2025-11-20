package com.poc.domain.gateway;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderGatewayTest {

    @Test
    void isInterface_and_hasBuildRequestBodyWithExpectedSignature() throws Exception {
        Class<?> iface = Class.forName("com.poc.domain.gateway.PromptBuilderGateway");
        assertTrue(iface.isInterface(), "PromptBuilderGateway deve ser uma interface");

        Method method = iface.getMethod("buildRequestBody", String.class, String.class, String.class);
        assertEquals(String.class, method.getReturnType(), "Tipo de retorno inesperado");

        Class<?>[] params = method.getParameterTypes();
        assertEquals(3, params.length, "Número de parâmetros inesperado");
        assertEquals(String.class, params[0], "Tipo do primeiro parâmetro inesperado");
        assertEquals(String.class, params[1], "Tipo do segundo parâmetro inesperado");
        assertEquals(String.class, params[2], "Tipo do terceiro parâmetro inesperado");

        Class<?>[] declared = method.getExceptionTypes();
        assertEquals(1, declared.length, "Número de exceções declaradas diferente do esperado");
        assertEquals("com.poc.infra.exception.PromptBuildException", declared[0].getName(),
                "Exceção declarada inesperada");
    }
}
