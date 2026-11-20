import { Tarefa } from "./tarefa.service";
import {
  addDays,
  completionRate,
  dayKey,
  habitStreak,
  nextBestAction,
  todayProgress,
  weeklyCompleted,
  weekStart,
} from "./productivity";

function tarefa(parcial: Partial<Tarefa>): Tarefa {
  return { id: 1, titulo: "Tarefa", prioridade: "MEDIA", status: "PENDENTE", ...parcial };
}

describe("productivity", () => {
  it("calcula a data civil local sem deslocar UTC", () => {
    expect(dayKey(new Date(2026, 10, 20, 23, 30))).toBe("2026-11-20");
    expect(addDays("2026-11-20", 1)).toBe("2026-11-21");
    expect(addDays("2026-11-01", -1)).toBe("2026-10-31");
  });

  it("inicia a semana na segunda-feira", () => {
    expect(weekStart("2026-11-20")).toBe("2026-11-16");
    expect(weekStart("2026-11-16")).toBe("2026-11-16");
  });

  it("prioriza atrasadas e depois o que vence hoje", () => {
    const hoje = "2026-11-20";
    const lista = [
      tarefa({ id: 1, titulo: "Futura", dataPrazo: "2026-11-25", prioridade: "ALTA" }),
      tarefa({ id: 2, titulo: "Hoje média", dataPrazo: hoje, prioridade: "MEDIA" }),
      tarefa({ id: 3, titulo: "Atrasada baixa", dataPrazo: "2026-11-18", prioridade: "BAIXA" }),
    ];

    const acao = nextBestAction(lista, hoje);
    expect(acao?.tarefa.id).toBe(3);
    expect(acao?.motivo).toContain("atrasada");

    const semAtraso = lista.filter((t) => t.id !== 3);
    expect(nextBestAction(semAtraso, hoje)?.tarefa.id).toBe(2);
  });

  it("não sugere tarefa quando tudo está concluído", () => {
    expect(nextBestAction([tarefa({ status: "CONCLUIDA" })], "2026-11-20")).toBeNull();
  });

  it("calcula progresso do dia e taxa geral", () => {
    const lista = [
      tarefa({ id: 1, dataPrazo: "2026-11-20", status: "CONCLUIDA" }),
      tarefa({ id: 2, dataPrazo: "2026-11-20", status: "PENDENTE" }),
      tarefa({ id: 3, dataPrazo: "2026-11-21", status: "CONCLUIDA" }),
    ];

    expect(todayProgress(lista, "2026-11-20")).toEqual({ total: 2, feitas: 1, pct: 50 });
    expect(completionRate(lista)).toBe(67);
  });

  it("conta conclusões por dia da semana usando o histórico", () => {
    const lista = [
      tarefa({ id: 1 }),
      tarefa({ id: 2, conclusoes: ["2026-11-20T12:00:00Z", "2026-11-19T12:00:00Z"] }),
    ];

    const semana = weeklyCompleted(lista, "2026-11-20");
    expect(semana.length).toBe(7);
    expect(semana.find((d) => d.data === "2026-11-20")?.total).toBe(1);
    expect(semana.find((d) => d.data === "2026-11-19")?.total).toBe(1);
  });

  it("calcula sequência de hábito contando hoje ou ontem", () => {
    const datas = ["2026-11-20", "2026-11-19", "2026-11-18"];
    expect(habitStreak(datas, "2026-11-20")).toBe(3);
    expect(habitStreak(["2026-11-19", "2026-11-18"], "2026-11-20")).toBe(2);
    expect(habitStreak([], "2026-11-20")).toBe(0);
  });
});
