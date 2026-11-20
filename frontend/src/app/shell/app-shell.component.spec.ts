import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Router, provideRouter } from "@angular/router";

import { AppShellComponent } from "./app-shell.component";
import { SessionService } from "../services/session.service";
import { ThemeService } from "../services/theme.service";

describe("AppShellComponent", () => {
  let fixture: ComponentFixture<AppShellComponent>;
  let router: Router;
  let limparChamado: boolean;

  async function montar(papel: "USER" | "ADMIN" | "SUPER_ADMIN" = "USER"): Promise<void> {
    limparChamado = false;
    await TestBed.configureTestingModule({
      imports: [AppShellComponent],
      providers: [
        provideRouter([]),
        {
          provide: SessionService,
          useValue: {
            usuario: () => ({ id: 1, nome: "Ana Silva", email: "ana@example.invalid", papel }),
            limpar: () => {
              limparChamado = true;
            },
          },
        },
        { provide: ThemeService, useValue: { dark: false, alternar: () => undefined } },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, "navigate").and.resolveTo(true);
    localStorage.removeItem("organiza-sidebar-collapsed");
    fixture = TestBed.createComponent(AppShellComponent);
    fixture.detectChanges();
  }

  function elemento(): HTMLElement {
    return fixture.nativeElement as HTMLElement;
  }

  it("renderiza a navegação completa e a identidade Premium", async () => {
    await montar();

    expect(elemento().querySelector(".brand-text")?.textContent).toContain("Premium Experience");
    const links = Array.from(elemento().querySelectorAll(".admin-nav a")) as HTMLAnchorElement[];
    expect(links.map((a) => a.textContent?.trim())).toEqual([
      "Início", "Tarefas", "Planejamento", "Foco", "Hábitos", "Revisão", "Categorias", "Lixeira", "Perfil",
    ]);
  });

  it("mostra a navegação inferior no mobile com cinco itens", async () => {
    await montar();

    const itens = Array.from(elemento().querySelectorAll(".user-bottom-nav a")) as HTMLAnchorElement[];
    expect(itens.map((a) => a.textContent?.trim())).toEqual([
      "Início", "Tarefas", "Planejamento", "Foco", "Perfil",
    ]);
  });

  it("recolhe a sidebar e persiste a preferência", async () => {
    await montar();

    expect(elemento().querySelector(".admin-shell")?.classList.contains("collapsed")).toBeFalse();
    (elemento().querySelector(".sidebar-toggle") as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(elemento().querySelector(".admin-shell")?.classList.contains("collapsed")).toBeTrue();
    expect(localStorage.getItem("organiza-sidebar-collapsed")).toBe("1");
  });

  it("só exibe o atalho de Admin para papéis administrativos", async () => {
    await montar("USER");
    const temAdmin = () =>
      Array.from(elemento().querySelectorAll(".admin-nav a")).some((a) => a.textContent?.trim() === "Admin");
    expect(temAdmin()).toBeFalse();

    TestBed.resetTestingModule();
    await montar("ADMIN");
    expect(temAdmin()).toBeTrue();
  });

  it("encerra a sessão ao sair", async () => {
    await montar();

    (elemento().querySelector('button[aria-label="Sair"]') as HTMLButtonElement).click();

    expect(limparChamado).toBeTrue();
    expect(router.navigate).toHaveBeenCalledWith(["/login"]);
  });
});
