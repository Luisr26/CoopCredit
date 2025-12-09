package com.coopcredit.credit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Test principal de la aplicación.
 * Verifica que el contexto de Spring se carga correctamente.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CoopCreditApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot se carga correctamente
    }
}
