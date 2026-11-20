import { CommonModule } from "@angular/common";
import { Component, HostListener, OnInit } from "@angular/core";
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { SessionService } from "../services/session.service";
import { ThemeService } from "../services/theme.service";

interface MenuItem {
  path: string;
  label: string;
  icon: string;
  mobile?: boolean;
}

/**
 * Shell da área autenticada: sidebar retrátil no desktop e navegação inferior no mobile.
 * As páginas mantêm seus próprios cabeçalhos (identidade Premium 3.1) e cuidam do conteúdo.
 */
@Component({
  selector: "app-shell",
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: "./app-shell.component.html",
  styleUrl: "./app-shell.component.css",
})
export class AppShellComponent implements OnInit {
  readonly menu: MenuItem[] = [
    { path: "/home", label: "Início", icon: "M3 10.5 12 3l9 7.5V21H3z", mobile: true },
    { path: "/tarefas", label: "Tarefas", icon: "M4 6h16M4 12h16M4 18h10", mobile: true },
    { path: "/planejamento", label: "Planejamento", icon: "M4 5h16v16H4zM8 3v4M16 3v4M4 10h16", mobile: true },
    { path: "/foco", label: "Foco", icon: "M12 7v5l3 3M21 12a9 9 0 1 1-9-9 9 9 0 0 1 9 9z", mobile: true },
    { path: "/habitos", label: "Hábitos", icon: "M4 12l4 4L20 4M4 20h16" },
    { path: "/revisao", label: "Revisão", icon: "M4 19V5M4 19h16M8 15l3-4 3 2 4-6" },
    { path: "/categorias", label: "Categorias", icon: "M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z" },
    { path: "/lixeira", label: "Lixeira", icon: "M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6M10 11v5M14 11v5" },
    { path: "/perfil", label: "Perfil", icon: "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 21a8 8 0 0 1 16 0", mobile: true },
  ];

  readonly mobileMenu = this.menu.filter((item) => item.mobile);

  collapsed = false;
  nome = "você";
  papel = "Usuário";
  admin = false;

  constructor(
    public session: SessionService,
    public theme: ThemeService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const usuario = this.session.usuario();
    this.nome = usuario?.nome?.split(" ")[0] || "você";
    this.papel =
      usuario?.papel === "SUPER_ADMIN" ? "Super admin" : usuario?.papel === "ADMIN" ? "Admin" : "Usuário";
    this.admin = usuario?.papel === "ADMIN" || usuario?.papel === "SUPER_ADMIN";
    this.collapsed = localStorage.getItem("organiza-sidebar-collapsed") === "1";
  }

  alternarSidebar(): void {
    this.collapsed = !this.collapsed;
    localStorage.setItem("organiza-sidebar-collapsed", this.collapsed ? "1" : "0");
  }

  sair(): void {
    this.session.limpar();
    void this.router.navigate(["/login"]);
  }

  @HostListener("document:keydown.escape")
  fecharMenu(): void {
    // Reservado para drawers futuros; mantém o comportamento de Escape consistente.
  }
}
