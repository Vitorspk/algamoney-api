package com.example.algamoney.api.cors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.algamoney.api.config.property.AlgamoneyApiProperty;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro CORS com validação segura de origem
 * 
 * SECURITY FIX: Valida o header Origin em TODAS as requisições,
 * não apenas em OPTIONS. Isso previne vulnerabilidades CSRF.
 * 
 * Só adiciona headers CORS se a origem da requisição corresponder
 * à origem permitida configurada.
 */
@Component("customCorsFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);
	
	private final AlgamoneyApiProperty algamoneyApiProperty;
	
	// FIX: Constructor injection ao invés de field injection
	public CorsFilter(AlgamoneyApiProperty algamoneyApiProperty) {
		this.algamoneyApiProperty = algamoneyApiProperty;
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		// FIX: Validar Origin header em TODAS as requisições
		String requestOrigin = request.getHeader("Origin");
		String allowedOrigin = algamoneyApiProperty.getOriginPermitida();
		
		// Só adiciona headers CORS se a origem da requisição for válida
		if (allowedOrigin != null && allowedOrigin.equals(requestOrigin)) {
			response.setHeader("Access-Control-Allow-Origin", requestOrigin);
			response.setHeader("Access-Control-Allow-Credentials", "true");
			
			// Tratamento especial para requisições OPTIONS (preflight)
			if ("OPTIONS".equals(request.getMethod())) {
				response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
				response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
				response.setHeader("Access-Control-Max-Age", "3600");
				response.setStatus(HttpServletResponse.SC_OK);
				return; // Não continua a cadeia para OPTIONS
			}
			
			logger.debug("CORS headers set for origin: {}", requestOrigin);
		} else if (requestOrigin != null) {
			// Origem não permitida - log para auditoria
			logger.warn("CORS request from unauthorized origin rejected: {}", requestOrigin);
		}
		
		// Continua a cadeia de filtros
		chain.doFilter(req, resp);
	}
	
	@Override
	public void destroy() {
		// Cleanup se necessário
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		logger.info("CORS Filter initialized with allowed origin: {}", 
		           algamoneyApiProperty.getOriginPermitida());
	}

}
