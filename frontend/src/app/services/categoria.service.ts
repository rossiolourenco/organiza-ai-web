import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Categoria {
  id?: number;
  nome: string;
  cor?: string;
}

@Injectable({
  providedIn: "root",
})
export class CategoriaService {

  private readonly apiUrl = "http://localhost:8080/api/categorias";

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
}
