package com.poc.domain.gateway;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AiHttpClientTest {

    @Test
    void isInterface_and_hasSendWithRetryWithExpectedSignature() throws Exception {
        Class<?> iface = Class.forName("com.poc.domain.gateway.AiHttpClient");
        assertTrue(iface.isInterface(), "AiHttpClient deve ser uma interface");

        Method method = iface.getMethod("sendWithRetry", String.class);
        assertEquals(String.class, method.getReturnType(), "Tipo de retorno inesperado");

        Class<?>[] declared = method.getExceptionTypes();
        assertEquals(1, declared.length, "Número de exceções declaradas diferente do esperado");
        assertEquals("com.poc.domain.exception.AiClientException", declared[0].getName(),
                "Exceção declarada inesperada");
    }
}
