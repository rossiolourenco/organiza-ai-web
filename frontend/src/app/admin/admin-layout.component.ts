import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { AdminService } from '../services/admin.service';
import { SessionService } from '../services/session.service';
import { ThemeService } from '../services/theme.service';
import { AdminMe } from './admin.models';

interface MenuItem { path: string; label: string; icon: string }

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  readonly menu: MenuItem[] = [
    { path: 'dashboard', label: 'Dashboard', icon: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z' },
    { path: 'usuarios', label: 'Usuários', icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75' },
    { path: 'seguranca', label: 'Segurança', icon: 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z' },
    { path: 'firewall', label: 'Firewall', icon: 'M2 5h20v5H2zM2 14h20v5H2zM6 7h.01M6 16h.01' },
    { path: 'monitoramento', label: 'Monitoramento', icon: 'M3 3v18h18M7 14l4-4 3 3 5-6' },
    { path: 'auditoria', label: 'Auditoria', icon: 'M9 5h6M9 9h6M9 13h6M5 3h14v18H5z' },
    { path: 'configuracoes', label: 'Configurações', icon: 'M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM4 12h2m12 0h2M12 4v2m0 12v2' },
  ];
  collapsed = typeof localStorage !== 'undefined' && localStorage.getItem('organiza-admin-sidebar') === 'collapsed';
  mobileOpen = false;
  alertas = 0;
  me: AdminMe | null = null;
  private sub = new Subscription();

  constructor(
    public session: SessionService,
    public theme: ThemeService,
    private admin: AdminService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.sub.add(this.admin.me().subscribe({
      next: (me) => this.me = me,
      error: () => { /* a guarda e o backend continuam protegendo a navegação */ },
    }));
    this.sub.add(this.admin.seguranca().subscribe({
      next: (overview) => this.alertas = overview.alertas.length,
      error: () => this.alertas = 0,
    }));
  }

  ngOnDestroy(): void { this.sub.unsubscribe(); }

  alternarSidebar(): void {
    if (window.innerWidth < 980) this.mobileOpen = !this.mobileOpen;
    else {
      this.collapsed = !this.collapsed;
      localStorage.setItem('organiza-admin-sidebar', this.collapsed ? 'collapsed' : 'expanded');
    }
  }

  sair(): void {
    this.session.limpar();
    this.router.navigate(['/login']);
  }

  papel(): string {
    const papel = this.session.papel();
    return papel === 'SUPER_ADMIN' ? 'Super admin' : papel === 'ADMIN' ? 'Admin' : 'Usuário';
  }
}
