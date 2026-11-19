import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { TarefaService } from "../../services/tarefa.service";
import { Categoria, CategoriaService } from "../../services/categoria.service";
import { SessionService } from "../../services/session.service";
import { ThemeService } from "../../services/theme.service";

@Component({
  selector: "app-tarefa-form",
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./tarefa-form.component.html",
  styleUrl: "./tarefa-form.component.css",
})
export class TarefaFormComponent implements OnInit {

  titulo = "";
  descricao = "";
  prioridade = "MEDIA";
  dataPrazo = "";

  categoriaId: number | null = null;
  categorias: Categoria[] = [];
  novaCategoria = "";
  salvandoCategoria = false;
  excluindoCategoriaId: number | null = null;

  tarefaId?: number;
  statusTarefa = "PENDENTE";

  salvando = false;
  carregando = false;
  erro: string | null = null;
  erroCategoria: string | null = null;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private tarefaService: TarefaService,
    private categoriaService: CategoriaService,
    private session: SessionService,
    public theme: ThemeService,
  ) {}

  ngOnInit(): void {
    this.carregarCategorias();

    const id = this.route.snapshot.paramMap.get("id");
    if (id) {
      this.tarefaId = Number(id);
      this.carregando = true;
      this.tarefaService.buscarPorId(this.tarefaId).subscribe({
        next: tarefa => {
          this.titulo = tarefa.titulo;
          this.descricao = tarefa.descricao || "";
          this.prioridade = tarefa.prioridade;
          this.dataPrazo = tarefa.dataPrazo || "";
          this.statusTarefa = tarefa.status;
          this.categoriaId = tarefa.categoria?.id ?? null;
          this.carregando = false;
        },
        error: erro => {
          console.error("Erro ao carregar tarefa:", erro);
          this.carregando = false;
          this.erro = "Não foi possível carregar esta tarefa. Volte para a lista e tente novamente.";
        },
      });
    }
  }

  carregarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: categorias => { this.categorias = categorias; },
      error: erro => {
        console.error("Erro ao carregar categorias:", erro);
        this.erroCategoria = "Não foi possível carregar as categorias.";
      },
    });
  }

  cadastrarCategoria(): void {
    const nome = this.novaCategoria.trim();
    if (!nome || this.salvandoCategoria) return;
    this.salvandoCategoria = true;
    this.erroCategoria = null;
    this.categoriaService.criar({ nome }).subscribe({
      next: categoria => {
        this.novaCategoria = "";
        this.salvandoCategoria = false;
        this.categoriaId = categoria.id ?? null;
        this.carregarCategorias();
      },
      error: erro => {
        this.salvandoCategoria = false;
        console.error("Erro ao cadastrar categoria:", erro);
        this.erroCategoria = "Não foi possível cadastrar a categoria.";
      },
    });
  }

  excluirCategoria(categoria: Categoria): void {
    if (!categoria.id || this.excluindoCategoriaId !== null) return;
    if (!confirm(`Excluir a categoria "${categoria.nome}"? As tarefas serão mantidas sem essa categoria.`)) return;
    this.excluindoCategoriaId = categoria.id;
    this.erroCategoria = null;
    this.categoriaService.excluir(categoria.id).subscribe({
      next: () => {
        if (this.categoriaId === categoria.id) this.categoriaId = null;
        this.excluindoCategoriaId = null;
        this.carregarCategorias();
      },
      error: erro => {
        this.excluindoCategoriaId = null;
        console.error("Erro ao excluir categoria:", erro);
        this.erroCategoria = "Não foi possível excluir a categoria.";
      },
    });
  }

  salvar(): void {
    this.erro = null;
    if (!this.titulo.trim()) {
      this.erro = "Informe um título para a tarefa.";
      return;
    }
    if (this.salvando) return;

    const usuario = this.session.usuario();
    if (!usuario) {
      this.erro = "Sua sessão expirou. Entre novamente.";
      void this.router.navigate(["/login"]);
      return;
    }

    const tarefa = {
      titulo: this.titulo.trim(),
      descricao: this.descricao,
      prioridade: this.prioridade,
      status: this.tarefaId ? this.statusTarefa : "PENDENTE",
      dataPrazo: this.dataPrazo || undefined,
      usuario: { id: usuario.id },
      categoria: this.categoriaId ? { id: Number(this.categoriaId) } : undefined,
    };

    this.salvando = true;

    if (this.tarefaId) {
      this.tarefaService.atualizar(this.tarefaId, tarefa).subscribe({
        next: () => this.concluir("Tarefa atualizada com sucesso."),
        error: erro => this.falhar(erro, "Não foi possível atualizar a tarefa."),
      });
      return;
    }

    this.tarefaService.criar(tarefa).subscribe({
      next: () => this.concluir("Tarefa criada com sucesso."),
      error: erro => this.falhar(erro, "Não foi possível criar a tarefa."),
    });
  }

  private concluir(mensagem: string): void {
    this.salvando = false;
    sessionStorage.setItem("organiza-flash", mensagem);
    void this.router.navigate(["/home"]);
  }

  private falhar(erro: unknown, mensagem: string): void {
    console.error(mensagem, erro);
    this.salvando = false;
    this.erro = mensagem;
  }

  cancelar(): void {
    void this.router.navigate(["/home"]);
  }

  sair(): void {
    this.session.limpar();
    void this.router.navigate(["/login"]);
  }
}
