import { Tarefa } from "./tarefa.service";

/** Data civil local (AAAA-MM-DD) sem deslocamento de UTC. */
export function dayKey(date: Date = new Date()): string {
  const ano = date.getFullYear();
  const mes = `${date.getMonth() + 1}`.padStart(2, "0");
  const dia = `${date.getDate()}`.padStart(2, "0");
  return `${ano}-${mes}-${dia}`;
}

export function addDays(date: string, dias: number): string {
  const [a, m, d] = date.split("-").map(Number);
  return dayKey(new Date(a, m - 1, d + dias));
}

export function weekStart(date: string): string {
  const [a, m, d] = date.split("-").map(Number);
  const base = new Date(a, m - 1, d);
  const offset = (base.getDay() + 6) % 7; // segunda-feira
  return addDays(date, -offset);
}

export interface NextAction {
  tarefa: Tarefa;
  motivo: string;
}

const prioridadeRank: Record<string, number> = { ALTA: 0, MEDIA: 1, BAIXA: 2 };

/** Próxima melhor ação determinística: atrasadas > vence hoje > alta prioridade. */
export function nextBestAction(tarefas: Tarefa[], hoje: string = dayKey()): NextAction | null {
  const abertas = tarefas.filter((t) => t.status !== "CONCLUIDA");
  if (!abertas.length) return null;

  const ordenar = (lista: Tarefa[]) =>
    [...lista].sort((a, b) =>
      (prioridadeRank[a.prioridade] ?? 9) - (prioridadeRank[b.prioridade] ?? 9) ||
      (a.dataPrazo || "9999").localeCompare(b.dataPrazo || "9999") || (a.id ?? 0) - (b.id ?? 0));

  const atrasadas = abertas.filter((t) => !!t.dataPrazo && t.dataPrazo < hoje);
  if (atrasadas.length) {
    return { tarefa: ordenar(atrasadas)[0], motivo: "Está atrasada — resolva primeiro para liberar o restante." };
  }

  const hojeTasks = abertas.filter((t) => t.dataPrazo === hoje);
  if (hojeTasks.length) {
    return { tarefa: ordenar(hojeTasks)[0], motivo: "Vence hoje. Um passo agora evita correr no fim do dia." };
  }

  const semPrazo = abertas.filter((t) => !t.dataPrazo);
  if (semPrazo.length) {
    return { tarefa: ordenar(semPrazo)[0], motivo: "Sem prazo definido — comece pela de maior prioridade." };
  }

  return { tarefa: ordenar(abertas)[0], motivo: "Próxima na ordem de prioridade e prazo." };
}

/** Série de 7 dias com tarefas concluídas por dia (segunda a domingo). */
export function weeklyCompleted(tarefas: Tarefa[], hoje: string = dayKey()): { data: string; total: number }[] {
  const inicio = weekStart(hoje);
  return Array.from({ length: 7 }, (_, i) => {
    const data = addDays(inicio, i);
    const total = tarefas.filter((t) =>
      (t.conclusoes || []).some((iso) => dayKey(new Date(iso)) === data)).length;
    return { data, total };
  });
}

export function completionRate(tarefas: Tarefa[]): number {
  if (!tarefas.length) return 0;
  return Math.round((tarefas.filter((t) => t.status === "CONCLUIDA").length / tarefas.length) * 100);
}

export function todayProgress(tarefas: Tarefa[], hoje: string = dayKey()): { total: number; feitas: number; pct: number } {
  const doDia = tarefas.filter((t) => t.dataPrazo === hoje);
  const feitas = doDia.filter((t) => t.status === "CONCLUIDA").length;
  const pct = doDia.length ? Math.round((feitas / doDia.length) * 100) : 0;
  return { total: doDia.length, feitas, pct };
}

/** Sequência de dias consecutivos com registro, contando a partir de hoje (ou ontem). */
export function habitStreak(datas: string[], hoje: string = dayKey()): number {
  const conjunto = new Set(datas);
  let cursor = conjunto.has(hoje) ? hoje : addDays(hoje, -1);
  let sequencia = 0;
  while (conjunto.has(cursor)) {
    sequencia++;
    cursor = addDays(cursor, -1);
  }
  return sequencia;
}
