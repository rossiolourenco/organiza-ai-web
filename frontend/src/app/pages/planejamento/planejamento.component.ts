import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { RouterLink } from "@angular/router";

import { Tarefa, TarefaService } from "../../services/tarefa.service";
import { Planejamento, PlanejamentoItem, ProdutividadeService } from "../../services/produtividade.service";
import { addDays, dayKey } from "../../services/productivity";

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
  salvando = false;
  erro: string | null = null;
  feedback: string | null = null;
  plano: Planejamento | null = null;
  selecionadas = new Set<number>();
  readonly hoje = dayKey();
  readonly limite = addDays(dayKey(), 7);
  readonly disponibilidade = 480;

  constructor(
    private tarefasService: TarefaService,
    private produtividade: ProdutividadeService,
  ) {}

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
        this.carregarPlano();
      },
      error: () => {
        this.erro = "Não foi possível carregar as tarefas.";
        this.carregando = false;
      },
    });
  }

  private carregarPlano(): void {
    this.produtividade.obterPlanejamento(this.hoje).subscribe({
      next: (plano) => {
        this.plano = plano;
        this.selecionadas = new Set((plano.itens || []).map((item) => item.tarefaId));
      },
      error: () => {
        this.erro = "Não foi possível carregar o plano salvo.";
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
    const ids = new Set((this.plano?.itens || []).map((item) => item.tarefaId));
    return this.tarefas.filter((t) => t.id && ids.has(t.id));
  }

  get minutosPlano(): number {
    return (this.plano?.itens || []).reduce((total, item) => total + item.minutosEstimados, 0);
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
    if (this.salvando) return;
    this.salvando = true;
    this.erro = null;
    this.feedback = null;

    const itens: PlanejamentoItem[] = this.candidatas
      .filter((t) => t.id && this.selecionadas.has(t.id))
      .map((t, ordem) => ({ tarefaId: t.id!, ordem, minutosEstimados: this.duracao(t) }));

    this.produtividade
      .salvarPlanejamento({ data: this.hoje, itens, versao: this.plano?.versao ?? 0 })
      .subscribe({
        next: (plano) => {
          this.salvando = false;
          this.plano = plano;
          this.selecionadas = new Set(plano.itens.map((item) => item.tarefaId));
          this.feedback = `Plano de hoje salvo com ${itens.length} tarefa(s) e ${this.minutosSelecionados} min estimados.`;
        },
        error: (erro) => {
          this.salvando = false;
          if (erro.status === 409) {
            this.erro = "O plano foi alterado em outro dispositivo. Recarregue e tente novamente.";
            this.carregarPlano();
          } else {
            this.erro = "Não foi possível salvar o plano.";
          }
        },
      });
  }

  limpar(): void {
    if (this.salvando) return;
    this.salvando = true;
    this.erro = null;
    this.feedback = null;
    this.produtividade
      .salvarPlanejamento({ data: this.hoje, itens: [], versao: this.plano?.versao ?? 0 })
      .subscribe({
        next: (plano) => {
          this.salvando = false;
          this.plano = plano;
          this.selecionadas.clear();
          this.feedback = "Plano de hoje removido.";
        },
        error: (erro) => {
          this.salvando = false;
          this.erro = erro.status === 409
            ? "O plano foi alterado em outro dispositivo. Recarregue e tente novamente."
            : "Não foi possível remover o plano.";
          this.carregarPlano();
        },
      });
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
