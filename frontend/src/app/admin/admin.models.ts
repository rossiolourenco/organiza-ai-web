export type Papel = 'USER' | 'ADMIN' | 'SUPER_ADMIN';
export type StatusConta = 'ATIVO' | 'BLOQUEADO' | 'DESATIVADO' | 'ANONIMIZADO';

export interface UsuarioSessao {
  id: number;
  nome: string;
  email: string;
  papel?: Papel;
  status?: StatusConta;
  deveTrocarSenha?: boolean;
}

export interface UsuarioAdmin {
  id: number;
  nome: string;
  email: string;
  papel: Papel;
  status: StatusConta;
  dataCriacao: string;
  ultimoLoginEm: string | null;
  bloqueadoAte: string | null;
  motivoBloqueio: string | null;
  deveTrocarSenha: boolean;
  sessoesAtivas: number;
}

export interface SessaoResumo {
  criadoEm: string;
  ultimoUsoEm: string | null;
  expiraEm: string;
  ip: string | null;
  userAgent: string | null;
}

export interface EventoAuditoria {
  id: number;
  criadoEm: string;
  atorId: number | null;
  atorEmail: string | null;
  atorPapel: string | null;
  acao: string;
  recurso: string | null;
  recursoId: string | null;
  resultado: string;
  ip: string | null;
  userAgent: string | null;
  detalhes: string | null;
  correlacao: string | null;
}

export interface UsuarioDetalhe {
  usuario: UsuarioAdmin;
  sessoes: SessaoResumo[];
  eventosRecentes: EventoAuditoria[];
}

export interface Pagina<T> {
  content: T[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface Politica {
  senhaMinima: number;
  exigirNumero: boolean;
  exigirMaiuscula: boolean;
  maxTentativas: number;
  janelaMinutos: number;
  bloqueioMinutos: number;
  sessaoDias: number;
  atualizadoEm: string | null;
  atualizadoPor: string | null;
}

export interface TentativaLogin {
  id: number;
  email: string;
  sucesso: boolean;
  ip: string | null;
  userAgent: string | null;
  motivo: string | null;
  criadoEm: string;
}

export interface Alerta {
  nivel: 'CRITICO' | 'ATENCAO' | 'INFO' | string;
  titulo: string;
  detalhe: string;
}

export interface SegurancaOverview {
  contasAtivas: number;
  contasBloqueadas: number;
  falhas24h: number;
  sucessos24h: number;
  sessoesAtivas: number;
  alertas: Alerta[];
  ultimosEventos: EventoAuditoria[];
  politica: Politica;
}

export interface FirewallRegra {
  id: number;
  nome: string;
  descricao: string | null;
  ipCidr: string;
  porta: number | null;
  protocolo: 'TCP' | 'UDP' | 'ANY';
  direcao: 'INBOUND' | 'OUTBOUND';
  acao: 'ALLOW' | 'DENY';
  prioridade: number;
  ativa: boolean;
  criadoEm: string;
  atualizadoEm: string | null;
  criadoPor: string | null;
  atualizadoPor: string | null;
}

export interface FirewallEstado {
  modo: string;
  demo: boolean;
  aplicado: boolean;
  aplicadoEm: string | null;
  snapshotAtualId: number | null;
  totalRegras: number;
  regrasAtivas: number;
}

export interface FirewallConflito {
  regraA: number;
  nomeA: string;
  regraB: number;
  nomeB: string;
  motivo: string;
}

export interface FirewallValidacao {
  valido: boolean;
  conflitos: FirewallConflito[];
}

export interface FirewallHistorico {
  id: number;
  criadoEm: string;
  atorEmail: string | null;
  operacao: string;
  regraId: number | null;
  detalhes: string | null;
  resultado: string;
}

export interface Monitor {
  coletadoEm: string;
  uptimeSegundos: number;
  backendDisponivel: boolean;
  banco: { disponivel: boolean; latenciaMs: number | null; detalhe: string | null };
  cpu: { cargaSistema: number | null; cargaProcesso: number | null; nucleos: number; loadAverage: number | null };
  memoria: {
    heapUsadoBytes: number; heapMaximoBytes: number; heapCommitadoBytes: number;
    sistemaTotalBytes: number; sistemaLivreBytes: number;
  };
  disco: { disponivel: boolean; totalBytes: number | null; livreBytes: number | null; caminho: string | null };
  http: { totalRequisicoes: number; erros4xx: number; erros5xx: number; tempoMedioMs: number };
  sessoesAtivas: number;
  firewall: { modo: string; aplicado: boolean; aplicadoEm: string | null; regrasAtivas: number };
  usuariosAtivos: number;
  usuariosBloqueados: number;
}

export interface AdminMe {
  id: number;
  nome: string;
  email: string;
  papel: Papel;
  permissoes: string[];
}

export interface SenhaTemporaria {
  usuarioId: number;
  email: string;
  senhaTemporaria: string;
  aviso: string;
}
