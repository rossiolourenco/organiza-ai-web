import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, map } from "rxjs";
import { API_BASE } from "./api.config";

export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  senha?: string;
  dataCriacao?: string;
  papel?: "USER" | "ADMIN" | "SUPER_ADMIN";
  status?: "ATIVO" | "BLOQUEADO" | "DESATIVADO" | "ANONIMIZADO";
  deveTrocarSenha?: boolean;
}

@Injectable({
  providedIn: "root",
})
export class UsuarioService {
  private readonly apiUrl = `${API_BASE}/usuarios`;

  constructor(private http: HttpClient) {}

  criar(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, usuario);
  }
  login(email: string, senha: string): Observable<Usuario> {
    return this.http.post<Usuario & { token: string }>(`${this.apiUrl}/login`, {
      email,
      senha,
    }).pipe(map(({ token, ...usuario }) => {
      localStorage.setItem('organiza-token', token);
      sessionStorage.removeItem('organiza-token');
      return usuario;
    }));
  }

  /** Perfil do usuário autenticado (o token é adicionado pelo interceptor). */
  perfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/perfil`);
  }

  atualizarPerfil(nome: string): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/perfil`, { nome });
  }

  /** A troca revoga todas as sessões no backend; o frontend deve limpar a sessão local. */
  alterarSenha(senhaAtual: string, novaSenha: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/senha`, { senhaAtual, novaSenha });
  }

  /** Exclusão da própria conta; exige a senha atual e apaga os dados associados. */
  excluirConta(senha: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/conta`, { body: { senha } });
  }
}
