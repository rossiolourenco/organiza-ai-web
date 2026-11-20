import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

import { API_BASE } from "./api.config";
import { SessionService } from "./session.service";

export interface HabitoRegistro {
  id?: number;
  data: string;
  criadoEm?: string;
}

export interface Habito {
  id?: number;
  nome: string;
  descricao?: string;
  frequencia: "DIARIA" | "SEMANAL";
  metaSemanal: number;
  cor?: string;
  ativo?: boolean;
  criadoEm?: string;
  registros?: HabitoRegistro[];
}

/** Espelha exatamente os endpoints de `/api/habitos`; o usuário vem da sessão, nunca do input. */
@Injectable({ providedIn: "root" })
export class HabitoService {
  private readonly apiUrl = `${API_BASE}/habitos`;

  constructor(
    private http: HttpClient,
    private session: SessionService,
  ) {}

  private usuarioId(): number {
    const id = this.session.usuario()?.id;
    if (!id) throw new Error("Usuário não autenticado.");
    return id;
  }

  listar(): Observable<Habito[]> {
    return this.http.get<Habito[]>(`${this.apiUrl}?usuarioId=${this.usuarioId()}`);
  }

  criar(habito: Habito): Observable<Habito> {
    return this.http.post<Habito>(`${this.apiUrl}?usuarioId=${this.usuarioId()}`, habito);
  }

  atualizar(id: number, habito: Habito): Observable<Habito> {
    return this.http.put<Habito>(`${this.apiUrl}/${id}?usuarioId=${this.usuarioId()}`, habito);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}?usuarioId=${this.usuarioId()}`);
  }

  registrar(id: number, data: string): Observable<Habito> {
    return this.http.post<Habito>(`${this.apiUrl}/${id}/registros?usuarioId=${this.usuarioId()}`,
      { data });
  }

  removerRegistro(id: number, data: string): Observable<Habito> {
    return this.http.delete<Habito>(
      `${this.apiUrl}/${id}/registros?usuarioId=${this.usuarioId()}&data=${encodeURIComponent(data)}`);
  }
}
