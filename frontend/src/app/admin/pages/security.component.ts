import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import { Pagina, SegurancaOverview, TentativaLogin } from '../admin.models';
import {
  AuditTimelineComponent, EmptyStateComponent, ErrorStateComponent, LoadingSkeletonComponent,
  MetricCardComponent, PageHeaderComponent, PaginationComponent, StatusBadgeComponent,
} from '../components/ui';

@Component({
  selector: 'app-admin-security',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, PageHeaderComponent, MetricCardComponent,
    StatusBadgeComponent, PaginationComponent, EmptyStateComponent, ErrorStateComponent,
    LoadingSkeletonComponent, AuditTimelineComponent],
  templateUrl: './security.component.html',
})
export class AdminSecurityComponent implements OnInit, OnDestroy {
  overview: SegurancaOverview | null = null;
  tentativas: Pagina<TentativaLogin> | null = null;
  carregando = true;
  erro: string | null = null;
  filtros = { email: '', sucesso: '' };
  page = 0;
  private sub = new Subscription();

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void { this.sub.unsubscribe(); }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.sub.add(forkJoin({
      overview: this.admin.seguranca(),
      tentativas: this.admin.tentativas({ ...this.filtros, page: this.page, size: 15 }),
    }).subscribe({
      next: ({ overview, tentativas }) => {
        this.overview = overview; this.tentativas = tentativas; this.carregando = false;
      },
      error: (erro) => { this.erro = erro.error?.message || 'Falha ao carregar segurança.'; this.carregando = false; },
    }));
  }

  filtrar(): void { this.page = 0; this.carregar(); }
  limpar(): void { this.filtros = { email: '', sucesso: '' }; this.page = 0; this.carregar(); }
  mudarPagina(page: number): void { this.page = page; this.carregar(); }

  tomAlerta(nivel: string): 'danger' | 'warning' | 'primary' {
    if (nivel === 'CRITICO') return 'danger';
    if (nivel === 'ATENCAO') return 'warning';
    return 'primary';
  }
}
