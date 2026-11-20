import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { RouterLink } from "@angular/router";

import { Tarefa, TarefaService } from "../../services/tarefa.service";
import { SessionService } from "../../services/session.service";
import {
  dayKey,
  focusMinutes,
  nextBestAction,
  todayProgress,
  weeklyCompleted,
} from "../../services/productivity";

@Component({
  selector: "app-dashboard",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./dashboard.component.html",
  styleUrl: "./dashboard.component.css",
})
export class DashboardComponent implements OnInit {
  tarefas: Tarefa[] = [];
  carregando = true;
  erro: string | null = null;
  concluindo: number | null = null;

  nome = "você";
  papelLabel = "Usuário";
  readonly hojeKey = dayKey();
  hoje = "";
  focoHoje = 0;

  constructor(
    private tarefasService: TarefaService,
    private session: SessionService,
  ) {}

  ngOnInit(): void {
    const usuario = this.session.usuario();
    this.nome = usuario?.nome?.split(" ")[0] || "você";
    this.papelLabel =
      usuario?.papel === "SUPER_ADMIN" ? "Super admin" : usuario?.papel === "ADMIN" ? "Admin" : "Usuário";
    this.hoje = new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "numeric", month: "long" })
      .format(new Date());
    this.hoje = this.hoje.charAt(0).toUpperCase() + this.hoje.slice(1);
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.focoHoje = focusMinutes(this.session.usuario()?.id ?? 0);
    this.tarefasService.listarTodas().subscribe({
      next: (tarefas) => {
        this.tarefas = tarefas;
        this.carregando = false;
      },
      error: () => {
        this.erro = "Não foi possível carregar seu painel. Tente novamente.";
        this.carregando = false;
      },
    });
  }

  get abertas(): number {
    return this.tarefas.filter((t) => t.status !== "CONCLUIDA").length;
  }

  get concluidas(): number {
    return this.tarefas.filter((t) => t.status === "CONCLUIDA").length;
  }

  get atrasadas(): number {
    return this.tarefas.filter(
      (t) => t.status !== "CONCLUIDA" && !!t.dataPrazo && t.dataPrazo < this.hojeKey,
    ).length;
  }

  get progresso() {
    return todayProgress(this.tarefas, this.hojeKey);
  }

  get proxima() {
    return nextBestAction(this.tarefas, this.hojeKey);
  }

  get hojeTarefas(): Tarefa[] {
    return this.tarefas
      .filter((t) => t.dataPrazo === this.hojeKey)
      .sort((a, b) => Number(a.status === "CONCLUIDA") - Number(b.status === "CONCLUIDA"))
      .slice(0, 4);
  }

  get semana() {
    return weeklyCompleted(this.tarefas, this.hojeKey);
  }

  get maxSemana(): number {
    return Math.max(1, ...this.semana.map((d) => d.total));
  }

  concluir(tarefa: Tarefa): void {
    if (!tarefa.id || this.concluindo) return;
    this.concluindo = tarefa.id;
    this.tarefasService.concluir(tarefa.id).subscribe({
      next: () => {
        this.concluindo = null;
        this.carregar();
      },
      error: () => {
        this.concluindo = null;
        this.erro = "Não foi possível concluir a tarefa.";
      },
    });
  }
}
