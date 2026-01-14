package com.example.algamoney.api.token;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
 */
@RestController
@RequestMapping("/oauth")
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${algamoney.jwt.secret}")
    private String secret;

    @Value("${algamoney.jwt.expiration-time-ms:1800000}")
    private long expirationTime;

    @Value("${algamoney.jwt.issuer}")
    private String issuer;

    @Value("${algamoney.jwt.audience}")
    private String audience;

    @PostMapping("/token")
    public ResponseEntity<?> token(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("grant_type") String grantType) {

        logger.info("Authentication attempt for user: {}", username);

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

            // FIX: Extrair authorities como List diretamente, não como String
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            // FIX: Obter nome real do usuário em vez de username/email
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
                .withClaim("authorities", authorities) // FIX: Direto como List, não String
                .withClaim("nome", nomeUsuario) // FIX: Nome real do usuário
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC256(secret));

            logger.info("Token generated successfully for user: {} with {} authorities",
                       username, authorities.size());

            // Resposta no formato OAuth2
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", token);
            response.put("token_type", "bearer");
            response.put("expires_in", expirationTime / 1000); // em segundos
            response.put("scope", "read write");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {} - Invalid credentials", username);
            Map<String, String> error = new HashMap<>();
            error.put("error", "invalid_grant");
            error.put("error_description", "Usuário ou senha inválida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user: {}", username, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "server_error");
            error.put("error_description", "Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
