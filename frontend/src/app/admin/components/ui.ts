import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EventoAuditoria } from '../admin.models';

@Component({
  selector: 'adm-page-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-head">
      <div>
        <p class="eyebrow" *ngIf="eyebrow">{{ eyebrow }}</p>
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-sub" *ngIf="subtitle">{{ subtitle }}</p>
      </div>
      <div class="page-actions"><ng-content></ng-content></div>
    </div>
  `,
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() eyebrow = '';
}

@Component({
  selector: 'adm-metric',
  standalone: true,
  imports: [CommonModule],
  template: `
    <article class="card metric" [class.metric-warning]="tone === 'warning'"
      [class.metric-danger]="tone === 'danger'" [class.metric-success]="tone === 'success'">
      <div class="metric-top">
        <span class="metric-label">{{ label }}</span>
        <span class="metric-dot" [attr.data-tone]="tone"></span>
      </div>
      <strong class="metric-value">{{ value }}</strong>
      <span class="metric-hint" *ngIf="hint">{{ hint }}</span>
    </article>
  `,
})
export class MetricCardComponent {
  @Input() label = '';
  @Input() value: string | number = '—';
  @Input() hint = '';
  @Input() tone: 'primary' | 'success' | 'warning' | 'danger' | 'neutral' = 'primary';
}

@Component({
  selector: 'adm-status',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="badge" [ngClass]="classe()">{{ rotulo() }}</span>`,
})
export class StatusBadgeComponent {
  @Input() status = 'ATIVO';
  @Input() label = '';

  rotulo(): string {
    if (this.label) return this.label;
    const mapa: Record<string, string> = {
      ATIVO: 'Ativo', BLOQUEADO: 'Bloqueado', DESATIVADO: 'Desativado', ANONIMIZADO: 'Anonimizado',
      USER: 'Usuário', ADMIN: 'Admin', SUPER_ADMIN: 'Super admin',
      ALLOW: 'Permitir', DENY: 'Bloquear', SIMULADO: 'Simulado', SUCESSO: 'Sucesso', FALHA: 'Falha',
    };
    return mapa[this.status] ?? this.status;
  }

  classe(): string {
    const mapa: Record<string, string> = {
      ATIVO: 'success', USER: 'neutral', ALLOW: 'success', SUCESSO: 'success',
      BLOQUEADO: 'warning', ADMIN: 'info', SIMULADO: 'info',
      DESATIVADO: 'danger', ANONIMIZADO: 'danger', DENY: 'danger', FALHA: 'danger',
      SUPER_ADMIN: 'primary',
    };
    return mapa[this.status] ?? 'neutral';
  }
}

@Component({
  selector: 'adm-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modal-backdrop" *ngIf="open" (click)="fechar()">
      <section class="modal" [class.modal-wide]="wide" role="dialog" aria-modal="true"
        [attr.aria-label]="title" (click)="$event.stopPropagation()">
        <header class="modal-head">
          <div>
            <h2>{{ title }}</h2>
            <p class="page-sub" *ngIf="subtitle">{{ subtitle }}</p>
          </div>
          <button class="icon-btn" type="button" aria-label="Fechar" (click)="fechar()">✕</button>
        </header>
        <div class="modal-body"><ng-content></ng-content></div>
      </section>
    </div>
  `,
})
export class ModalShellComponent {
  @Input() open = false;
  @Input() title = '';
  @Input() subtitle = '';
  @Input() wide = false;
  @Output() closed = new EventEmitter<void>();

  fechar(): void { this.closed.emit(); }
}

@Component({
  selector: 'adm-confirm',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modal-backdrop" *ngIf="open" (click)="cancel.emit()">
      <section class="modal modal-small" role="alertdialog" aria-modal="true" [attr.aria-label]="title"
        (click)="$event.stopPropagation()">
        <header class="modal-head"><h2>{{ title }}</h2></header>
        <div class="modal-body">
          <p class="muted">{{ message }}</p>
          <div class="modal-actions">
            <button type="button" class="btn ghost" [disabled]="busy" (click)="cancel.emit()">Cancelar</button>
            <button type="button" class="btn" [class.danger]="danger" [disabled]="busy" (click)="confirm.emit()">
              {{ busy ? 'Processando…' : confirmLabel }}
            </button>
          </div>
        </div>
      </section>
    </div>
  `,
})
export class ConfirmDialogComponent {
  @Input() open = false;
  @Input() title = 'Confirmar operação';
  @Input() message = '';
  @Input() confirmLabel = 'Confirmar';
  @Input() busy = false;
  @Input() danger = false;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}

