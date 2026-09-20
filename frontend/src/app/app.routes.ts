import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login.component";
import { CadastroComponent } from "./pages/cadastro/cadastro.component";
import { HomeComponent } from "./pages/home/home.component";
import { TarefaFormComponent } from "./pages/tarefa-form/tarefa-form.component";

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
    component: HomeComponent,
  },
  {
    path: "tarefas/nova",
    component: TarefaFormComponent,
  },
  {
    path: "tarefas/editar/:id",
    component: TarefaFormComponent,
  },
  {
    path: "",
    redirectTo: "login",
    pathMatch: "full",
  },
];
