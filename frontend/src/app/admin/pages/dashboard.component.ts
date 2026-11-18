import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import { Monitor, SegurancaOverview } from '../admin.models';
import {
  AuditTimelineComponent, ErrorStateComponent, LoadingSkeletonComponent, MetricCardComponent,
  PageHeaderComponent, StatusBadgeComponent,
} from '../components/ui';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, PageHeaderComponent, MetricCardComponent, StatusBadgeComponent,
    AuditTimelineComponent, ErrorStateComponent, LoadingSkeletonComponent],
  templateUrl: './dashboard.component.html',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  seguranca: SegurancaOverview | null = null;
  monitor: Monitor | null = null;
  carregando = true;
  erro: string | null = null;
  private sub = new Subscription();

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void { this.sub.unsubscribe(); }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.sub.add(forkJoin({
      seguranca: this.admin.seguranca(),
      monitor: this.admin.monitoramento(),
    }).subscribe({
      next: ({ seguranca, monitor }) => {
        this.seguranca = seguranca;
        this.monitor = monitor;
        this.carregando = false;
      },
      error: (erro) => {
        this.erro = erro.error?.message || 'Falha ao carregar indicadores.';
        this.carregando = false;
      },
    }));
  }

  percentual(usado: number, total: number): number {
    return total > 0 ? Math.min(100, Math.round(usado / total * 100)) : 0;
  }

  bytes(valor: number | null | undefined): string {
    if (valor == null) return 'Indisponível';
    const unidades = ['B', 'KB', 'MB', 'GB', 'TB'];
    let numero = valor, indice = 0;
    while (numero >= 1024 && indice < unidades.length - 1) { numero /= 1024; indice++; }
    return `${numero.toFixed(numero >= 10 ? 0 : 1)} ${unidades[indice]}`;
  }

  uptime(segundos: number): string {
    const dias = Math.floor(segundos / 86400);
    const horas = Math.floor((segundos % 86400) / 3600);
    const minutos = Math.floor((segundos % 3600) / 60);
    if (dias) return `${dias}d ${horas}h`;
    if (horas) return `${horas}h ${minutos}min`;
    return `${minutos}min`;
  }

  monitorTone(valor: boolean | null | undefined): 'success' | 'danger' | 'warning' {
    if (valor === true) return 'success';
    if (valor === false) return 'danger';
    return 'warning';
  }
}
