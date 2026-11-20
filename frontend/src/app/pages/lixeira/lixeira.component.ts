import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { RouterLink } from "@angular/router";

import { Tarefa, TarefaService } from "../../services/tarefa.service";

@Component({
  selector: "app-lixeira",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./lixeira.component.html",
  styleUrl: "./lixeira.component.css",
})
export class LixeiraComponent implements OnInit {
  tarefas: Tarefa[] = [];
  carregando = true;
  erro: string | null = null;
  feedback: string | null = null;
  processando: number | null = null;
  confirmandoExclusao: number | null = null;

  constructor(private tarefaService: TarefaService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.tarefaService.listarLixeira().subscribe({
      next: (tarefas) => {
        this.tarefas = tarefas;
        this.carregando = false;
      },
      error: () => {
        this.erro = "Não foi possível carregar a lixeira.";
        this.carregando = false;
      },
    });
  }

  restaurar(tarefa: Tarefa): void {
    if (!tarefa.id || this.processando) return;
    this.processando = tarefa.id;
    this.tarefaService.restaurar(tarefa.id).subscribe({
      next: () => {
        this.processando = null;
        this.feedback = "Tarefa restaurada. Ela voltou para a lista.";
        this.carregar();
      },
      error: () => {
        this.processando = null;
        this.erro = "Não foi possível restaurar a tarefa.";
      },
    });
  }

  pedirExclusao(tarefa: Tarefa): void {
    this.confirmandoExclusao = tarefa.id ?? null;
  }

  cancelarExclusao(): void {
    this.confirmandoExclusao = null;
  }

  excluirDefinitivo(tarefa: Tarefa): void {
    if (!tarefa.id || this.processando) return;
    this.processando = tarefa.id;
    this.tarefaService.excluirDefinitivo(tarefa.id).subscribe({
      next: () => {
        this.processando = null;
        this.confirmandoExclusao = null;
        this.feedback = "Tarefa excluída definitivamente.";
        this.carregar();
      },
      error: () => {
        this.processando = null;
        this.erro = "Não foi possível excluir a tarefa.";
      },
    });
  }
}
