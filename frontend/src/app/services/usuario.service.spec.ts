import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';

import { UsuarioService } from './usuario.service';

describe('UsuarioService', () => {
  let service: UsuarioService;

  beforeEach(() => {
    localStorage.removeItem('organiza-token'); sessionStorage.removeItem('organiza-token');
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(), provideRouter([])
    ] });
    service = TestBed.inject(UsuarioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
  afterEach(() => {
    localStorage.removeItem('organiza-token');
    sessionStorage.removeItem('organiza-token');
  });

  it('keeps the login shape while separating the credential and authenticating API calls', () => {
    const http = TestBed.inject(HttpTestingController);
    service.login('teste@demo.invalid', 'senha').subscribe(user => {
      expect(user).toEqual({ id: 1, nome: 'DEMO', email: 'teste@demo.invalid' });
    });
    http.expectOne('http://localhost:8080/api/usuarios/login').flush({
      id: 1, nome: 'DEMO', email: 'teste@demo.invalid', token: 'test-only-token'
    });
    TestBed.inject(HttpClient).get('http://localhost:8080/api/tarefas?usuarioId=1').subscribe();
    const request = http.expectOne('http://localhost:8080/api/tarefas?usuarioId=1');
    expect(request.request.headers.get('Authorization')).toBe('Bearer test-only-token');
    request.flush([]);
    http.verify();
  });
});
