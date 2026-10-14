import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Tarefa, TarefaService } from "../../services/tarefa.service";
import { Router, RouterLink } from "@angular/router";
import { FormsModule } from "@angular/forms";

@Component({
  selector: "app-home",
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: "./home.component.html",
  styleUrl: "./home.component.css",
})
export class HomeComponent implements OnInit {
  tarefas: Tarefa[] = [];
  todasTarefas: Tarefa[] = [];
  dataInicial = "";
  dataFinal = "";
  erroPeriodo = "";

  statusSelecionado: string = "";
  prioridadeSelecionada: string = "";

  constructor(
    private tarefaService: TarefaService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.carregarTarefas();
  }

  carregarTarefas(): void {
    this.tarefaService.listarTodas().subscribe({
      next: tarefas => {
        this.todasTarefas = tarefas;
        this.carregarTarefas();
      },
      error: erro => {
        console.error("Erro ao carregar tarefas:", erro);
        alert("Não foi possível carregar as tarefas.");
      },
    });
  }

  filtrarPorStatus(): void { this.aplicarFiltros(); }
  filtrarPorPrioridade(): void { this.aplicarFiltros(); }

  aplicarFiltros(): void {
    this.erroPeriodo = this.dataInicial && this.dataFinal && this.dataInicial > this.dataFinal
      ? "A data inicial não pode ser posterior à data final."
      : "";
    if (this.erroPeriodo) { this.tarefas = []; return; }
    this.tarefas = this.todasTarefas.filter(tarefa =>
      (!this.statusSelecionado || tarefa.status === this.statusSelecionado) &&
      (!this.prioridadeSelecionada || tarefa.prioridade === this.prioridadeSelecionada) &&
      (!this.dataInicial || !!tarefa.dataPrazo && tarefa.dataPrazo >= this.dataInicial) &&
      (!this.dataFinal || !!tarefa.dataPrazo && tarefa.dataPrazo <= this.dataFinal)
    );
  }

  limparFiltros(): void {
    this.statusSelecionado = "";
    this.prioridadeSelecionada = "";
    this.dataInicial = "";
    this.dataFinal = "";
    this.aplicarFiltros();
  }

  concluirTarefa(id: number): void {
    this.tarefaService.concluir(id).subscribe({
      next: () => {
        this.carregarTarefas();
      },
      error: (erro) => {
        console.error("Erro ao concluir tarefa:", erro);
        alert("Não foi possível concluir a tarefa.");
      },
    });
  }
  excluirTarefa(id: number): void {
    const confirmar = confirm("Deseja realmente excluir esta tarefa?");

    if (!confirmar) {
      return;
    }

    this.tarefaService.excluir(id).subscribe({
      next: () => {
        this.carregarTarefas();
      },
      error: (erro) => {
        console.error("Erro ao excluir tarefa:", erro);
        alert("Não foi possível excluir a tarefa.");
      },
    });
  }
  sair(): void {
    localStorage.removeItem("usuario");
    this.router.navigate(["/login"]);
  }
}
