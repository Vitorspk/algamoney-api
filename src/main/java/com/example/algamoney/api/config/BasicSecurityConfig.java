package com.example.algamoney.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.algamoney.api.token.JwtAuthenticationFilter;

@Profile("basic-security")
@Configuration
@EnableWebSecurity
public class BasicSecurityConfig {

	// FIX: Constructor injection com final fields (imutabilidade)
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public BasicSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/oauth/token").permitAll() // Permitir acesso ao endpoint de token
				.anyRequest().authenticated()
			)
			// Injetar filtro JWT (já usando constructor injection)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			// Session stateless para JWT
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.csrf(csrf -> csrf.disable())
			// FIX: Adicionar security headers
			.headers(headers -> headers
				// Content Security Policy
				.contentSecurityPolicy(csp -> csp
					.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none'")
				)
				// HTTP Strict Transport Security (HSTS)
				.httpStrictTransportSecurity(hsts -> hsts
					.includeSubDomains(true)
					.maxAgeInSeconds(31536000) // 1 ano
				)
				// Frame Options - previne clickjacking
				.frameOptions(frame -> frame.deny())
				// X-Content-Type-Options - previne MIME sniffing
				.contentTypeOptions(contentType -> {})
				// X-XSS-Protection não é mais necessário em navegadores modernos
				.xssProtection(xss -> xss.disable())
			);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
