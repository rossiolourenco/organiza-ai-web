import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from './api.config';
import {
  AdminMe, EventoAuditoria, FirewallEstado, FirewallHistorico, FirewallRegra, FirewallValidacao,
  Monitor, Pagina, Politica, SegurancaOverview, SenhaTemporaria, TentativaLogin, UsuarioAdmin,
  UsuarioDetalhe,
} from '../admin/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly base = `${API_BASE}/admin`;

  constructor(private http: HttpClient) {}

  me(): Observable<AdminMe> {
    return this.http.get<AdminMe>(`${this.base}/usuarios/me`);
  }

  usuarios(filtros: { q?: string; status?: string; papel?: string; page?: number; size?: number }): Observable<Pagina<UsuarioAdmin>> {
    let params = new HttpParams();
    Object.entries(filtros).forEach(([chave, valor]) => {
      if (valor !== undefined && valor !== null && valor !== '') params = params.set(chave, String(valor));
    });
    return this.http.get<Pagina<UsuarioAdmin>>(`${this.base}/usuarios`, { params });
  }

  usuario(id: number): Observable<UsuarioDetalhe> {
    return this.http.get<UsuarioDetalhe>(`${this.base}/usuarios/${id}`);
  }

  criarUsuario(dados: { nome: string; email: string; papel: string }): Observable<SenhaTemporaria> {
    return this.http.post<SenhaTemporaria>(`${this.base}/usuarios`, dados);
  }

  alterarStatus(id: number, status: string, motivo?: string): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`${this.base}/usuarios/${id}/status`, { status, motivo });
  }

  alterarPapel(id: number, papel: string): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`${this.base}/usuarios/${id}/papel`, { papel });
  }

  revogarSessoes(id: number): Observable<{ revogadas: number }> {
    return this.http.post<{ revogadas: number }>(`${this.base}/usuarios/${id}/revogar-sessoes`, {});
  }

  redefinirSenha(id: number): Observable<SenhaTemporaria> {
    return this.http.post<SenhaTemporaria>(`${this.base}/usuarios/${id}/redefinir-senha`, {});
  }

  anonimizar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/usuarios/${id}`);
  }

  seguranca(): Observable<SegurancaOverview> {
    return this.http.get<SegurancaOverview>(`${this.base}/seguranca/overview`);
  }

  tentativas(filtros: { email?: string; sucesso?: string; desde?: string; page?: number; size?: number }): Observable<Pagina<TentativaLogin>> {
    let params = new HttpParams();
    Object.entries(filtros).forEach(([chave, valor]) => {
      if (valor !== undefined && valor !== null && valor !== '') params = params.set(chave, String(valor));
    });
    return this.http.get<Pagina<TentativaLogin>>(`${this.base}/seguranca/tentativas`, { params });
  }

  politica(): Observable<Politica> {
    return this.http.get<Politica>(`${this.base}/seguranca/politica`);
  }

  salvarPolitica(politica: Partial<Politica>): Observable<Politica> {
    return this.http.put<Politica>(`${this.base}/seguranca/politica`, politica);
  }

  auditoria(filtros: { acao?: string; ator?: string; desde?: string; ate?: string; page?: number; size?: number }): Observable<Pagina<EventoAuditoria>> {
    let params = new HttpParams();
    Object.entries(filtros).forEach(([chave, valor]) => {
      if (valor !== undefined && valor !== null && valor !== '') params = params.set(chave, String(valor));
    });
    return this.http.get<Pagina<EventoAuditoria>>(`${this.base}/auditoria`, { params });
  }

  firewallEstado(): Observable<FirewallEstado> {
    return this.http.get<FirewallEstado>(`${this.base}/firewall/estado`);
  }

  firewallRegras(): Observable<FirewallRegra[]> {
    return this.http.get<FirewallRegra[]>(`${this.base}/firewall/regras`);
  }

  criarRegra(regra: Partial<FirewallRegra>): Observable<FirewallRegra> {
    return this.http.post<FirewallRegra>(`${this.base}/firewall/regras`, regra);
  }

  atualizarRegra(id: number, regra: Partial<FirewallRegra>): Observable<FirewallRegra> {
    return this.http.put<FirewallRegra>(`${this.base}/firewall/regras/${id}`, regra);
  }

  alternarRegra(id: number, ativa: boolean): Observable<FirewallRegra> {
    return this.http.patch<FirewallRegra>(`${this.base}/firewall/regras/${id}/ativa?ativa=${ativa}`, {});
  }

  excluirRegra(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/firewall/regras/${id}`);
  }

  validarFirewall(): Observable<FirewallValidacao> {
    return this.http.get<FirewallValidacao>(`${this.base}/firewall/validar`);
  }

  aplicarFirewall(motivo: string): Observable<FirewallEstado> {
    return this.http.post<FirewallEstado>(`${this.base}/firewall/aplicar`, { motivo });
  }

  rollbackFirewall(): Observable<FirewallEstado> {
    return this.http.post<FirewallEstado>(`${this.base}/firewall/rollback`, {});
  }

  resetFirewall(): Observable<FirewallEstado> {
    return this.http.post<FirewallEstado>(`${this.base}/firewall/reset-demo`, {});
  }

  firewallHistorico(): Observable<FirewallHistorico[]> {
    return this.http.get<FirewallHistorico[]>(`${this.base}/firewall/historico`);
  }

  monitoramento(): Observable<Monitor> {
    return this.http.get<Monitor>(`${this.base}/monitoramento/overview`);
  }
}
