import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login.component";
import { CadastroComponent } from "./pages/cadastro/cadastro.component";
import { HomeComponent } from "./pages/home/home.component";
import { TarefaFormComponent } from "./pages/tarefa-form/tarefa-form.component";
import { PerfilComponent } from "./pages/perfil/perfil.component";
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
    path: "home",
    canActivate: [authGuard],
    component: HomeComponent,
  },
  {
    path: "tarefas/nova",
    canActivate: [authGuard],
    component: TarefaFormComponent,
  },
  {
    path: "tarefas/editar/:id",
    canActivate: [authGuard],
    component: TarefaFormComponent,
  },
  {
    path: "perfil",
    canActivate: [authGuard],
    component: PerfilComponent,
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
    redirectTo: "login",
    pathMatch: "full",
  },
];
