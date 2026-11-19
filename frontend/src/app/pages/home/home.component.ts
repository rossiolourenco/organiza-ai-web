import { Component, HostListener, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Tarefa, TarefaService } from "../../services/tarefa.service";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { SessionService } from "../../services/session.service";
import { ThemeService } from "../../services/theme.service";

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

  statusSelecionado = "";
  prioridadeSelecionada = "";

  carregando = true;
  erro: string | null = null;
  feedback: string | null = null;

  tarefaParaExcluir: Tarefa | null = null;
  excluindo = false;

  nome = "você";
  hoje = "";

  constructor(
    private tarefaService: TarefaService,
    private router: Router,
    private route: ActivatedRoute,
    private session: SessionService,
    public theme: ThemeService,
  ) {}

  ngOnInit(): void {
    this.nome = this.session.usuario()?.nome?.split(" ")[0] || "você";
    this.hoje = new Intl.DateTimeFormat("pt-BR", {
      weekday: "long",
      day: "numeric",
      month: "long",
    }).format(new Date());
    this.hoje = this.hoje.charAt(0).toUpperCase() + this.hoje.slice(1);

    const flash = sessionStorage.getItem("organiza-flash");
    if (flash) {
      sessionStorage.removeItem("organiza-flash");
      this.feedback = flash;
    }

    if (this.route.snapshot.queryParamMap.get("cadastro") === "1") {
      this.feedback = "Conta criada com sucesso. Bem-vindo ao Organiza Aí.";
      void this.router.navigate(["/home"], { replaceUrl: true });
    }

    this.carregarTarefas();
  }

  @HostListener("document:keydown.escape")
  fecharDialogo(): void {
    if (!this.excluindo) this.tarefaParaExcluir = null;
  }

  get totalTarefas(): number {
    return this.todasTarefas.length;
  }

  get totalPendentes(): number {
    return this.todasTarefas.filter(tarefa => tarefa.status === "PENDENTE").length;
  }

  get totalEmAndamento(): number {
    return this.todasTarefas.filter(tarefa => tarefa.status === "EM_ANDAMENTO").length;
  }

  get totalConcluidas(): number {
    return this.todasTarefas.filter(tarefa => tarefa.status === "CONCLUIDA").length;
  }

  get deveTrocarSenha(): boolean {
    return this.session.usuario()?.deveTrocarSenha === true;
  }

  get filtrosAtivos(): boolean {
    return !!(
      this.statusSelecionado ||
      this.prioridadeSelecionada ||
      this.dataInicial ||
      this.dataFinal
    );
  }

  carregarTarefas(): void {
    this.carregando = true;
    this.erro = null;
    this.tarefaService.listarTodas().subscribe({
      next: tarefas => {
        this.todasTarefas = tarefas;
        this.aplicarFiltros();
        this.carregando = false;
      },
      error: erro => {
        console.error("Erro ao carregar tarefas:", erro);
        this.erro = "Não foi possível carregar suas tarefas. Verifique a conexão e tente novamente.";
        this.carregando = false;
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
    this.erro = null;
    this.tarefaService.concluir(id).subscribe({
      next: () => {
        this.feedback = "Tarefa concluída. Bom trabalho!";
        this.carregarTarefas();
      },
      error: erro => {
        console.error("Erro ao concluir tarefa:", erro);
        this.erro = "Não foi possível concluir a tarefa. Tente novamente.";
      },
    });
  }

  reabrirTarefa(tarefa: Tarefa): void {
    if (!tarefa.id) return;
    this.erro = null;
    this.tarefaService.atualizar(tarefa.id, {
      titulo: tarefa.titulo,
      descricao: tarefa.descricao,
      prioridade: tarefa.prioridade,
      status: "PENDENTE",
      dataPrazo: tarefa.dataPrazo,
      usuario: { id: tarefa.usuario?.id ?? this.session.usuario()?.id ?? 0 },
      categoria: tarefa.categoria ? { id: tarefa.categoria.id } : undefined,
    }).subscribe({
      next: () => {
        this.feedback = "Tarefa reaberta. Ela voltou para Pendente.";
        this.carregarTarefas();
      },
      error: erro => {
        console.error("Erro ao reabrir tarefa:", erro);
        this.erro = "Não foi possível reabrir a tarefa. Tente novamente.";
      },
    });
  }

  pedirExclusao(tarefa: Tarefa): void {
    this.tarefaParaExcluir = tarefa;
  }

  confirmarExclusao(): void {
    const tarefa = this.tarefaParaExcluir;
    if (!tarefa?.id || this.excluindo) return;
    this.excluindo = true;
    this.erro = null;
    this.tarefaService.excluir(tarefa.id).subscribe({
      next: () => {
        this.excluindo = false;
        this.tarefaParaExcluir = null;
        this.feedback = "Tarefa excluída.";
        this.carregarTarefas();
      },
      error: erro => {
        console.error("Erro ao excluir tarefa:", erro);
        this.excluindo = false;
        this.erro = "Não foi possível excluir a tarefa. Tente novamente.";
      },
    });
  }

  sair(): void {
    this.session.limpar();
    void this.router.navigate(["/login"]);
  }
}
