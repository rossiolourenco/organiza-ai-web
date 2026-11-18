package br.com.organizaai.backend.dto;

public record PoliticaRequest(Integer senhaMinima, Boolean exigirNumero, Boolean exigirMaiuscula,
    Integer maxTentativas, Integer janelaMinutos, Integer bloqueioMinutos, Integer sessaoDias) {}
