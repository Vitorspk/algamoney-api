package com.example.algamoney.api.token;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro para validar JWT tokens em cada requisição
 * Extrai o token do header Authorization e valida usando a mesma secret key
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String SECRET = "algaworks";
    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

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
            // Verificar e decodificar o token
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token);

            String username = decodedJWT.getSubject();
            String[] authorities = decodedJWT.getClaim("authorities").asArray(String.class);

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

        } catch (Exception e) {
            // Token inválido ou expirado - deixa passar sem autenticação
            // O Spring Security vai bloquear se a rota exigir autenticação
        }

        chain.doFilter(request, response);
    }
}