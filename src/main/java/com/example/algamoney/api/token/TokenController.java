package com.example.algamoney.api.token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Date;

/**
 * Controller para simular endpoint OAuth2 /oauth/token
 * Compatível com frontend Angular que espera OAuth2 Password Grant Flow
 * Gera JWT tokens compatíveis com o formato esperado pelo frontend
 */
@RestController
@RequestMapping("/oauth")
public class TokenController {

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final String SECRET = "algaworks"; // Mesmo secret usado no OAuth2 antigo
    private static final long EXPIRATION_TIME = 1800000; // 30 minutos em ms

    @PostMapping("/token")
    public ResponseEntity<?> token(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("grant_type") String grantType) {

        if (!"password".equals(grantType)) {
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

            // Extrair authorities (roles/permissões)
            String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

            // Gerar JWT token
            String token = JWT.create()
                .withSubject(username)
                .withClaim("user_name", username)
                .withClaim("authorities", Arrays.asList(authorities.split(",")))
                .withClaim("nome", username) // Nome do usuário
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET));

            // Resposta no formato OAuth2
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", token);
            response.put("token_type", "bearer");
            response.put("expires_in", EXPIRATION_TIME / 1000); // em segundos
            response.put("scope", "read write");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "invalid_grant");
            error.put("error_description", "Usuário ou senha inválida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}