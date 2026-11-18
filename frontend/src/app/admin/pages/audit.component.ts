import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import { EventoAuditoria, Pagina } from '../admin.models';
import {
  EmptyStateComponent, ErrorStateComponent, LoadingSkeletonComponent, PageHeaderComponent,
  PaginationComponent, StatusBadgeComponent,
} from '../components/ui';

@Component({
  selector: 'app-admin-audit',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, StatusBadgeComponent, PaginationComponent,
    EmptyStateComponent, ErrorStateComponent, LoadingSkeletonComponent],
  templateUrl: './audit.component.html',
})
export class AdminAuditComponent implements OnInit, OnDestroy {
  resultado: Pagina<EventoAuditoria> | null = null;
  carregando = true;
  erro: string | null = null;
  filtros = { acao: '', ator: '', desde: '', ate: '' };
  page = 0;
  expandido: number | null = null;
  private sub = new Subscription();

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void { this.sub.unsubscribe(); }

  carregar(resetPage = false): void {
    if (resetPage) this.page = 0;
    this.carregando = true;
    this.erro = null;
    this.sub.add(this.admin.auditoria({ ...this.filtros, page: this.page, size: 25 }).subscribe({
      next: (pagina) => { this.resultado = pagina; this.carregando = false; },
      error: (erro) => { this.erro = erro.error?.message || 'Falha ao carregar auditoria.'; this.carregando = false; },
    }));
  }

  filtrar(): void { this.carregar(true); }
  limpar(): void { this.filtros = { acao: '', ator: '', desde: '', ate: '' }; this.carregar(true); }
  mudarPagina(page: number): void { this.page = page; this.carregar(); }
  alternar(id: number): void { this.expandido = this.expandido === id ? null : id; }
}
