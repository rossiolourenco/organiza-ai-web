import { Injectable } from '@angular/core';
import { Papel, UsuarioSessao } from '../admin/admin.models';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly usuarioKey = 'usuario';

  usuario(): UsuarioSessao | null {
    try {
      const bruto = localStorage.getItem(this.usuarioKey);
      return bruto ? JSON.parse(bruto) as UsuarioSessao : null;
    } catch { return null; }
  }

  token(): string | null {
    return localStorage.getItem('organiza-token') || sessionStorage.getItem('organiza-token');
  }

  papel(): Papel {
    return this.usuario()?.papel ?? 'USER';
  }

  isAdmin(): boolean {
    const papel = this.papel();
    return papel === 'ADMIN' || papel === 'SUPER_ADMIN';
  }

  isSuperAdmin(): boolean {
    return this.papel() === 'SUPER_ADMIN';
  }

  salvar(usuario: UsuarioSessao): void {
    localStorage.setItem(this.usuarioKey, JSON.stringify(usuario));
  }

  limpar(): void {
    localStorage.removeItem('organiza-token');
    sessionStorage.removeItem('organiza-token');
    localStorage.removeItem(this.usuarioKey);
  }
}
