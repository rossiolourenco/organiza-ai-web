import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { RouterLink } from "@angular/router";

import { Tarefa, TarefaService } from "../../services/tarefa.service";
import {
  completionRate,
  dayKey,
  habitStreak,
  weeklyCompleted,
} from "../../services/productivity";

@Component({
  selector: "app-revisao",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./revisao.component.html",
  styleUrl: "./revisao.component.css",
})
export class RevisaoComponent implements OnInit {
  tarefas: Tarefa[] = [];
  carregando = true;
  erro: string | null = null;
  readonly hoje = dayKey();

  constructor(private tarefasService: TarefaService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.tarefasService.listarTodas().subscribe({
      next: (tarefas) => {
        this.tarefas = tarefas;
        this.carregando = false;
      },
      error: () => {
        this.erro = "Não foi possível carregar a revisão.";
        this.carregando = false;
      },
    });
  }

  get semana() {
    return weeklyCompleted(this.tarefas, this.hoje);
  }

  get totalSemana(): number {
    return this.semana.reduce((total, dia) => total + dia.total, 0);
  }

  get taxa(): number {
    return completionRate(this.tarefas);
  }

  get sequencia(): number {
    return habitStreak(this.diasComConclusao, this.hoje);
  }

  get atrasadas(): Tarefa[] {
    return this.tarefas.filter(
      (t) => t.status !== "CONCLUIDA" && !!t.dataPrazo && t.dataPrazo < this.hoje,
    );
  }

  get categorias(): { nome: string; total: number; concluidas: number }[] {
    const mapa = new Map<string, { nome: string; total: number; concluidas: number }>();
    for (const tarefa of this.tarefas) {
      const nome = tarefa.categoria?.nome || "Sem categoria";
      const atual = mapa.get(nome) || { nome, total: 0, concluidas: 0 };
      atual.total++;
      if (tarefa.status === "CONCLUIDA") atual.concluidas++;
      mapa.set(nome, atual);
    }
    return Array.from(mapa.values()).sort((a, b) => b.total - a.total);
  }

  private get diasComConclusao(): string[] {
    return this.tarefas.flatMap((t) => (t.conclusoes || []).map((iso) => dayKey(new Date(iso))));
  }

  get criadasNaSemana(): number {
    const inicio = this.semana[0]?.data;
    if (!inicio) return 0;
    return this.tarefas.filter((t) => !!t.dataCriacao && dayKey(new Date(t.dataCriacao)) >= inicio).length;
  }

  get melhorDia(): { data: string; total: number } | null {
    if (!this.semana.length) return null;
    return this.semana.reduce((melhor, dia) => (dia.total > melhor.total ? dia : melhor), this.semana[0]);
  }
}
