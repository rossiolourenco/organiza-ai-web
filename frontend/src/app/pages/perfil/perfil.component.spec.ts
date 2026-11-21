import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { PerfilComponent } from './perfil.component';
import { SessionService } from '../../services/session.service';

describe('PerfilComponent', () => {
  let component: PerfilComponent;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('usuario', JSON.stringify({ id: 1, nome: 'DEMO Teste', email: 'demo@teste.invalid' }));
    localStorage.setItem('organiza-token', 'token');
    await TestBed.configureTestingModule({
      imports: [PerfilComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    const fixture = TestBed.createComponent(PerfilComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(r => r.url.includes('/api/usuarios/perfil')).flush({
      id: 1, nome: 'DEMO Teste', email: 'demo@teste.invalid', papel: 'USER', status: 'ATIVO',
      deveTrocarSenha: false, dataCriacao: '2026-11-18T10:00:00'
    });
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('carrega o perfil autenticado sem expor senha', () => {
    expect(component.nome).toBe('DEMO Teste');
    expect(component.email).toBe('demo@teste.invalid');
    expect(component.papelLabel).toBe('Usuário');
  });

  it('valida confirmação divergente antes de chamar a API', () => {
    component.senhaAtual = 'Senha-atual-2026';
    component.novaSenha = 'Nova-senha-2026';
    component.confirmarSenha = 'Outra-senha-2026';
    component.alterarSenha();
    expect(component.erroSenha).toContain('confere');
    http.expectNone(r => r.url.endsWith('/api/usuarios/senha'));
  });

  it('altera a senha, limpa a sessão e volta ao login', () => {
    const session = TestBed.inject(SessionService);
    const router = TestBed.inject(Router);
    const limpar = spyOn(session, 'limpar');
    const navegar = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.senhaAtual = 'Senha-atual-2026';
    component.novaSenha = 'Nova-senha-2026';
    component.confirmarSenha = 'Nova-senha-2026';
    component.alterarSenha();

    const req = http.expectOne(r => r.url.endsWith('/api/usuarios/senha'));
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ senhaAtual: 'Senha-atual-2026', novaSenha: 'Nova-senha-2026' });
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(limpar).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: { senha: 'alterada' } });
  });

  it('exige EXCLUIR e a senha antes de excluir a conta', () => {
    component.abrirExclusao();
    component.confirmarExclusao();
    expect(component.erroExclusao).toContain('senha');

    component.senhaExclusao = 'Senha-atual-2026';
    component.confirmacaoExclusao = 'excluir agora';
    component.confirmarExclusao();
    expect(component.erroExclusao).toContain('EXCLUIR');
    http.expectNone(r => r.url.endsWith('/api/usuarios/conta'));
  });

  it('exclui a conta, limpa a sessão e volta ao login', () => {
    const session = TestBed.inject(SessionService);
    const router = TestBed.inject(Router);
    const limpar = spyOn(session, 'limpar');
    const navegar = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.abrirExclusao();
    component.senhaExclusao = 'Senha-atual-2026';
    component.confirmacaoExclusao = 'EXCLUIR';
    component.confirmarExclusao();

    const req = http.expectOne(r => r.url.endsWith('/api/usuarios/conta'));
    expect(req.request.method).toBe('DELETE');
    expect(req.request.body).toEqual({ senha: 'Senha-atual-2026' });
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(limpar).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: { conta: 'excluida' } });
  });
});
