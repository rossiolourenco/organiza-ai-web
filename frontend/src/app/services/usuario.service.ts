import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  senha?: string;
  dataCriacao?: string;
}

@Injectable({
  providedIn: "root",
})
export class UsuarioService {
  private readonly apiUrl = "http://localhost:8080/api/usuarios";

  constructor(private http: HttpClient) {}

  criar(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, usuario);
  }
  login(email: string, senha: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/login`, {
      email,
      senha,
    });
  }
}
