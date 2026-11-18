package br.com.organizaai.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(List<T> content, int page, int size, long total, int totalPages) {
    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> mapper) {
        return new PaginaResponse<>(pagina.getContent().stream().map(mapper).toList(),
            pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
