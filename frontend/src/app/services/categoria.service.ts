import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { API_BASE } from "./api.config";

export interface Categoria {
  id?: number;
  nome: string;
  cor?: string;
}

@Injectable({
  providedIn: "root",
})
export class CategoriaService {

  private readonly apiUrl = `${API_BASE}/categorias`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Categoria[]> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.get<Categoria[]>(
      `${this.apiUrl}?usuarioId=${usuario.id}`
    );
  }

  criar(categoria: Categoria): Observable<Categoria> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.post<Categoria>(
      `${this.apiUrl}?usuarioId=${usuario.id}`,
      categoria
    );
  }

  excluir(id: number): Observable<void> {
    const usuarioSalvo = localStorage.getItem("usuario");
    if (!usuarioSalvo) throw new Error("Usuário não autenticado.");
    const usuario = JSON.parse(usuarioSalvo);
    return this.http.delete<void>(`${this.apiUrl}/${id}?usuarioId=${usuario.id}`);
  }

  atualizar(id: number, categoria: Categoria): Observable<Categoria> {
    const usuarioSalvo = localStorage.getItem("usuario");
    if (!usuarioSalvo) throw new Error("Usuário não autenticado.");
    const usuario = JSON.parse(usuarioSalvo);
    return this.http.put<Categoria>(
      `${this.apiUrl}/${id}?usuarioId=${usuario.id}`,
      categoria
    );
  }

}
