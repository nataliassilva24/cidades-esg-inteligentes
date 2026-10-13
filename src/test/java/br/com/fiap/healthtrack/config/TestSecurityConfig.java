package br.com.fiap.healthtrack.config;

import br.com.fiap.healthtrack.config.SecurityConfig;
import br.com.fiap.healthtrack.security.SecurityAccessDeniedHandler;
import br.com.fiap.healthtrack.security.SecurityEntryPoint;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Garante que SecurityConfig e seus handlers sejam carregados no contexto @WebMvcTest.
 */
@TestConfiguration
@Import({SecurityConfig.class, SecurityEntryPoint.class, SecurityAccessDeniedHandler.class})
public class TestSecurityConfig {
}
