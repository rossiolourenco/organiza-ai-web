import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

import { API_BASE } from "./api.config";

export interface PlanejamentoItem {
  tarefaId: number;
  ordem: number;
  minutosEstimados: number;
}

export interface Planejamento {
  id: number | null;
  data: string;
  itens: PlanejamentoItem[];
  versao: number;
  atualizadoEm?: string;
}

export interface SessaoFoco {
  id: number;
  minutos: number;
  data: string;
  iniciadaEm: string;
  tarefaId: number | null;
}

export interface FocoResumo {
  minutos: number;
  sessoes: number;
}

/** Planejamento e sessões de foco são dados de negócio centralizados no backend. */
@Injectable({ providedIn: "root" })
export class ProdutividadeService {
  private readonly apiUrl = `${API_BASE}/produtividade`;

  constructor(private http: HttpClient) {}

  obterPlanejamento(data: string): Observable<Planejamento> {
    return this.http.get<Planejamento>(`${this.apiUrl}/planejamento?data=${data}`);
  }

  salvarPlanejamento(req: { data: string; itens: PlanejamentoItem[]; versao: number }): Observable<Planejamento> {
    return this.http.put<Planejamento>(`${this.apiUrl}/planejamento`, req);
  }

  sessoesFoco(data: string): Observable<SessaoFoco[]> {
    return this.http.get<SessaoFoco[]>(`${this.apiUrl}/foco?data=${data}`);
  }

  registrarFoco(req: { minutos: number; tarefaId: number | null; data: string }): Observable<SessaoFoco> {
    return this.http.post<SessaoFoco>(`${this.apiUrl}/foco`, req);
  }

  resumoFoco(inicio: string, fim: string): Observable<FocoResumo> {
    return this.http.get<FocoResumo>(`${this.apiUrl}/foco/resumo?inicio=${inicio}&fim=${fim}`);
  }
}
