import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { RouterLink } from "@angular/router";

import { Tarefa, TarefaService } from "../../services/tarefa.service";
import { SessionService } from "../../services/session.service";
import {
  DayPlan,
  addDays,
  clearPlan,
  dayKey,
  loadPlan,
  savePlan,
} from "../../services/productivity";

@Component({
  selector: "app-planejamento",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./planejamento.component.html",
  styleUrl: "./planejamento.component.css",
})
export class PlanejamentoComponent implements OnInit {
  tarefas: Tarefa[] = [];
  carregando = true;
  erro: string | null = null;
  feedback: string | null = null;
  plano: DayPlan | null = null;
  selecionadas = new Set<number>();
  readonly hoje = dayKey();
  readonly limite = addDays(dayKey(), 7);
  readonly disponibilidade = 480; // 8h por dia, mesma referência do Mobile

  constructor(
    private tarefasService: TarefaService,
    private session: SessionService,
  ) {}

  ngOnInit(): void {
    this.plano = loadPlan(this.usuarioId());
    this.plano?.tarefaIds.forEach((id) => this.selecionadas.add(id));
    this.carregar();
  }

  private usuarioId(): number {
    return this.session.usuario()?.id ?? 0;
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
        this.erro = "Não foi possível carregar as tarefas.";
        this.carregando = false;
      },
    });
  }

  get candidatas(): Tarefa[] {
    return this.tarefas
      .filter((t) => t.status !== "CONCLUIDA")
      .filter((t) => !t.dataPrazo || (t.dataPrazo >= this.hoje && t.dataPrazo <= this.limite))
      .sort((a, b) => (a.dataPrazo || "9999").localeCompare(b.dataPrazo || "9999"));
  }

  get minutosSelecionados(): number {
    return this.candidatas
      .filter((t) => t.id && this.selecionadas.has(t.id))
      .reduce((total, t) => total + this.duracao(t), 0);
  }

  get planoAtivo(): Tarefa[] {
    const ids = this.plano?.tarefaIds ?? [];
    return this.tarefas.filter((t) => t.id && ids.includes(t.id));
  }

  duracao(tarefa: Tarefa): number {
    const estimativa = tarefa.detalhes?.duracaoEstimada;
    return estimativa && estimativa > 0 ? estimativa : 30;
  }

  alternar(tarefa: Tarefa): void {
    if (!tarefa.id) return;
    if (this.selecionadas.has(tarefa.id)) this.selecionadas.delete(tarefa.id);
    else this.selecionadas.add(tarefa.id);
  }

  aceitar(): void {
    const ids = Array.from(this.selecionadas);
    const plano: DayPlan = {
      data: this.hoje,
      tarefaIds: ids,
      minutos: this.minutosSelecionados,
      aceitoEm: new Date().toISOString(),
    };
    savePlan(this.usuarioId(), plano);
    this.plano = plano;
    this.feedback = `Plano de hoje salvo com ${ids.length} tarefa(s) e ${plano.minutos} min estimados.`;
  }

  limpar(): void {
    clearPlan(this.usuarioId(), this.hoje);
    this.plano = null;
    this.selecionadas.clear();
    this.feedback = "Plano de hoje removido.";
  }

  concluir(tarefa: Tarefa): void {
    if (!tarefa.id) return;
    this.tarefasService.concluir(tarefa.id).subscribe({
      next: () => {
        this.feedback = "Tarefa concluída.";
        this.carregar();
      },
      error: () => {
        this.erro = "Não foi possível concluir a tarefa.";
      },
    });
  }

  formatarMinutos(minutos: number): string {
    const horas = Math.floor(minutos / 60);
    const resto = minutos % 60;
    if (!horas) return `${resto} min`;
    return resto ? `${horas}h ${resto}min` : `${horas}h`;
  }
}
