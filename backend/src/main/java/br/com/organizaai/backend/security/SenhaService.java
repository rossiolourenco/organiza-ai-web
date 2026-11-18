package br.com.organizaai.backend.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Gera credenciais temporárias fortes, sem persistir texto puro. */
@Service
public class SenhaService {
    private static final String MINUSCULAS = "abcdefghjkmnpqrstuvwxyz";
    private static final String MAIUSCULAS = "ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final String DIGITOS = "23456789";
    private static final String SIMBOLOS = "!@#$%&*?";
    private final SecureRandom random = new SecureRandom();

    public String temporaria() {
        List<Character> caracteres = new ArrayList<>();
        caracteres.add(sortear(MINUSCULAS));
        caracteres.add(sortear(MAIUSCULAS));
        caracteres.add(sortear(DIGITOS));
        caracteres.add(sortear(SIMBOLOS));
        String todos = MINUSCULAS + MAIUSCULAS + DIGITOS + SIMBOLOS;
        while (caracteres.size() < 16) caracteres.add(sortear(todos));
        Collections.shuffle(caracteres, random);
        StringBuilder senha = new StringBuilder();
        caracteres.forEach(senha::append);
        return senha.toString();
    }

    private char sortear(String origem) {
        return origem.charAt(random.nextInt(origem.length()));
    }
}
