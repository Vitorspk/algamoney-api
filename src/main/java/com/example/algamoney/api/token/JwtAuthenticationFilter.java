package com.example.algamoney.api.token;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro para validar JWT tokens em cada requisição
 * Extrai o token do header Authorization e valida usando a secret key configurada
 *
 * SECURITY: Este filtro é um Spring Component para permitir injeção de dependências
 * O secret do JWT é injetado via @Value do application.properties
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    // PERFORMANCE: Reusar Algorithm object ao invés de recriar a cada request
    private final Algorithm algorithm;
    private final String issuer;
    private final String audience;

    // FIX: Constructor injection para otimizar criação do Algorithm
    public JwtAuthenticationFilter(
            @Value("${algamoney.jwt.secret}") String secret,
            @Value("${algamoney.jwt.issuer}") String issuer,
            @Value("${algamoney.jwt.audience}") String audience) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER);

        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace(PREFIX, "");

        try {
            // Verificar e decodificar o token com validação de issuer e audience
            DecodedJWT decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token);

            String username = decodedJWT.getSubject();

            // FIX: Proteção contra null pointer - se authorities for null, usa array vazio
            String[] authorities = decodedJWT.getClaim("authorities").asArray(String.class);
            if (authorities == null) {
                authorities = new String[0];
                logger.warn("Token for user '{}' has no authorities claim", username);
            }

            // Criar autenticação
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Arrays.stream(authorities)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList())
            );

            // Setar no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("JWT authentication successful for user: {}", username);

        } catch (JWTVerificationException e) {
            // FIX: Log específico para erros de validação JWT
            // Token inválido, expirado, assinatura incorreta, issuer/audience inválidos, etc.
            logger.warn("JWT validation failed for request to {}: {}",
                        request.getRequestURI(), e.getMessage());
            // Continua sem autenticação - Spring Security bloqueará se a rota exigir auth
        } catch (Exception e) {
            // Log para erros inesperados
            logger.error("Unexpected error during JWT validation for request to {}",
                        request.getRequestURI(), e);
        }

        chain.doFilter(request, response);
    }
}
