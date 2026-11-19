import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-cadastro',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.css'
})
export class CadastroComponent {
  nome = '';
  email = '';
  senha = '';
  carregando = false;
  erro: string | null = null;
  mostrarSenha = false;

  constructor(
    private usuarioService: UsuarioService,
    private router: Router,
    public theme: ThemeService,
  ) {}

  alternarTema(): void { this.theme.alternar(); }

  cadastrar() {
    if (this.carregando) return;
    this.erro = null;
    if (!this.nome.trim() || !this.email.includes('@') || this.senha.length < 8) {
      this.erro = 'Informe nome, e-mail válido e senha com pelo menos 8 caracteres.';
      return;
    }
    this.carregando = true;
    this.usuarioService.criar({ nome: this.nome.trim(), email: this.email.trim(), senha: this.senha }).subscribe({
      next: () => {
        this.carregando = false;
        this.router.navigate(['/login'], { queryParams: { cadastro: '1' } });
      },
      error: (erro) => {
        this.carregando = false;
        this.erro = erro.error?.message || 'Não foi possível criar a conta.';
      }
    });
  }
}
