import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import { SessionService } from '../../services/session.service';
import { Papel, Pagina, SenhaTemporaria, UsuarioAdmin, UsuarioDetalhe } from '../admin.models';
import {
  AlertBannerComponent, AuditTimelineComponent, ConfirmDialogComponent, EmptyStateComponent,
  LoadingSkeletonComponent, MetricCardComponent, ModalShellComponent,
  PageHeaderComponent, PaginationComponent, StatusBadgeComponent,
} from '../components/ui';

interface Confirmacao {
  titulo: string;
  mensagem: string;
  rotulo: string;
  perigo: boolean;
  acao: () => void;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, MetricCardComponent, StatusBadgeComponent,
    PaginationComponent, EmptyStateComponent, LoadingSkeletonComponent,
    ModalShellComponent, ConfirmDialogComponent, AuditTimelineComponent, AlertBannerComponent],
  templateUrl: './users.component.html',
})
export class AdminUsersComponent implements OnInit, OnDestroy {
  resultado: Pagina<UsuarioAdmin> | null = null;
  detalhe: UsuarioDetalhe | null = null;
  carregando = true;
  carregandoDetalhe = false;
  erro: string | null = null;
  aviso: string | null = null;
  filtros = { q: '', status: '', papel: '' };
  page = 0;
  size = 20;
  confirmacao: Confirmacao | null = null;
  executando = false;
  modalCriar = false;
  modalDetalhe = false;
  modalSenha = false;
  senha: SenhaTemporaria | null = null;
  copiado = false;
  novo = { nome: '', email: '', papel: 'USER' };
  papelSelecionado: Papel = 'USER';
  private sub = new Subscription();

