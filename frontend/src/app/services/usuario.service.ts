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
      sessionStorage.setItem('organiza-token', token);
      return usuario;
    }));
  }
}
