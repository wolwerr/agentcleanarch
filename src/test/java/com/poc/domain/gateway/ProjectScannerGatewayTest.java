package com.poc.domain.gateway;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectScannerGatewayTest {

    @Test
    void isInterface_and_hasScanJavaSourcesWithExpectedSignature() throws Exception {
        Class<?> iface = Class.forName("com.poc.domain.gateway.ProjectScannerGateway");
        assertTrue(iface.isInterface(), "ProjectScannerGateway deve ser uma interface");

        Method method = iface.getMethod("scanJavaSources", Path.class, int.class);
        assertEquals(String.class, method.getReturnType(), "Tipo de retorno inesperado");

        Class<?>[] params = method.getParameterTypes();
        assertEquals(2, params.length, "Número de parâmetros inesperado");
        assertEquals(Path.class, params[0], "Tipo do primeiro parâmetro inesperado");
        assertEquals(int.class, params[1], "Tipo do segundo parâmetro inesperado");

        Class<?>[] declared = method.getExceptionTypes();
        assertEquals(1, declared.length, "Número de exceções declaradas diferente do esperado");
        assertEquals("com.poc.domain.exception.ProjectScanException", declared[0].getName(),
                "Exceção declarada inesperada");
    }
}
