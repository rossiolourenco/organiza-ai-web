import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterLink, Router } from "@angular/router";
import { UsuarioService } from "../../services/usuario.service";
import { SessionService } from "../../services/session.service";
import { ThemeService } from "../../services/theme.service";

@Component({
  selector: "app-login",
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./login.component.html",
  styleUrl: "./login.component.css",
})
export class LoginComponent implements OnInit {
  email = "";
  senha = "";
  erro: string | null = null;
  aviso: string | null = null;
  carregando = false;
  mostrarSenha = false;

  constructor(
    private usuarioService: UsuarioService,
    private session: SessionService,
    private router: Router,
    public theme: ThemeService,
  ) {}

  ngOnInit(): void {
    if (this.router.url.includes("senha=alterada")) {
      this.aviso = "Senha alterada. Entre novamente com a nova credencial.";
    } else if (this.router.url.includes("admin=1")) {
      this.aviso = "Área administrativa: entre com uma conta ADMIN ou SUPER_ADMIN.";
    } else if (this.router.url.includes("cadastro=1")) {
      this.aviso = "Conta criada com sucesso. Entre com suas credenciais.";
    }
  }

  alternarTema(): void {
    this.theme.alternar();
  }

  entrar() {
    if (this.carregando) return;    this.erro = null;
    if (!this.email.trim() || !this.senha) {
      this.erro = "Informe e-mail e senha.";
      return;
    }
    this.carregando = true;
    this.usuarioService.login(this.email, this.senha).subscribe({
      next: (usuario) => {
        this.session.salvar({ ...usuario, id: usuario.id ?? 0 });
        this.carregando = false;
        const destino = usuario.papel === "ADMIN" || usuario.papel === "SUPER_ADMIN" ? "/admin" : "/home";
        this.router.navigate([destino]);
      },
      error: (erro) => {
        this.carregando = false;
        if (erro.status === 429) {
          this.erro = erro.error?.message || "Muitas tentativas. Aguarde e tente novamente.";
        } else if (erro.status === 403) {
          this.erro = erro.error?.message || "Conta indisponível. Procure um administrador.";
        } else if (erro.status === 401) {
          this.erro = "E-mail ou senha inválidos.";
        } else {
          this.erro = erro.error?.message || "Não foi possível realizar o login.";
        }
      },
    });
  }
}
