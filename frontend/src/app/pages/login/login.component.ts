import { Component } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterLink, Router } from "@angular/router";
import { UsuarioService } from "../../services/usuario.service";

@Component({
  selector: "app-login",
  imports: [FormsModule, RouterLink],
  templateUrl: "./login.component.html",
  styleUrl: "./login.component.css",
})
export class LoginComponent {
  email: string = "";
  senha: string = "";

  constructor(
    private usuarioService: UsuarioService,
    private router: Router,
  ) {}

  entrar() {
    this.usuarioService.login(this.email, this.senha).subscribe({
      next: (usuario) => {
        console.log("Login realizado:", usuario);

        localStorage.setItem("usuario", JSON.stringify(usuario));

        alert("Login realizado com sucesso!");
        this.router.navigate(["/home"]);
      },
      error: (erro) => {
        console.error("Erro no login:", erro);

        if (erro.status === 401) {
          alert("E-mail ou senha inválidos.");
        } else {
          alert("Não foi possível realizar o login.");
        }
      },
    });
  }
}
