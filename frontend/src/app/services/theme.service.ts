import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly chave = 'organiza-admin-theme';
  dark = false;

  constructor() {
    const salvo = localStorage.getItem(this.chave);
    this.dark = salvo ? salvo === 'dark' : window.matchMedia?.('(prefers-color-scheme: dark)').matches === true;
    this.aplicar();
  }

  alternar(): void {
    this.dark = !this.dark;
    localStorage.setItem(this.chave, this.dark ? 'dark' : 'light');
    this.aplicar();
  }

  private aplicar(): void {
    document.documentElement.setAttribute('data-theme', this.dark ? 'dark' : 'light');
  }
}
