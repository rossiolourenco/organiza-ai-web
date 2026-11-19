import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import { SessionService } from '../../services/session.service';
import { AdminMe, Politica } from '../admin.models';
import {
  AlertBannerComponent, ErrorStateComponent, LoadingSkeletonComponent, PageHeaderComponent,
  StatusBadgeComponent,
} from '../components/ui';

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, ErrorStateComponent, LoadingSkeletonComponent,
    AlertBannerComponent, StatusBadgeComponent],
  templateUrl: './settings.component.html',
})
export class AdminSettingsComponent implements OnInit, OnDestroy {
  politica: Politica | null = null;
  me: AdminMe | null = null;
  carregando = true;
  salvando = false;
  erro: string | null = null;
  aviso: string | null = null;
  podeEditar = false;
  senhaAtual = '';
  novaSenha = '';
  confirmarSenha = '';
  trocando = false;
  erroSenha: string | null = null;
  private sub = new Subscription();

  constructor(private admin: AdminService, public session: SessionService, private router: Router) {}

  ngOnInit(): void {
    this.podeEditar = this.session.isSuperAdmin();
    this.carregar();
  }
  ngOnDestroy(): void { this.sub.unsubscribe(); }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.sub.add(this.admin.politica().subscribe({
      next: (politica) => { this.politica = politica; this.carregando = false; },
      error: (erro) => { this.erro = erro.error?.message || 'Falha ao carregar configurações.'; this.carregando = false; },
    }));
    this.sub.add(this.admin.me().subscribe({ next: (me) => this.me = me }));
  }

  salvar(): void {
    if (!this.politica || !this.podeEditar) return;
    this.salvando = true;
    this.erro = null;
    this.sub.add(this.admin.salvarPolitica(this.politica).subscribe({
      next: (politica) => {
        this.politica = politica; this.salvando = false;
        this.aviso = 'Política de segurança atualizada e auditada.';
      },
      error: (erro) => { this.salvando = false; this.erro = erro.error?.message || 'Falha ao salvar política.'; },
    }));
  }

  alterarSenha(): void {
    this.erroSenha = null;
    if (!this.senhaAtual || !this.novaSenha) { this.erroSenha = 'Informe a senha atual e a nova senha.'; return; }
    if (this.novaSenha.length < 8) { this.erroSenha = 'A nova senha deve ter pelo menos 8 caracteres.'; return; }
    if (this.novaSenha !== this.confirmarSenha) { this.erroSenha = 'A confirmação não confere.'; return; }
    this.trocando = true;
    this.sub.add(this.admin.alterarSenha(this.senhaAtual, this.novaSenha).subscribe({
      next: () => {
        this.trocando = false;
        this.session.limpar();
        this.router.navigate(['/login'], { queryParams: { senha: 'alterada' } });
      },
      error: (erro) => {
        this.trocando = false;
        this.erroSenha = erro.error?.message || 'Não foi possível alterar a senha.';
      },
    }));
  }
}
