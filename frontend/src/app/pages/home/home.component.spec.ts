import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    localStorage.setItem('usuario', JSON.stringify({ id: 1, nome: 'DEMO Teste' }));
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
  afterEach(() => localStorage.removeItem('usuario'));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads once and combines inclusive deadline filters without re-fetching', () => {
    component.statusSelecionado = 'PENDENTE';
    component.dataInicial = '2026-11-01';
    component.dataFinal = '2026-11-30';
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(r => r.url.includes('/api/tarefas')).flush([
      { id: 1, titulo: 'Dentro', status: 'PENDENTE', prioridade: 'ALTA', dataPrazo: '2026-11-30' },
      { id: 2, titulo: 'Fora', status: 'PENDENTE', prioridade: 'ALTA', dataPrazo: '2026-12-01' },
      { id: 3, titulo: 'Concluída', status: 'CONCLUIDA', prioridade: 'ALTA', dataPrazo: '2026-11-10' },
    ]);
    expect(component.tarefas.map(t => t.id)).toEqual([1]);
    http.verify();
  });

  it('reabre tarefa concluída via PUT preservando os dados do cartão', () => {
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(r => r.url.includes('/api/tarefas')).flush([
      { id: 9, titulo: 'Reabrir', status: 'CONCLUIDA', prioridade: 'ALTA',
        usuario: { id: 1 }, categoria: { id: 2, nome: 'Trabalho' } },
    ]);
    component.reabrirTarefa(component.tarefas[0]);
    const req = http.expectOne(r => r.url.includes('/api/tarefas/9') && r.method === 'PUT');
    expect(req.request.body.status).toBe('PENDENTE');
    expect(req.request.body.titulo).toBe('Reabrir');
    expect(req.request.body.categoria).toEqual({ id: 2 });
    req.flush({ id: 9, titulo: 'Reabrir', status: 'PENDENTE', prioridade: 'ALTA' });
    http.expectOne(r => r.url.includes('/api/tarefas')).flush([]);
  });
});
