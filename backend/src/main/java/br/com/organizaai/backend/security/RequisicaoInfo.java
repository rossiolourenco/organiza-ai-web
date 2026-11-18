package br.com.organizaai.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/** Extrai metadados de rede sem confiar em cabeçalhos arbitrários além do proxy local. */
@Component
public class RequisicaoInfo {
    public String ip() {
        HttpServletRequest request = request();
        if (request == null) return null;
        String encaminhado = request.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return recortar(encaminhado.split(",")[0].trim(), 64);
        }
        return recortar(request.getRemoteAddr(), 64);
    }

    public String userAgent() {
        HttpServletRequest request = request();
        return request == null ? null : recortar(request.getHeader("User-Agent"), 255);
    }

    public String correlacao() {
        HttpServletRequest request = request();
        String header = request == null ? null : request.getHeader("X-Correlation-Id");
        if (header == null || header.isBlank()) return UUID.randomUUID().toString();
        return recortar(header.replaceAll("[^A-Za-z0-9_.:-]", ""), 64);
    }

    public String metodo() {
        HttpServletRequest request = request();
        return request == null ? null : request.getMethod();
    }

    private HttpServletRequest request() {
        var attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    public static String recortar(String valor, int limite) {
        if (valor == null) return null;
        String limpo = valor.replaceAll("[\\r\\n\\t]", " ").trim();
        return limpo.length() <= limite ? limpo : limpo.substring(0, limite);
    }
}
