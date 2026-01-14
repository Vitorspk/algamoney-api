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

    // Constantes para validação
    private static final int MIN_SECRET_LENGTH = 32; // 256 bits
    private static final long MIN_EXPIRATION_TIME_MS = 300000; // 5 minutos
    private static final long MAX_EXPIRATION_TIME_MS = 86400000; // 24 horas
    private static final long MINUTES_PER_MILLISECOND = 60000;
    private static final long HOURS_PER_MILLISECOND = 3600000;

    // FIX: Constructor injection com final fields
    private final String jwtSecret;
    private final long expirationTime;
    private final String issuer;
    private final String audience;
    private final String activeProfile;

    public JwtConfigurationValidator(
            @Value("${algamoney.jwt.secret}") String jwtSecret,
            @Value("${algamoney.jwt.expiration-time-ms}") long expirationTime,
            @Value("${algamoney.jwt.issuer}") String issuer,
            @Value("${algamoney.jwt.audience}") String audience,
            @Value("${spring.profiles.active:default}") String activeProfile) {
        this.jwtSecret = jwtSecret;
        this.expirationTime = expirationTime;
        this.issuer = issuer;
        this.audience = audience;
        this.activeProfile = activeProfile;
    }

    /**
     * Valida as configurações JWT no momento da inicialização do bean
     * Falha rápido se houver problema de configuração
     */
    @PostConstruct
    public void validateJwtConfiguration() {
        logger.info("Validating JWT configuration for profile: {}", activeProfile);

        // FIX: Alertar se perfil 'dev' está ativo (pode ser acidental em produção)
        if (activeProfile.contains("dev")) {
            logger.warn("⚠️  Development profile is active. Do NOT use in production!");
        }

        // Validar que o secret não está vazio
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            String errorMsg = "CRITICAL SECURITY ERROR: JWT secret is not configured! " +
                            "Set JWT_SECRET environment variable before starting the application. " +
                            "Generate a strong secret with: openssl rand -base64 64";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // Validar comprimento mínimo do secret (256 bits = 32 bytes = 44 caracteres em base64)
        if (jwtSecret.length() < MIN_SECRET_LENGTH) {
            String errorMsg = "SECURITY WARNING: JWT secret is too short! " +
                            "Current length: " + jwtSecret.length() + " characters. " +
                            "Minimum recommended: " + MIN_SECRET_LENGTH + " characters (256 bits). " +
                            "Generate a strong secret with: openssl rand -base64 64";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // FIX: Validar secret contra valor conhecido do application-dev.properties
        // Hardcoded dev secret for validation - matches application-dev.properties
        // This is intentional to prevent accidental production use of version-controlled secret
        String knownDevSecret = "YMkBXW7Iicvdg/VIVqcUc7ifNntf1mpl0V0FGUDOlEJ4SVLGPo6fpQ2w9YwjirwleoB/6CbNlgUwvDTgkwPMHw==";

        // Fail if using known dev secret outside dev profile
        if (jwtSecret.equals(knownDevSecret) && !activeProfile.contains("dev")) {
            String errorMsg = "CRITICAL SECURITY ERROR: Using development JWT secret in non-dev profile '" + activeProfile + "'! " +
                            "This secret is version-controlled and publicly known. " +
                            "Set JWT_SECRET environment variable with a unique production secret. " +
                            "Generate with: openssl rand -base64 64";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // Also fail if using dev secret in any profile that looks like production
        if (jwtSecret.equals(knownDevSecret) &&
            (activeProfile.contains("prod") || activeProfile.contains("production"))) {
            String errorMsg = "CRITICAL SECURITY ERROR: Using development JWT secret in production profile! " +
                            "This secret is version-controlled and publicly known. " +
                            "Set JWT_SECRET environment variable with a unique production secret. " +
                            "Generate with: openssl rand -base64 64";
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
        if (expirationTime < MIN_EXPIRATION_TIME_MS) {
            logger.warn("JWT expiration time is very short: {} ms ({} minutes)",
                       expirationTime, expirationTime / MINUTES_PER_MILLISECOND);
        }

        if (expirationTime > MAX_EXPIRATION_TIME_MS) {
            logger.warn("JWT expiration time is very long: {} ms ({} hours). " +
                       "Consider shorter tokens with refresh mechanism.",
                       expirationTime, expirationTime / HOURS_PER_MILLISECOND);
        }

        logger.info("✅ JWT configuration validated successfully");
        logger.info("   - Secret length: {} characters", jwtSecret.length());
        logger.info("   - Expiration time: {} ms ({} minutes)",
                   expirationTime, expirationTime / MINUTES_PER_MILLISECOND);
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
