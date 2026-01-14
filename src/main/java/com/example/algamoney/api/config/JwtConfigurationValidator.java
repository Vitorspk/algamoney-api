package com.example.algamoney.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Valida configurações críticas de segurança JWT no startup da aplicação
 * 
 * SECURITY: Garante que o secret do JWT está configurado corretamente
 * antes da aplicação aceitar requisições.
 */
@Component
public class JwtConfigurationValidator {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfigurationValidator.class);

    @Value("${algamoney.jwt.secret}")
    private String jwtSecret;

    @Value("${algamoney.jwt.expiration-time-ms}")
    private long expirationTime;

    @Value("${algamoney.jwt.issuer}")
    private String issuer;

    @Value("${algamoney.jwt.audience}")
    private String audience;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Valida as configurações JWT no momento da inicialização do bean
     * Falha rápido se houver problema de configuração
     */
    @PostConstruct
    public void validateJwtConfiguration() {
        logger.info("Validating JWT configuration for profile: {}", activeProfile);

        // Validar que o secret não está vazio
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            String errorMsg = "CRITICAL SECURITY ERROR: JWT secret is not configured! " +
                            "Set JWT_SECRET environment variable before starting the application. " +
                            "Generate a strong secret with: openssl rand -base64 64";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // Validar comprimento mínimo do secret (256 bits = 32 bytes = 44 caracteres em base64)
        if (jwtSecret.length() < 32) {
            String errorMsg = "SECURITY WARNING: JWT secret is too short! " +
                            "Current length: " + jwtSecret.length() + " characters. " +
                            "Minimum recommended: 32 characters (256 bits). " +
                            "Generate a strong secret with: openssl rand -base64 64";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // Avisar se está usando configuração de desenvolvimento suspeita
        if (jwtSecret.contains("dev") || jwtSecret.contains("development") || 
            jwtSecret.contains("local") || jwtSecret.contains("test")) {
            logger.warn("⚠️  JWT secret appears to contain development keywords. " +
                       "Ensure you're not using a development secret in production!");
        }

        // Validar tempo de expiração razoável
        if (expirationTime < 300000) { // menos de 5 minutos
            logger.warn("JWT expiration time is very short: {} ms ({} minutes)",
                       expirationTime, expirationTime / 60000);
        }

        if (expirationTime > 86400000) { // mais de 24 horas
            logger.warn("JWT expiration time is very long: {} ms ({} hours). " +
                       "Consider shorter tokens with refresh mechanism.",
                       expirationTime, expirationTime / 3600000);
        }

        logger.info("✅ JWT configuration validated successfully");
        logger.info("   - Secret length: {} characters", jwtSecret.length());
        logger.info("   - Expiration time: {} ms ({} minutes)", 
                   expirationTime, expirationTime / 60000);
        logger.info("   - Issuer: {}", issuer);
        logger.info("   - Audience: {}", audience);
    }

    /**
     * Log adicional quando a aplicação está pronta para receber requisições
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("🔒 Security: JWT authentication is active and validated");
    }
}
