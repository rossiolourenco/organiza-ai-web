import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, provideRouter } from "@angular/router";
import { of } from "rxjs";

import { HomeComponent } from "./home.component";
import { Tarefa, TarefaService } from "../../services/tarefa.service";
import { SessionService } from "../../services/session.service";
import { ThemeService } from "../../services/theme.service";

/**
 * Guarda de regressão da interface Premium da Home (redesenho 3.1).
 *
 * Valida a estrutura real renderizada (DOM + comportamento), não apenas a presença de textos no
 * template. Se alguém trocar a Home pelo layout acadêmico ou remover topbar, indicadores, filtros,
 * cards ou o modal acessível, estes testes falham e o CI bloqueia o merge.
 */
describe("HomeComponent — estrutura Premium", () => {
  let fixture: ComponentFixture<HomeComponent>;
  let componente: HomeComponent;

  const tarefas: Tarefa[] = [
    { id: 1, titulo: "Revisar proposta", descricao: "Conferir escopo", prioridade: "ALTA",
      status: "PENDENTE", dataPrazo: "2026-11-21", usuario: { id: 1 } },
    { id: 2, titulo: "Planejar semana", descricao: "Definir prioridades", prioridade: "MEDIA",
      status: "PENDENTE", dataPrazo: "2026-11-22", usuario: { id: 1 } },
    { id: 3, titulo: "Estudar Angular", descricao: "Revisar signals", prioridade: "BAIXA",
      status: "EM_ANDAMENTO", dataPrazo: "2026-11-25",
      categoria: { id: 7, nome: "Estudos" }, usuario: { id: 1 } },
  ];

  async function montar(lista: Tarefa[]): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([]),
        {
          provide: TarefaService,
          useValue: {
            listarTodas: () => of(lista),
            concluir: () => of({}),
            atualizar: () => of({}),
            excluir: () => of(void 0),
          },
        },
        {
          provide: SessionService,
          useValue: {
            usuario: () => ({ id: 1, nome: "Ana Silva", email: "ana@example.invalid", papel: "USER" }),
            limpar: () => undefined,
          },
        },
        { provide: ThemeService, useValue: { dark: false, alternar: () => undefined } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => null } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    componente = fixture.componentInstance;
    fixture.detectChanges();
  }

  function elemento(): HTMLElement {
    return fixture.nativeElement as HTMLElement;
  }

  it("mantém a hierarquia da tela de tarefas: eyebrow, saudação e ação principal", async () => {
    await montar(tarefas);

    expect(elemento().querySelector(".eyebrow")?.textContent?.trim()).toBe("TAREFAS");
    expect(elemento().querySelector(".welcome h1")?.textContent).toContain("Suas tarefas");
    expect(elemento().querySelector(".welcome .welcome-sub")?.textContent).toContain("Menos ruído");
    expect(elemento().querySelector(".primary-action")?.textContent).toContain("Nova tarefa");
  });

  it("renderiza os quatro indicadores com valores derivados das tarefas", async () => {
    await montar(tarefas);

    const cards = Array.from(elemento().querySelectorAll(".stats .stat-card")) as HTMLElement[];
    expect(cards.length).toBe(4);

    const valores = cards.map(card => card.querySelector("div strong")?.textContent?.trim());
    const rotulos = cards.map(card => {
      const spans = Array.from(card.querySelectorAll("span"));
      return spans[spans.length - 1]?.textContent?.trim();
    });

    expect(valores).toEqual(["3", "2", "1", "0"]);
    expect(rotulos).toEqual(["Total de tarefas", "Pendentes", "Em andamento", "Concluídas"]);
  });

  it("mantém o card de filtros com os quatro campos", async () => {
    await montar(tarefas);

    const filtros = elemento().querySelector(".filters-card");
    expect(filtros).not.toBeNull();
    expect(filtros?.querySelectorAll(".filters-grid .field").length).toBe(4);

    const rotulos = Array.from(filtros!.querySelectorAll(".field label"))
      .map(label => label.textContent?.trim());
    expect(rotulos).toEqual(["Status", "Prioridade", "Prazo de", "Prazo até"]);
  });

  it("renderiza os cards de tarefa com status, prioridade e categoria", async () => {
    await montar(tarefas);

    const cards = Array.from(elemento().querySelectorAll(".tarefas-grid .tarefa-card")) as HTMLElement[];
    expect(cards.length).toBe(3);
    expect(cards[0].querySelector(".status-badge")?.textContent?.trim()).toBe("Pendente");
    expect(cards[0].querySelector(".prioridade")?.textContent?.toUpperCase()).toContain("ALTA");
    expect(cards[2].querySelector(".categoria")?.textContent?.trim()).toBe("Estudos");
  });

  it("filtra por status sem perder a estrutura do dashboard", async () => {
    await montar(tarefas);

    componente.statusSelecionado = "EM_ANDAMENTO";
    componente.aplicarFiltros();
    fixture.detectChanges();

    expect(elemento().querySelectorAll(".tarefas-grid .tarefa-card").length).toBe(1);
    expect(elemento().querySelector(".stats .stat-card")).not.toBeNull();
  });

  it("mostra o estado vazio Premium quando não há tarefas", async () => {
    await montar([]);

    expect(elemento().querySelector(".stats")).not.toBeNull();
    expect(elemento().querySelector(".estado-vazio h3")?.textContent).toContain("Seu espaço está pronto");
  });

  it("mantém o modal de exclusão acessível e fecha com Escape", async () => {
    await montar(tarefas);

    componente.pedirExclusao(tarefas[0]);
    fixture.detectChanges();

    const modal = elemento().querySelector('[role="alertdialog"]') as HTMLElement;
    expect(modal).not.toBeNull();
    expect(modal.getAttribute("aria-modal")).toBe("true");
    expect(modal.getAttribute("aria-labelledby")).toBe("titulo-excluir");

    document.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape" }));
    fixture.detectChanges();

    expect(elemento().querySelector('[role="alertdialog"]')).toBeNull();
  });
});
