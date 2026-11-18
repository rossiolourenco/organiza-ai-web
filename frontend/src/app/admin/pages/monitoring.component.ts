import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import { Monitor } from '../admin.models';
import {
  ErrorStateComponent, LoadingSkeletonComponent, MetricCardComponent, PageHeaderComponent,
  StatusBadgeComponent,
} from '../components/ui';

@Component({
  selector: 'app-admin-monitoring',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, MetricCardComponent, StatusBadgeComponent,
    ErrorStateComponent, LoadingSkeletonComponent],
  templateUrl: './monitoring.component.html',
})
export class AdminMonitoringComponent implements OnInit, OnDestroy {
  monitor: Monitor | null = null;
  carregando = true;
  erro: string | null = null;
  auto = false;
  private timer: ReturnType<typeof setInterval> | null = null;
  private sub = new Subscription();

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.timer) clearInterval(this.timer);
  }

  carregar(): void {
    this.carregando = !this.monitor;
    this.sub.add(this.admin.monitoramento().subscribe({
      next: (monitor) => { this.monitor = monitor; this.carregando = false; this.erro = null; },
      error: (erro) => { this.erro = erro.error?.message || 'Falha ao coletar métricas.'; this.carregando = false; },
    }));
  }

  alternarAuto(): void {
    this.auto = !this.auto;
    if (this.timer) { clearInterval(this.timer); this.timer = null; }
    if (this.auto) this.timer = setInterval(() => this.carregar(), 15000);
  }

  percentual(usado: number | null | undefined, total: number | null | undefined): number {
    return usado != null && total != null && total > 0 ? Math.min(100, Math.round(usado / total * 100)) : 0;
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
    return dias ? `${dias}d ${horas}h ${minutos}min` : horas ? `${horas}h ${minutos}min` : `${minutos}min`;
  }

  numero(valor: number | null | undefined, sufixo = ''): string {
    return valor == null ? 'Indisponível' : `${valor}${sufixo}`;
  }
}
