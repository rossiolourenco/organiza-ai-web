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
      next: (tarefas) => {
        this.tarefas = tarefas;
        console.log("Tarefas carregadas:", tarefas);
      },
      error: (erro) => {
        console.error("Erro ao carregar tarefas:", erro);
      },
    });
  }
  filtrarPorStatus(): void {
    this.aplicarFiltros();
  }

  filtrarPorPrioridade(): void {
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    if (this.statusSelecionado) {
      this.tarefaService.listarPorStatus(this.statusSelecionado).subscribe({
        next: (tarefas) => {
          this.tarefas = this.prioridadeSelecionada
            ? tarefas.filter(
                (tarefa) => tarefa.prioridade === this.prioridadeSelecionada,
              )
            : tarefas;
        },
        error: (erro) => {
          console.error("Erro ao filtrar tarefas:", erro);
        },
      });

      return;
    }

    if (this.prioridadeSelecionada) {
      this.tarefaService
        .listarPorPrioridade(this.prioridadeSelecionada)
        .subscribe({
          next: (tarefas) => {
            this.tarefas = tarefas;
          },
          error: (erro) => {
            console.error("Erro ao filtrar tarefas:", erro);
          },
        });

      return;
    }

    this.carregarTarefas();
  }
  concluirTarefa(id: number): void {
    this.tarefaService.concluir(id).subscribe({
      next: () => {
        this.aplicarFiltros();
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
        this.aplicarFiltros();
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
