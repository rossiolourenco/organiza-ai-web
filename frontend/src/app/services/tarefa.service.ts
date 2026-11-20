import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { API_BASE } from "./api.config";

export interface Tarefa {
  id?: number;
  titulo: string;
  descricao?: string;
  prioridade: string;
  status: string;
  dataPrazo?: string;
  usuario?: {
    id: number;
    nome?: string;
    email?: string;
  };
  categoria?: {
    id: number;
    nome?: string;
  };
  dataCriacao?: string;
  dataAtualizacao?: string;
  /** Payload livre usado pelo Mobile: checklist, recorrência, duração e etiquetas. */
  detalhes?: {
    subtarefas?: { id: string; titulo: string; concluida: boolean }[];
    recorrencia?: string;
    fusoHorario?: string;
    horario?: string;
    duracaoEstimada?: number;
    favorita?: boolean;
    notas?: string;
    etiquetas?: string[];
  } | null;
  /** Instantes UTC de cada conclusão (histórico). */
  conclusoes?: string[];
  proximaGerada?: boolean;
}

@Injectable({
  providedIn: "root",
})
export class TarefaService {
  private readonly apiUrl = `${API_BASE}/tarefas`;

  constructor(private http: HttpClient) {}

  listarTodas(): Observable<Tarefa[]> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.get<Tarefa[]>(`${this.apiUrl}?usuarioId=${usuario.id}`);
  }
  listarPorStatus(status: string): Observable<Tarefa[]> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.get<Tarefa[]>(
      `${this.apiUrl}/status/${status}?usuarioId=${usuario.id}`,
    );
  }

  listarPorPrioridade(prioridade: string): Observable<Tarefa[]> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.get<Tarefa[]>(
      `${this.apiUrl}/prioridade/${prioridade}?usuarioId=${usuario.id}`,
    );
  }
  buscarPorId(id: number): Observable<Tarefa> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.get<Tarefa>(
      `${this.apiUrl}/${id}?usuarioId=${usuario.id}`,
    );
  }
  criar(tarefa: Tarefa): Observable<Tarefa> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.post<Tarefa>(
      `${this.apiUrl}?usuarioId=${usuario.id}`,
      tarefa,
    );
  }
  atualizar(id: number, tarefa: Tarefa): Observable<Tarefa> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.put<Tarefa>(
      `${this.apiUrl}/${id}?usuarioId=${usuario.id}`,
      tarefa,
    );
  }
  concluir(id: number): Observable<Tarefa> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.patch<Tarefa>(
      `${this.apiUrl}/${id}/concluir?usuarioId=${usuario.id}`,
      {},
    );
  }
  listarLixeira(): Observable<Tarefa[]> {
    const usuarioSalvo = localStorage.getItem("usuario");
    if (!usuarioSalvo) throw new Error("Usuário não autenticado.");
    const usuario = JSON.parse(usuarioSalvo);
    return this.http.get<Tarefa[]>(`${this.apiUrl}/lixeira?usuarioId=${usuario.id}`);
  }

  restaurar(id: number): Observable<Tarefa> {
    const usuarioSalvo = localStorage.getItem("usuario");
    if (!usuarioSalvo) throw new Error("Usuário não autenticado.");
    const usuario = JSON.parse(usuarioSalvo);
    return this.http.post<Tarefa>(`${this.apiUrl}/${id}/restaurar?usuarioId=${usuario.id}`, {});
  }

  excluirDefinitivo(id: number): Observable<void> {
    const usuarioSalvo = localStorage.getItem("usuario");
    if (!usuarioSalvo) throw new Error("Usuário não autenticado.");
    const usuario = JSON.parse(usuarioSalvo);
    return this.http.delete<void>(`${this.apiUrl}/${id}/definitivo?usuarioId=${usuario.id}`);
  }

  excluir(id: number): Observable<void> {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      throw new Error("Usuário não autenticado.");
    }

    const usuario = JSON.parse(usuarioSalvo);

    return this.http.delete<void>(
      `${this.apiUrl}/${id}?usuarioId=${usuario.id}`,
    );
  }
}
