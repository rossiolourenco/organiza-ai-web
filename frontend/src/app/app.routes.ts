import { Routes } from "@angular/router";

import { LoginComponent } from "./pages/login/login.component";
import { CadastroComponent } from "./pages/cadastro/cadastro.component";
import { ExcluirContaComponent } from "./pages/excluir-conta/excluir-conta.component";
import { LegalComponent } from "./pages/legal/legal.component";
import { HomeComponent } from "./pages/home/home.component";
import { TarefaFormComponent } from "./pages/tarefa-form/tarefa-form.component";
import { PerfilComponent } from "./pages/perfil/perfil.component";
import { DashboardComponent } from "./pages/dashboard/dashboard.component";
import { PlanejamentoComponent } from "./pages/planejamento/planejamento.component";
import { RevisaoComponent } from "./pages/revisao/revisao.component";
import { FocoComponent } from "./pages/foco/foco.component";
import { HabitosComponent } from "./pages/habitos/habitos.component";
import { CategoriasComponent } from "./pages/categorias/categorias.component";
import { LixeiraComponent } from "./pages/lixeira/lixeira.component";
import { AppShellComponent } from "./shell/app-shell.component";

import { authGuard } from "./services/auth.guard";
import { adminGuard } from "./services/admin.guard";

import { AdminLayoutComponent } from "./admin/admin-layout.component";
import { AdminDashboardComponent } from "./admin/pages/dashboard.component";
import { AdminUsersComponent } from "./admin/pages/users.component";
import { AdminSecurityComponent } from "./admin/pages/security.component";
import { AdminFirewallComponent } from "./admin/pages/firewall.component";
import { AdminMonitoringComponent } from "./admin/pages/monitoring.component";
import { AdminAuditComponent } from "./admin/pages/audit.component";
import { AdminSettingsComponent } from "./admin/pages/settings.component";

export const routes: Routes = [
  {
    path: "login",
    component: LoginComponent,
  },
  {
    path: "cadastro",
    component: CadastroComponent,
  },
  {
    path: "excluir-conta",
    component: ExcluirContaComponent,
  },
  {
    path: "privacidade",
    component: LegalComponent,
    data: { tipo: "privacidade" },
  },
  {
    path: "termos",
    component: LegalComponent,
    data: { tipo: "termos" },
  },
  {
    path: "admin",
    canActivate: [adminGuard],
    component: AdminLayoutComponent,
    children: [
      { path: "", pathMatch: "full", redirectTo: "dashboard" },
      { path: "dashboard", component: AdminDashboardComponent },
      { path: "usuarios", component: AdminUsersComponent },
      { path: "seguranca", component: AdminSecurityComponent },
      { path: "firewall", component: AdminFirewallComponent },
      { path: "monitoramento", component: AdminMonitoringComponent },
      { path: "auditoria", component: AdminAuditComponent },
      { path: "configuracoes", component: AdminSettingsComponent },
    ],
  },
  {
    path: "",
    canActivate: [authGuard],
    component: AppShellComponent,
    children: [
      { path: "", pathMatch: "full", redirectTo: "home" },
      { path: "home", component: DashboardComponent },
      { path: "tarefas", component: HomeComponent },
      { path: "tarefas/nova", component: TarefaFormComponent },
      { path: "tarefas/editar/:id", component: TarefaFormComponent },
      { path: "planejamento", component: PlanejamentoComponent },
      { path: "revisao", component: RevisaoComponent },
      { path: "foco", component: FocoComponent },
      { path: "habitos", component: HabitosComponent },
      { path: "categorias", component: CategoriasComponent },
      { path: "lixeira", component: LixeiraComponent },
      { path: "perfil", component: PerfilComponent },
    ],
  },
  {
    path: "**",
    redirectTo: "login",
  },
];
