import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";

import { Habito, HabitoService } from "../../services/habito.service";
import { addDays, dayKey, habitStreak } from "../../services/productivity";

interface HabitoForm {
  nome: string;
  descricao: string;
  frequencia: "DIARIA" | "SEMANAL";
  metaSemanal: number;
  cor: string;
}

@Component({
  selector: "app-habitos",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./habitos.component.html",
  styleUrl: "./habitos.component.css",
})
export class HabitosComponent implements OnInit {
  habitos: Habito[] = [];
  carregando = true;
  salvando = false;
  erro: string | null = null;
  feedback: string | null = null;
  editandoId: number | null = null;
  confirmandoExclusao: number | null = null;
  readonly hoje = dayKey();

  form: HabitoForm = this.formVazio();

  constructor(private habitoService: HabitoService) {}

  ngOnInit(): void {
    this.carregar();
  }

  private formVazio(): HabitoForm {
    return { nome: "", descricao: "", frequencia: "DIARIA", metaSemanal: 7, cor: "#4F46E5" };
  }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.habitoService.listar().subscribe({
      next: (habitos) => {
        this.habitos = habitos;
        this.carregando = false;
      },
      error: () => {
        this.erro = "Não foi possível carregar seus hábitos.";
        this.carregando = false;
      },
    });
  }

  get ativos(): number {
    return this.habitos.filter((h) => h.ativo !== false).length;
  }

  get feitosHoje(): number {
    return this.habitos.filter((h) => this.feitoHoje(h)).length;
  }

  dias(habito: Habito): { data: string; label: string; feito: boolean }[] {
    const registros = new Set((habito.registros || []).map((r) => r.data));
    return Array.from({ length: 7 }, (_, i) => {
      const data = addDays(this.hoje, i - 6);
      const [, mes, dia] = data.split("-");
      return { data, label: `${dia}/${mes}`, feito: registros.has(data) };
    });
  }

  feitoHoje(habito: Habito): boolean {
    return (habito.registros || []).some((r) => r.data === this.hoje);
  }

  sequencia(habito: Habito): number {
    return habitStreak((habito.registros || []).map((r) => r.data), this.hoje);
  }

  semana(habito: Habito): number {
    return (habito.registros || []).filter((r) => r.data >= addDays(this.hoje, -6)).length;
  }

  alternarHoje(habito: Habito): void {
    if (!habito.id) return;
    const acao = this.feitoHoje(habito)
      ? this.habitoService.removerRegistro(habito.id, this.hoje)
      : this.habitoService.registrar(habito.id, this.hoje);
    acao.subscribe({
      next: (atualizado) => {
        this.habitos = this.habitos.map((h) => (h.id === atualizado.id ? atualizado : h));
      },
      error: () => {
        this.erro = "Não foi possível atualizar o hábito.";
      },
    });
  }

  salvar(): void {
    if (this.salvando) return;
    const nome = this.form.nome.trim();
    if (!nome) {
      this.erro = "Informe o nome do hábito.";
      return;
    }
    this.salvando = true;
    this.erro = null;
    const payload: Habito = {
      nome,
      descricao: this.form.descricao.trim() || undefined,
      frequencia: this.form.frequencia,
      metaSemanal: Math.min(7, Math.max(1, Number(this.form.metaSemanal) || 7)),
      cor: this.form.cor,
      ativo: true,
    };
    const acao = this.editandoId
      ? this.habitoService.atualizar(this.editandoId, payload)
      : this.habitoService.criar(payload);
    acao.subscribe({
      next: () => {
        this.salvando = false;
        this.feedback = this.editandoId ? "Hábito atualizado." : "Hábito criado.";
        this.cancelarEdicao();
        this.carregar();
      },
      error: () => {
        this.salvando = false;
        this.erro = "Não foi possível salvar o hábito.";
      },
    });
  }

  editar(habito: Habito): void {
    this.editandoId = habito.id ?? null;
    this.form = {
      nome: habito.nome,
      descricao: habito.descricao || "",
      frequencia: habito.frequencia,
      metaSemanal: habito.metaSemanal,
      cor: habito.cor || "#4F46E5",
    };
    this.feedback = null;
  }

  cancelarEdicao(): void {
    this.editandoId = null;
    this.form = this.formVazio();
  }

  pedirExclusao(habito: Habito): void {
    this.confirmandoExclusao = habito.id ?? null;
  }

  cancelarExclusao(): void {
    this.confirmandoExclusao = null;
  }

  excluir(habito: Habito): void {
    if (!habito.id) return;
    this.habitoService.excluir(habito.id).subscribe({
      next: () => {
        this.confirmandoExclusao = null;
        this.feedback = "Hábito removido, junto com o histórico de registros.";
        this.carregar();
      },
      error: () => {
        this.erro = "Não foi possível remover o hábito.";
      },
    });
  }
}
