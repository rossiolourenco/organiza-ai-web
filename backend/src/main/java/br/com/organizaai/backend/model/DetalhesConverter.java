package br.com.organizaai.backend.model;

import br.com.organizaai.backend.dto.DetalhesTarefa;
import jakarta.persistence.*;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class DetalhesConverter implements AttributeConverter<DetalhesTarefa, String> {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    public String convertToDatabaseColumn(DetalhesTarefa value) {
        return value == null ? null : MAPPER.writeValueAsString(value);
    }
    public DetalhesTarefa convertToEntityAttribute(String value) {
        return value == null ? null : MAPPER.readValue(value, DetalhesTarefa.class);
    }
}
