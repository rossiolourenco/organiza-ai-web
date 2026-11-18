import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, Observable, Subscription } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import {
  FirewallEstado, FirewallHistorico, FirewallRegra, FirewallValidacao,
} from '../admin.models';
import {
  AlertBannerComponent, ConfirmDialogComponent, EmptyStateComponent,
  LoadingSkeletonComponent, MetricCardComponent, ModalShellComponent, PageHeaderComponent, StatusBadgeComponent,
} from '../components/ui';

interface RegraForm {
  nome: string; descricao: string; ipCidr: string; porta: number | null;
  protocolo: 'TCP' | 'UDP' | 'ANY'; direcao: 'INBOUND' | 'OUTBOUND'; acao: 'ALLOW' | 'DENY';
  prioridade: number; ativa: boolean;
}
interface Confirmacao { titulo: string; mensagem: string; rotulo: string; perigo: boolean; acao: () => void }

@Component({
  selector: 'app-admin-firewall',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, MetricCardComponent, StatusBadgeComponent,
    ModalShellComponent, ConfirmDialogComponent, EmptyStateComponent,
    LoadingSkeletonComponent, AlertBannerComponent],
  templateUrl: './firewall.component.html',
})
export class AdminFirewallComponent implements OnInit, OnDestroy {
  estado: FirewallEstado | null = null;
  regras: FirewallRegra[] = [];
  validacao: FirewallValidacao | null = null;
  historico: FirewallHistorico[] = [];
  carregando = true;
  erro: string | null = null;
  aviso: string | null = null;
  modal = false;
  editandoId: number | null = null;
  confirmacao: Confirmacao | null = null;
  executando = false;
  form: RegraForm = this.vazia();
  private sub = new Subscription();

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void { this.sub.unsubscribe(); }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.sub.add(forkJoin({
      estado: this.admin.firewallEstado(),
      regras: this.admin.firewallRegras(),
      historico: this.admin.firewallHistorico(),
      validacao: this.admin.validarFirewall(),
    }).subscribe({
      next: ({ estado, regras, historico, validacao }) => {
        this.estado = estado; this.regras = regras; this.historico = historico; this.validacao = validacao;
        this.carregando = false;
      },
      error: (erro) => { this.erro = this.mensagem(erro); this.carregando = false; },
    }));
  }

  abrirNova(): void { this.editandoId = null; this.form = this.vazia(); this.modal = true; }

  abrirEdicao(regra: FirewallRegra): void {
    this.editandoId = regra.id;
    this.form = { nome: regra.nome, descricao: regra.descricao || '', ipCidr: regra.ipCidr,
      porta: regra.porta, protocolo: regra.protocolo, direcao: regra.direcao, acao: regra.acao,
      prioridade: regra.prioridade, ativa: regra.ativa };
    this.modal = true;
  }

  salvar(): void {
    if (!this.form.nome.trim() || !this.form.ipCidr.trim()) {
      this.erro = 'Informe nome e IP/CIDR da regra.'; return;
    }
    this.executando = true;
    const operacao = this.editandoId
      ? this.admin.atualizarRegra(this.editandoId, this.form)
      : this.admin.criarRegra(this.form);
    this.sub.add(operacao.subscribe({
      next: () => {
        this.executando = false; this.modal = false;
        this.aviso = this.editandoId ? 'Regra atualizada no simulador.' : 'Regra criada no simulador.';
        this.carregar();
      },
      error: (erro) => { this.executando = false; this.erro = this.mensagem(erro); },
    }));
  }

  alternar(regra: FirewallRegra, ativa: boolean): void {
    this.executar(this.admin.alternarRegra(regra.id, ativa),
      ativa ? 'Regra ativada no simulador.' : 'Regra desativada no simulador.');
  }

  excluir(regra: FirewallRegra): void {
    this.confirmar('Excluir regra', `A regra “${regra.nome}” será removida do simulador. Nenhum firewall real é alterado.`,
      'Excluir regra', true, () => this.executar(this.admin.excluirRegra(regra.id), 'Regra removida.'));
  }

  validar(): void {
    this.sub.add(this.admin.validarFirewall().subscribe({
      next: (validacao) => {
        this.validacao = validacao;
        this.aviso = validacao.valido ? 'Nenhum conflito encontrado entre regras ativas.'
          : `${validacao.conflitos.length} conflito(s) encontrado(s).`;
      },
      error: (erro) => this.erro = this.mensagem(erro),
    }));
  }

  aplicar(): void {
    if (this.validacao && !this.validacao.valido) {
      this.erro = 'Corrija os conflitos antes de aplicar a simulação.'; return;
    }
    this.confirmar('Aplicar simulação', 'Um snapshot das regras será criado e o estado será marcado como aplicado. ' +
      'Esta operação é apenas uma simulação e não altera o firewall do sistema operacional.',
      'Aplicar simulação', false, () => this.executar(this.admin.aplicarFirewall('Aplicação pela interface'), 'Simulação aplicada.'));
  }

  rollback(): void {
    this.confirmar('Restaurar snapshot', 'As regras atuais serão substituídas pelo último snapshot simulado. ' +
      'Nenhuma alteração real será feita no sistema operacional.', 'Restaurar snapshot', true,
      () => this.executar(this.admin.rollbackFirewall(), 'Snapshot restaurado.'));
  }

  resetar(): void {
    this.confirmar('Restaurar cenário DEMO', 'As regras simuladas serão substituídas pelo conjunto padrão de demonstração. ' +
      'O firewall real permanece intocado.', 'Restaurar DEMO', true,
      () => this.executar(this.admin.resetFirewall(), 'Cenário de firewall restaurado.'));
  }

  confirmar(titulo: string, mensagem: string, rotulo: string, perigo: boolean, acao: () => void): void {
    this.confirmacao = { titulo, mensagem, rotulo, perigo, acao };
  }

  executarConfirmacao(): void { this.confirmacao?.acao(); }
  cancelarConfirmacao(): void { if (!this.executando) this.confirmacao = null; }

  private executar(obs: Observable<unknown>, sucesso: string): void {
    this.executando = true;
    this.sub.add(obs.subscribe({
      next: () => { this.executando = false; this.confirmacao = null; this.aviso = sucesso; this.carregar(); },
      error: (erro) => { this.executando = false; this.confirmacao = null; this.erro = this.mensagem(erro); },
    }));
  }

  private vazia(): RegraForm {
    return { nome: '', descricao: '', ipCidr: '0.0.0.0/0', porta: null, protocolo: 'TCP',
      direcao: 'INBOUND', acao: 'ALLOW', prioridade: 100, ativa: true };
  }

  private mensagem(erro: any): string {
    return erro?.error?.message || erro?.message || 'Falha na operação de firewall.';
  }
}
