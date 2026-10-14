import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { TarefaService } from "../../services/tarefa.service";
import {
  Categoria,
  CategoriaService,
} from "../../services/categoria.service";

@Component({
  selector: "app-tarefa-form",
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./tarefa-form.component.html",
  styleUrl: "./tarefa-form.component.css",
})
export class TarefaFormComponent implements OnInit {

  titulo: string = "";
  descricao: string = "";
  prioridade: string = "MEDIA";
  dataPrazo: string = "";

  categoriaId: number | null = null;
  categorias: Categoria[] = [];
  novaCategoria = "";
  salvandoCategoria = false;
  excluindoCategoriaId: number | null = null;

  tarefaId?: number;
  statusTarefa: string = "PENDENTE";

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private tarefaService: TarefaService,
    private categoriaService: CategoriaService,
  ) {}

  ngOnInit(): void {
    this.carregarCategorias();

    const id = this.route.snapshot.paramMap.get("id");

    if (id) {
      this.tarefaId = Number(id);

      this.tarefaService.buscarPorId(this.tarefaId).subscribe({
        next: (tarefa) => {
          this.titulo = tarefa.titulo;
          this.descricao = tarefa.descricao || "";
          this.prioridade = tarefa.prioridade;
          this.dataPrazo = tarefa.dataPrazo || "";
          this.statusTarefa = tarefa.status;
          this.categoriaId = tarefa.categoria?.id ?? null;
        },
        error: (erro) => {
          console.error("Erro ao carregar tarefa:", erro);
          alert("Não foi possível carregar a tarefa.");
          this.router.navigate(["/home"]);
        },
      });
    }
  }

  carregarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
      },
      error: (erro) => {
        console.error("Erro ao carregar categorias:", erro);
      },
    });
  }

  cadastrarCategoria(): void {
    const nome = this.novaCategoria.trim();
    if (!nome || this.salvandoCategoria) return;
    this.salvandoCategoria = true;
    this.categoriaService.criar({ nome }).subscribe({
      next: categoria => {
        this.novaCategoria = "";
        this.salvandoCategoria = false;
        this.carregarCategorias();
        this.categoriaId = categoria.id ?? null;
      },
      error: erro => {
        this.salvandoCategoria = false;
        console.error("Erro ao cadastrar categoria:", erro);
        alert("Não foi possível cadastrar a categoria.");
      },
    });
  }

  excluirCategoria(categoria: Categoria): void {
    if (!categoria.id || this.excluindoCategoriaId !== null) return;
    if (!confirm(`Excluir a categoria "${categoria.nome}"? As tarefas serão mantidas sem essa categoria.`)) return;
    this.excluindoCategoriaId = categoria.id;
    this.categoriaService.excluir(categoria.id).subscribe({
      next: () => {
        if (this.categoriaId === categoria.id) this.categoriaId = null;
        this.excluindoCategoriaId = null;
        this.carregarCategorias();
      },
      error: erro => {
        this.excluindoCategoriaId = null;
        console.error("Erro ao excluir categoria:", erro);
        alert("Não foi possível excluir a categoria.");
      },
    });
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
      status: this.tarefaId ? this.statusTarefa : "PENDENTE",
      dataPrazo: this.dataPrazo || undefined,
      usuario: {
        id: usuario.id,
      },
      categoria: this.categoriaId
        ? {
            id: Number(this.categoriaId),
          }
        : undefined,
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
