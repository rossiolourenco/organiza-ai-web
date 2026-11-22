import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

/** Página pública exigida pelas lojas: explica como excluir a conta e os dados. */
@Component({
  selector: "app-excluir-conta",
  standalone: true,
  imports: [RouterLink],
  templateUrl: "./excluir-conta.component.html",
  styleUrl: "./excluir-conta.component.css",
})
export class ExcluirContaComponent {
  readonly contato = "contato@evox.net.br";
}