  constructor(private admin: AdminService, public session: SessionService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void { this.sub.unsubscribe(); }

  carregar(resetPage = false): void {
    if (resetPage) this.page = 0;
    this.carregando = true;
    this.erro = null;
    this.sub.add(this.admin.usuarios({ ...this.filtros, page: this.page, size: this.size }).subscribe({
      next: (pagina) => { this.resultado = pagina; this.carregando = false; },
      error: (erro) => { this.erro = this.mensagem(erro); this.carregando = false; },
    }));
  }

  buscar(): void { this.carregar(true); }
  limpar(): void { this.filtros = { q: '', status: '', papel: '' }; this.carregar(true); }
  mudarPagina(page: number): void { this.page = page; this.carregar(); }

  abrirDetalhe(usuario: UsuarioAdmin): void {
    this.modalDetalhe = true;
    this.carregandoDetalhe = true;
    this.detalhe = null;
    this.papelSelecionado = usuario.papel;
    this.sub.add(this.admin.usuario(usuario.id).subscribe({
      next: (detalhe) => { this.detalhe = detalhe; this.papelSelecionado = detalhe.usuario.papel; this.carregandoDetalhe = false; },
      error: (erro) => { this.erro = this.mensagem(erro); this.carregandoDetalhe = false; this.modalDetalhe = false; },
    }));
  }

  fecharDetalhe(): void { this.modalDetalhe = false; this.detalhe = null; }

  podeGerenciar(usuario: UsuarioAdmin): boolean {
    if (usuario.id === this.session.usuario()?.id) return false;
    if (this.session.isSuperAdmin()) return true;
    return this.session.papel() === 'ADMIN' && usuario.papel === 'USER';
  }

  podeAlterarPapel(usuario: UsuarioAdmin): boolean {
    return this.session.isSuperAdmin() && usuario.id !== this.session.usuario()?.id;
  }

  alterarStatus(usuario: UsuarioAdmin, status: string): void {
    const rotulo = status === 'ATIVO' ? 'Desbloquear conta' : status === 'BLOQUEADO' ? 'Bloquear conta' : 'Desativar conta';
    this.confirmacao = {
      titulo: rotulo,
      mensagem: `A conta de ${usuario.nome} (${usuario.email}) terá o status alterado para ${status}. ` +
        (status === 'ATIVO' ? 'O acesso será restabelecido no próximo login.' : 'Todas as sessões ativas serão revogadas.'),
      rotulo, perigo: status !== 'ATIVO',
      acao: () => this.executar(this.admin.alterarStatus(usuario.id, status),
        `Status de ${usuario.nome} atualizado para ${status}.`),
    };
  }

  aplicarPapel(): void {
    if (!this.detalhe) return;
    const usuario = this.detalhe.usuario;
    if (this.papelSelecionado === usuario.papel) { this.aviso = 'O papel selecionado já está atribuído.'; return; }
    this.confirmacao = {
      titulo: 'Alterar papel da conta',
      mensagem: `O papel de ${usuario.nome} mudará de ${usuario.papel} para ${this.papelSelecionado}. ` +
        'A alteração é auditada e o usuário pode precisar entrar novamente.',
      rotulo: 'Alterar papel', perigo: true,
      acao: () => this.executar(this.admin.alterarPapel(usuario.id, this.papelSelecionado),
        `Papel atualizado para ${this.papelSelecionado}.`, () => this.recarregarDetalhe()),
    };
  }

  revogar(usuario: UsuarioAdmin): void {
    this.confirmacao = {
      titulo: 'Revogar sessões',
      mensagem: `Todas as sessões de ${usuario.nome} serão encerradas imediatamente. O usuário precisará entrar novamente.`,
      rotulo: 'Revogar sessões', perigo: false,
      acao: () => this.executar(this.admin.revogarSessoes(usuario.id),
        'Sessões revogadas.', () => this.recarregarDetalhe()),
    };
  }

  redefinirSenha(usuario: UsuarioAdmin): void {
    this.confirmacao = {
      titulo: 'Redefinir senha',
      mensagem: `Uma senha temporária será gerada para ${usuario.nome}. Todas as sessões serão revogadas e a senha ` +
        'será exibida apenas uma vez.',
      rotulo: 'Gerar senha temporária', perigo: true,
      acao: () => {
        this.executando = true;
        this.sub.add(this.admin.redefinirSenha(usuario.id).subscribe({
          next: (senha) => {
            this.executando = false; this.confirmacao = null; this.senha = senha;
            this.modalSenha = true; this.copiado = false;
            this.aviso = 'Senha temporária emitida. Copie agora e entregue por canal seguro.';
            this.recarregarDetalhe(); this.carregar();
          },
          error: (erro) => { this.executando = false; this.erro = this.mensagem(erro); },
        }));
      },
    };
  }

  anonimizar(usuario: UsuarioAdmin): void {
    this.confirmacao = {
      titulo: 'Anonimizar conta',
      mensagem: `Os dados de identificação de ${usuario.nome} serão removidos e a conta será desativada. ` +
        'Tarefas, categorias e hábitos são preservados por integridade, mas ficam inacessíveis. Esta ação não pode ser desfeita.',
      rotulo: 'Anonimizar definitivamente', perigo: true,
      acao: () => this.executar(this.admin.anonimizar(usuario.id),
        'Conta anonimizada. Dados operacionais preservados.', () => { this.fecharDetalhe(); this.carregar(); }),
    };
  }

  criar(): void {
    this.erro = null;
    if (!this.novo.nome.trim() || !this.novo.email.includes('@')) {
      this.erro = 'Informe nome e e-mail válidos.'; return;
    }
    this.executando = true;
    this.sub.add(this.admin.criarUsuario({ ...this.novo, nome: this.novo.nome.trim(), email: this.novo.email.trim() }).subscribe({
      next: (senha) => {
        this.executando = false; this.modalCriar = false; this.senha = senha; this.modalSenha = true;
        this.copiado = false;
        this.novo = { nome: '', email: '', papel: 'USER' };
        this.aviso = 'Conta criada. Entregue a senha temporária por canal seguro.';
        this.carregar(true);
      },
      error: (erro) => { this.executando = false; this.erro = this.mensagem(erro); },
    }));
  }

  confirmar(): void { this.confirmacao?.acao(); }
  cancelarConfirmacao(): void { if (!this.executando) this.confirmacao = null; }

  copiarSenha(): void {
    if (!this.senha) return;
    void navigator.clipboard?.writeText(this.senha.senhaTemporaria).then(() => this.copiado = true)
      .catch(() => this.copiado = false);
  }

  fecharSenha(): void { this.modalSenha = false; this.senha = null; this.copiado = false; }

  private executar(obs: Observable<unknown>, sucesso: string, apos?: () => void): void {
    this.executando = true;
    this.sub.add(obs.subscribe({
      next: () => {
        this.executando = false; this.confirmacao = null; this.aviso = sucesso; this.erro = null;
        apos?.(); this.carregar();
      },
      error: (erro) => { this.executando = false; this.confirmacao = null; this.erro = this.mensagem(erro); },
    }));
  }

  private recarregarDetalhe(): void {
    if (!this.detalhe) return;
    const id = this.detalhe.usuario.id;
    this.sub.add(this.admin.usuario(id).subscribe({ next: (detalhe) => this.detalhe = detalhe }));
  }

  private mensagem(erro: any): string {
    return erro?.error?.message || erro?.message || 'Falha na operação administrativa.';
  }
}
