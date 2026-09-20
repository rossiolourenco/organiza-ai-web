import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-cadastro',
  imports: [FormsModule, RouterLink],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.css'
})
export class CadastroComponent {

  nome: string = '';
  email: string = '';
  senha: string = '';

  constructor(
    private usuarioService: UsuarioService,
    private router: Router
  ) {}

  cadastrar() {
    const usuario = {
      nome: this.nome,
      email: this.email,
      senha: this.senha
    };

    this.usuarioService.criar(usuario).subscribe({
      next: () => {
        alert('Conta criada com sucesso!');
        this.router.navigate(['/login']);
      },
      error: (erro) => {
        console.error('Erro ao criar conta:', erro);
        alert('Não foi possível criar a conta.');
      }
    });
  }
}