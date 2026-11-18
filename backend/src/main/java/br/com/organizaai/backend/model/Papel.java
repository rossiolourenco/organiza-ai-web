package br.com.organizaai.backend.model;

public enum Papel {
    USER, ADMIN, SUPER_ADMIN;

    public boolean administrador() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
}
