package com.example.algamoney.api.token;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.algamoney.api.security.UsuarioSistema;

/**
 * Controller para simular endpoint OAuth2 /oauth/token
 * Compatível com frontend Angular que espera OAuth2 Password Grant Flow
 * Gera JWT tokens compatíveis com o formato esperado pelo frontend
 *
 * SECURITY IMPROVEMENTS:
 * - JWT secret injetado via application.properties
 * - Expiration time configurável
 * - Issuer e Audience para validação adicional
 * - Logging de tentativas de autenticação
 * - Authorities como List em vez de String separada por vírgulas
 * - Nome real do usuário em vez de username/email
 * - Constructor injection (imutabilidade)
 */
@RestController
@RequestMapping("/oauth")
@Validated
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // Limites de tamanho para prevenir DoS
    private static final int MAX_USERNAME_LENGTH = 255;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final int MAX_GRANT_TYPE_LENGTH = 50;

    // FIX: Constructor injection com final fields
    private final AuthenticationManager authenticationManager;
    private final Algorithm algorithm; // PERFORMANCE: Reusar ao invés de recriar
    private final long expirationTime;
    private final String issuer;
    private final String audience;

    public TokenController(
            AuthenticationManager authenticationManager,
            @Value("${algamoney.jwt.secret}") String secret,
            @Value("${algamoney.jwt.expiration-time-ms:1800000}") long expirationTime,
            @Value("${algamoney.jwt.issuer}") String issuer,
            @Value("${algamoney.jwt.audience}") String audience) {
        this.authenticationManager = authenticationManager;
        this.algorithm = Algorithm.HMAC256(secret); // Criar uma vez no construtor
        this.expirationTime = expirationTime;
        this.issuer = issuer;
        this.audience = audience;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(
            @RequestParam("username")
            @NotBlank(message = "Username is required")
            @Size(max = MAX_USERNAME_LENGTH, message = "Username too long")
            String username,

            @RequestParam("password")
            @NotBlank(message = "Password is required")
            @Size(max = MAX_PASSWORD_LENGTH, message = "Password too long")
            String password,

            @RequestParam("grant_type")
            @NotBlank(message = "Grant type is required")
            @Size(max = MAX_GRANT_TYPE_LENGTH, message = "Grant type too long")
            String grantType) {

        // FIX: Sanitizar username para prevenir log injection
        String sanitizedUsername = username.replaceAll("[\n\r\t]", "_");
        logger.info("Authentication attempt for user: {}", sanitizedUsername);

        if (!"password".equals(grantType)) {
            logger.warn("Unsupported grant type requested: {}", grantType);
            Map<String, String> error = new HashMap<>();
            error.put("error", "unsupported_grant_type");
            error.put("error_description", "Grant type not supported: " + grantType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            // Autenticar usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            // Extrair authorities como List diretamente
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            // Obter nome real do usuário
            String nomeUsuario = username; // fallback
            if (authentication.getPrincipal() instanceof UsuarioSistema) {
                UsuarioSistema usuarioSistema = (UsuarioSistema) authentication.getPrincipal();
                nomeUsuario = usuarioSistema.getUsuario().getNome();
            }

            // Gerar JWT token com issuer, audience e melhor estrutura
            String token = JWT.create()
                .withIssuer(issuer)
                .withAudience(audience)
                .withSubject(username)
                .withClaim("user_name", username)
                .withClaim("authorities", authorities)
                .withClaim("nome", nomeUsuario)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(algorithm);

            logger.info("Token generated successfully for user: {} with {} authorities",
                       sanitizedUsername, authorities.size());

            // Resposta no formato OAuth2
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", token);
            response.put("token_type", "bearer");
            response.put("expires_in", expirationTime / MILLISECONDS_PER_SECOND);
            response.put("scope", "read write");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {} - Invalid credentials", sanitizedUsername);
            Map<String, String> error = new HashMap<>();
            error.put("error", "invalid_grant");
            error.put("error_description", "Usuário ou senha inválida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user: {}", sanitizedUsername, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "server_error");
            error.put("error_description", "Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