@Component({
  selector: 'adm-empty',
  standalone: true,
  template: `
    <div class="empty">
      <span class="empty-icon">◎</span>
      <strong>{{ title }}</strong>
      <p>{{ detail }}</p>
    </div>
  `,
})
export class EmptyStateComponent {
  @Input() title = 'Nada por aqui';
  @Input() detail = 'Não existem registros para os filtros atuais.';
}

@Component({
  selector: 'adm-error',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="error-box" role="alert" *ngIf="message">
      <div><strong>Não foi possível concluir.</strong><p>{{ message }}</p></div>
      <button type="button" class="btn ghost small" (click)="retry.emit()">Tentar novamente</button>
    </div>
  `,
})
export class ErrorStateComponent {
  @Input() message: string | null = null;
  @Output() retry = new EventEmitter<void>();
}

@Component({
  selector: 'adm-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-list" aria-label="Carregando">
      <div class="skeleton" *ngFor="let item of itens" [style.width]="item + '%'"></div>
    </div>
  `,
})
export class LoadingSkeletonComponent {
  @Input() rows = 4;
  get itens(): number[] { return Array.from({ length: this.rows }, (_, i) => 92 - i * 7); }
}

@Component({
  selector: 'adm-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="alert-banner" [ngClass]="tone" *ngIf="message" role="status">
      <span>{{ message }}</span>
      <button type="button" class="icon-btn small" aria-label="Fechar aviso" (click)="closed.emit()">✕</button>
    </div>
  `,
})
export class AlertBannerComponent {
  @Input() message: string | null = null;
  @Input() tone: 'success' | 'error' | 'info' | 'warning' = 'info';
  @Output() closed = new EventEmitter<void>();
}

@Component({
  selector: 'adm-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pagination" *ngIf="totalPages > 1">
      <span class="muted">{{ total }} registro(s)</span>
      <div class="pagination-actions">
        <button type="button" class="btn ghost small" [disabled]="page <= 0" (click)="pageChange.emit(page - 1)">Anterior</button>
        <span class="muted">Página {{ page + 1 }} de {{ totalPages }}</span>
        <button type="button" class="btn ghost small" [disabled]="page >= totalPages - 1" (click)="pageChange.emit(page + 1)">Próxima</button>
      </div>
    </div>
  `,
})
export class PaginationComponent {
  @Input() page = 0;
  @Input() totalPages = 1;
  @Input() total = 0;
  @Output() pageChange = new EventEmitter<number>();
}

@Component({
  selector: 'adm-timeline',
  standalone: true,
  imports: [CommonModule],
  template: `
    <ol class="timeline">
      <li class="timeline-item" *ngFor="let evento of eventos">
        <span class="timeline-dot" [attr.data-resultado]="evento.resultado"></span>
        <div>
          <strong>{{ evento.acao }}</strong>
          <p class="muted">{{ evento.atorEmail || 'sistema' }} · {{ evento.criadoEm | date:'dd/MM/yyyy HH:mm' }}</p>
          <p class="small" *ngIf="evento.detalhes">{{ evento.detalhes }}</p>
        </div>
      </li>
      <li class="muted" *ngIf="!eventos.length">Nenhum evento recente.</li>
    </ol>
  `,
})
export class AuditTimelineComponent {
  @Input() eventos: EventoAuditoria[] = [];
}
