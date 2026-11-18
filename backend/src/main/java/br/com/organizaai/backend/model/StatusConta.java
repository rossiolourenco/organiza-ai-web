package br.com.organizaai.backend.model;

public enum StatusConta {
    ATIVO, BLOQUEADO, DESATIVADO, ANONIMIZADO;

    public boolean acessa() {
        return this == ATIVO;
    }
}
