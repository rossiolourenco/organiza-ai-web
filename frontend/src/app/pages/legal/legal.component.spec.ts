import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { LegalComponent } from './legal.component';

function criar(tipo: 'privacidade' | 'termos') {
  TestBed.configureTestingModule({
    imports: [LegalComponent],
    providers: [provideRouter([]), { provide: ActivatedRoute, useValue: { snapshot: { data: { tipo } } } }],
  });
  const fixture = TestBed.createComponent(LegalComponent);
  fixture.detectChanges();
  return fixture;
}

describe('LegalComponent', () => {
  it('renderiza a política com o controlador e o canal de contato', () => {
    const fixture = criar('privacidade');
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Política de Privacidade');
    expect(texto).toContain('Evox Sistemas Digitais LTDA');
    expect(texto).toContain('21.543.966/0001-45');
    expect(texto).toContain('contato@evox.net.br');
    expect(texto).toContain('15 dias');
  });

  it('renderiza os termos com aceitação e contato', () => {
    const fixture = criar('termos');
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Termos de Uso');
    expect(texto).toContain('Aceitação');
    expect(texto).toContain('13 anos');
  });
});
