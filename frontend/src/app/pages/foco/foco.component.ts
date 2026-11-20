import { CommonModule } from "@angular/common";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";

import { Tarefa, TarefaService } from "../../services/tarefa.service";
import { SessaoFoco, ProdutividadeService } from "../../services/produtividade.service";
import { dayKey } from "../../services/productivity";

type Modo = "FOCO" | "PAUSA";

@Component({
  selector: "app-foco",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./foco.component.html",
  styleUrl: "./foco.component.css",
})
export class FocoComponent implements OnInit, OnDestroy {
  readonly focoSegundos = 25 * 60;
  readonly pausaSegundos = 5 * 60;

  modo: Modo = "FOCO";
  restante = this.focoSegundos;
  rodando = false;
  ciclos = 0;
  sessoes: SessaoFoco[] = [];
  tarefas: Tarefa[] = [];
  tarefaSelecionada: number | null = null;

  private timer: number | null = null;

  constructor(
    private tarefasService: TarefaService,
    private produtividade: ProdutividadeService,
  ) {}

  ngOnInit(): void {
    this.carregarSessoes();
    this.tarefasService.listarTodas().subscribe({
      next: (tarefas) => {
        this.tarefas = tarefas.filter((t) => t.status !== "CONCLUIDA");
      },
      error: () => {
        this.tarefas = [];
      },
    });
  }

  ngOnDestroy(): void {
    this.pararIntervalo();
  }

  get totalSegundos(): number {
    return this.modo === "FOCO" ? this.focoSegundos : this.pausaSegundos;
  }

  get progresso(): number {
    return Math.round(((this.totalSegundos - this.restante) / this.totalSegundos) * 100);
  }

  get tempoFormatado(): string {
    const minutos = Math.floor(this.restante / 60);
    const segundos = this.restante % 60;
    return `${`${minutos}`.padStart(2, "0")}:${`${segundos}`.padStart(2, "0")}`;
  }

  get minutosHoje(): number {
    return this.sessoes.reduce((total, s) => total + s.minutos, 0);
  }

  get tarefaAtual(): Tarefa | null {
    return this.tarefas.find((t) => t.id === this.tarefaSelecionada) || null;
  }

  iniciar(): void {
    if (this.rodando) return;
    this.rodando = true;
    this.timer = window.setInterval(() => this.tick(), 1000);
  }

  pausar(): void {
    this.rodando = false;
    this.pararIntervalo();
  }

  reiniciar(): void {
    this.pausar();
    this.restante = this.totalSegundos;
  }

  alternarModo(): void {
    this.pausar();
    this.modo = this.modo === "FOCO" ? "PAUSA" : "FOCO";
    this.restante = this.totalSegundos;
  }

  private tick(): void {
    if (this.restante > 1) {
      this.restante--;
      return;
    }
    this.restante = 0;
    this.pararIntervalo();
    this.rodando = false;
    this.finalizarCiclo();
  }

  private finalizarCiclo(): void {
    if (this.modo === "FOCO") {
      this.produtividade
        .registrarFoco({ minutos: 25, tarefaId: this.tarefaSelecionada, data: dayKey() })
        .subscribe({
          next: () => {
            this.ciclos++;
            this.carregarSessoes();
          },
          error: () => {
            this.ciclos++;
          },
        });
      this.modo = "PAUSA";
    } else {
      this.modo = "FOCO";
    }
    this.restante = this.totalSegundos;
  }

  private carregarSessoes(): void {
    this.produtividade.sessoesFoco(dayKey()).subscribe({
      next: (sessoes) => {
        this.sessoes = sessoes;
      },
      error: () => {
        this.sessoes = [];
      },
    });
  }

  private pararIntervalo(): void {
    if (this.timer !== null) {
      window.clearInterval(this.timer);
      this.timer = null;
    }
  }
}
