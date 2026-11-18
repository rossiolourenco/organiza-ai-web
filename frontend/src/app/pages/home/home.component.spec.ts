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
});
