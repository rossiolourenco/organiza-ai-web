import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { ActivatedRoute, RouterLink } from "@angular/router";

type TipoLegal = "privacidade" | "termos";

/** Página pública de política de privacidade e termos de uso (exigidas pelas lojas). */
@Component({
  selector: "app-legal",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./legal.component.html",
  styleUrl: "./legal.component.css",
})
export class LegalComponent {
  readonly tipo: TipoLegal;
  readonly atualizado = "21 de novembro de 2026";
  readonly contato = "contato@evox.net.br";
  readonly empresa = "Evox Sistemas Digitais LTDA";
  readonly cnpj = "21.543.966/0001-45";
  readonly endereco = "Av. Generalíssimo Deodoro, 1893, Nazaré — Belém/PA, CEP 66040-140";

  constructor(route: ActivatedRoute) {
    this.tipo = (route.snapshot.data["tipo"] as TipoLegal) ?? "privacidade";
  }
}
