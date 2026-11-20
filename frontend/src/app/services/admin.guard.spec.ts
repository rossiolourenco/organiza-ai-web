import { TestBed } from '@angular/core/testing';
import { provideRouter, UrlTree } from '@angular/router';

import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  type Papel = 'USER' | 'ADMIN' | 'SUPER_ADMIN';

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  const executar = () => TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

  const autenticar = (papel: Papel) => {
    localStorage.setItem('organiza-token', 'token-de-teste');
    localStorage.setItem('usuario', JSON.stringify({
      id: 1, nome: 'Teste', email: 'teste@example.invalid', papel,
    }));
  };

  it('redireciona visitante para o login administrativo', () => {
    const resultado = executar();
    expect(resultado instanceof UrlTree).toBeTrue();
    expect((resultado as UrlTree).queryParams['admin']).toBe('1');
  });

  it('bloqueia usuário comum autenticado', () => {
    autenticar('USER');
    const resultado = executar();
    expect(resultado instanceof UrlTree).toBeTrue();
    expect((resultado as UrlTree).queryParams['admin']).toBe('1');
  });

  it('permite ADMIN', () => {
    autenticar('ADMIN');
    expect(executar()).toBeTrue();
  });

  it('permite SUPER_ADMIN', () => {
    autenticar('SUPER_ADMIN');
    expect(executar()).toBeTrue();
  });
});
