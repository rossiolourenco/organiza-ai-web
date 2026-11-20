import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";

import { Categoria, CategoriaService } from "../../services/categoria.service";

@Component({
  selector: "app-categorias",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./categorias.component.html",
  styleUrl: "./categorias.component.css",
})
export class CategoriasComponent implements OnInit {
  categorias: Categoria[] = [];
  carregando = true;
  salvando = false;
  erro: string | null = null;
  feedback: string | null = null;
  editandoId: number | null = null;
  confirmandoExclusao: number | null = null;

  nome = "";
  cor = "#4F46E5";

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.categoriaService.listar().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.carregando = false;
      },
      error: () => {
        this.erro = "Não foi possível carregar as categorias.";
        this.carregando = false;
      },
    });
  }

  salvar(): void {
    if (this.salvando) return;
    const nome = this.nome.trim();
    if (!nome) {
      this.erro = "Informe o nome da categoria.";
      return;
    }
    this.salvando = true;
    this.erro = null;
    const payload: Categoria = { nome, cor: this.cor };
    const acao = this.editandoId
      ? this.categoriaService.atualizar(this.editandoId, payload)
      : this.categoriaService.criar(payload);
    acao.subscribe({
      next: () => {
        this.salvando = false;
        this.feedback = this.editandoId ? "Categoria atualizada." : "Categoria criada.";
        this.cancelar();
        this.carregar();
      },
      error: () => {
        this.salvando = false;
        this.erro = "Não foi possível salvar a categoria.";
      },
    });
  }

  editar(categoria: Categoria): void {
    this.editandoId = categoria.id ?? null;
    this.nome = categoria.nome;
    this.cor = categoria.cor || "#4F46E5";
    this.feedback = null;
  }

  cancelar(): void {
    this.editandoId = null;
    this.nome = "";
    this.cor = "#4F46E5";
  }

  pedirExclusao(categoria: Categoria): void {
    this.confirmandoExclusao = categoria.id ?? null;
  }

  cancelarExclusao(): void {
    this.confirmandoExclusao = null;
  }

  excluir(categoria: Categoria): void {
    if (!categoria.id) return;
    this.categoriaService.excluir(categoria.id).subscribe({
      next: () => {
        this.confirmandoExclusao = null;
        this.feedback = "Categoria removida. As tarefas foram mantidas sem categoria.";
        this.carregar();
      },
      error: () => {
        this.erro = "Não foi possível remover a categoria.";
      },
    });
  }
}
