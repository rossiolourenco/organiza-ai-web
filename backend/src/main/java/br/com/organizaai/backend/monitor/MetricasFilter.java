package br.com.organizaai.backend.monitor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Mede apenas chamadas da API, sem tocar em conteúdo ou credenciais. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MetricasFilter extends OncePerRequestFilter {
    private final MetricsRegistry registry;

    public MetricasFilter(MetricsRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        long inicio = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            registry.registrar(response.getStatus(), System.nanoTime() - inicio);
        }
    }
}
