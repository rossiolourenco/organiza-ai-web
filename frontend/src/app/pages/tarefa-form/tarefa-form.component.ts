import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { TarefaService } from "../../services/tarefa.service";

@Component({
  selector: "app-tarefa-form",
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./tarefa-form.component.html",
  styleUrl: "./tarefa-form.component.css",
})
export class TarefaFormComponent {
  titulo: string = "";
  descricao: string = "";
  prioridade: string = "MEDIA";
  dataPrazo: string = "";
  tarefaId?: number;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private tarefaService: TarefaService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get("id");

    if (id) {
      this.tarefaId = Number(id);

      this.tarefaService.buscarPorId(this.tarefaId).subscribe({
        next: (tarefa) => {
          this.titulo = tarefa.titulo;
          this.descricao = tarefa.descricao || "";
          this.prioridade = tarefa.prioridade;
          this.dataPrazo = tarefa.dataPrazo || "";
        },
        error: (erro) => {
          console.error("Erro ao carregar tarefa:", erro);
          alert("Não foi possível carregar a tarefa.");
          this.router.navigate(["/home"]);
        },
      });
    }
  }

  salvar(): void {
    const usuarioSalvo = localStorage.getItem("usuario");

    if (!usuarioSalvo) {
      alert("Usuário não autenticado.");
      this.router.navigate(["/login"]);
      return;
    }

    const usuario = JSON.parse(usuarioSalvo);

    const tarefa = {
      titulo: this.titulo,
      descricao: this.descricao,
      prioridade: this.prioridade,
      status: "PENDENTE",
      dataPrazo: this.dataPrazo || undefined,
      usuario: {
        id: usuario.id,
      },
    };

    if (this.tarefaId) {
      this.tarefaService.atualizar(this.tarefaId, tarefa).subscribe({
        next: () => {
          alert("Tarefa atualizada com sucesso!");
          this.router.navigate(["/home"]);
        },
        error: (erro) => {
          console.error("Erro ao atualizar tarefa:", erro);
          alert("Não foi possível atualizar a tarefa.");
        },
      });

      return;
    }

    this.tarefaService.criar(tarefa).subscribe({
      next: () => {
        alert("Tarefa criada com sucesso!");
        this.router.navigate(["/home"]);
      },
      error: (erro) => {
        console.error("Erro ao criar tarefa:", erro);
        alert("Não foi possível criar a tarefa.");
      },
    });
  }

  cancelar(): void {
    this.router.navigate(["/home"]);
  }
}
