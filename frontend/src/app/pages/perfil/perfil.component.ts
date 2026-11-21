import { CommonModule } from "@angular/common";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { Subscription } from "rxjs";
import { SessionService } from "../../services/session.service";
import { ThemeService } from "../../services/theme.service";
import { Usuario, UsuarioService } from "../../services/usuario.service";

@Component({
  selector: "app-perfil",
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./perfil.component.html",
  styleUrl: "./perfil.component.css",
})
export class PerfilComponent implements OnInit, OnDestroy {
  usuario: Usuario | null = null;
  nome = "";
  email = "";
  papel = "USER";
  status = "ATIVO";

  carregando = true;
  erro: string | null = null;

  salvandoPerfil = false;
  erroPerfil: string | null = null;
  avisoPerfil: string | null = null;

  senhaAtual = "";
  novaSenha = "";
  confirmarSenha = "";
  mostrarSenhas = false;
  alterandoSenha = false;
  erroSenha: string | null = null;

  private sub = new Subscription();

  constructor(
    private usuarioService: UsuarioService,
    public session: SessionService,
    private router: Router,
    public theme: ThemeService,
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  get iniciais(): string {
    return (this.nome || this.email || "?").trim().slice(0, 1).toUpperCase();
  }

  get papelLabel(): string {
    return this.papel === "SUPER_ADMIN"
      ? "Super administrador"
      : this.papel === "ADMIN"
        ? "Administrador"
        : "Usuário";
  }

  get membroDesde(): string {
    if (!this.usuario?.dataCriacao) return "—";
    const data = new Date(this.usuario.dataCriacao);
    return Number.isNaN(data.getTime())
      ? "—"
      : new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "long", year: "numeric" }).format(data);
  }

  get deveTrocarSenha(): boolean {
    return this.session.usuario()?.deveTrocarSenha === true;
  }

  carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.sub.add(this.usuarioService.perfil().subscribe({
      next: usuario => {
        this.aplicar(usuario);
        this.carregando = false;
      },
      error: erro => {
        const local = this.session.usuario();
        if (local) this.aplicar(local);
        this.erro = erro.error?.message || "Não foi possível carregar seu perfil. Tente novamente.";
        this.carregando = false;
      },
    }));
  }

  salvarPerfil(): void {
    this.erroPerfil = null;
    this.avisoPerfil = null;
    const nome = this.nome.trim();
    if (nome.length < 2 || nome.length > 100) {
      this.erroPerfil = "O nome deve ter entre 2 e 100 caracteres.";
      return;
    }
    if (this.salvandoPerfil) return;

    this.salvandoPerfil = true;
    this.sub.add(this.usuarioService.atualizarPerfil(nome).subscribe({
      next: usuario => {
        this.aplicar(usuario);
        this.salvandoPerfil = false;
        this.avisoPerfil = "Nome atualizado com sucesso.";
      },
      error: erro => {
        this.salvandoPerfil = false;
        this.erroPerfil = erro.error?.message || "Não foi possível salvar seu nome.";
      },
    }));
  }

  alterarSenha(): void {
    this.erroSenha = null;
    if (!this.senhaAtual) {
      this.erroSenha = "Informe sua senha atual.";
      return;
    }
    if (this.novaSenha.length < 8) {
      this.erroSenha = "A nova senha deve ter pelo menos 8 caracteres.";
      return;
    }
    if (this.novaSenha.length > 72) {
      this.erroSenha = "A nova senha deve ter até 72 caracteres.";
      return;
    }
    if (this.novaSenha !== this.confirmarSenha) {
      this.erroSenha = "A confirmação não confere com a nova senha.";
      return;
    }
    if (this.novaSenha === this.senhaAtual) {
      this.erroSenha = "A nova senha deve ser diferente da senha atual.";
      return;
    }
    if (this.alterandoSenha) return;

    this.alterandoSenha = true;
    this.sub.add(this.usuarioService.alterarSenha(this.senhaAtual, this.novaSenha).subscribe({
      next: () => {
        this.alterandoSenha = false;
        this.senhaAtual = "";
        this.novaSenha = "";
        this.confirmarSenha = "";
        this.session.limpar();
        this.router.navigate(["/login"], { queryParams: { senha: "alterada" } });
      },
      error: erro => {
        this.alterandoSenha = false;
        this.erroSenha = erro.error?.message || "Não foi possível alterar a senha.";
      },
    }));
  }

  modalExclusao = false;
  senhaExclusao = "";
  confirmacaoExclusao = "";
  excluindo = false;
  erroExclusao: string | null = null;

  abrirExclusao(): void {
    this.modalExclusao = true;
    this.senhaExclusao = "";
    this.confirmacaoExclusao = "";
    this.erroExclusao = null;
  }

  fecharExclusao(): void {
    if (!this.excluindo) this.modalExclusao = false;
  }

  confirmarExclusao(): void {
    if (this.excluindo) return;
    if (!this.senhaExclusao) {
      this.erroExclusao = "Informe sua senha.";
      return;
    }
    if (this.confirmacaoExclusao.trim().toUpperCase() !== "EXCLUIR") {
      this.erroExclusao = "Digite EXCLUIR para confirmar.";
      return;
    }
    this.excluindo = true;
    this.erroExclusao = null;
    this.sub.add(this.usuarioService.excluirConta(this.senhaExclusao).subscribe({
      next: () => {
        this.excluindo = false;
        this.modalExclusao = false;
        this.session.limpar();
        void this.router.navigate(["/login"], { queryParams: { conta: "excluida" } });
      },
      error: erro => {
        this.excluindo = false;
        this.erroExclusao = erro.error?.message || "Não foi possível excluir a conta.";
      },
    }));
  }

  sair(): void {
    this.session.limpar();
    void this.router.navigate(["/login"]);
  }

  private aplicar(usuario: Usuario): void {
    this.usuario = usuario;
    this.nome = usuario.nome;
    this.email = usuario.email;
    this.papel = usuario.papel ?? "USER";
    this.status = usuario.status ?? "ATIVO";
    this.session.salvar({
      id: usuario.id!,
      nome: usuario.nome,
      email: usuario.email,
      papel: usuario.papel,
      status: usuario.status,
      deveTrocarSenha: usuario.deveTrocarSenha,
    });
  }
}
