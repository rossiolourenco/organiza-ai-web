#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
CANON="/Users/rossiolourenco/Framework-Projetos/dev-standards"

fail() { echo "FAIL: $*" >&2; exit 1; }
ok() { echo "OK: $*"; }

echo "== Organiza Aí Web/Admin — premium gate =="
echo "commit: $(git rev-parse --short HEAD 2>/dev/null || echo 'sem git')"
echo "branch: $(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'sem git')"

# Framework presente e íntegro em relação à fonte canônica.
[ -f dev-standards/AI-GUIDELINES.md ] || fail "dev-standards/AI-GUIDELINES.md ausente"
[ -f STACK.md ] || fail "STACK.md ausente"
[ -f AGENTS.md ] || fail "AGENTS.md ausente"
[ -f opencode.json ] || fail "opencode.json ausente"
node -e "JSON.parse(require('fs').readFileSync('opencode.json','utf8'))" || fail "opencode.json inválido"
if [ -d "$CANON" ]; then
  while IFS= read -r file; do
    cmp -s "$CANON/$file" "dev-standards/$file" || fail "dev-standards divergente: $file"
  done < <(cd "$CANON" && find . -type f -not -name '.DS_Store' | sed 's#^\./##' | sort)
  ok "dev-standards idêntico à fonte canônica"
fi

# Componentes premium e rotas.
[ -f frontend/src/app/pages/perfil/perfil.component.ts ] || fail "PerfilComponent ausente"
[ -f frontend/src/app/pages/home/home.component.ts ] || fail "HomeComponent ausente"
[ -f frontend/src/app/admin/admin-layout.component.ts ] || fail "Admin ausente"
grep -q "PAINEL PESSOAL" frontend/src/app/pages/home/home.component.html || fail "marcador Home premium ausente"
grep -q "Meu perfil" frontend/src/app/pages/perfil/perfil.component.html || fail "marcador Perfil ausente"
grep -q "Security Console" frontend/src/app/admin/admin-layout.component.html || fail "marcador Admin ausente"
grep -q 'path: "perfil"' frontend/src/app/app.routes.ts || fail "rota /perfil ausente"
ok "componentes e rotas premium presentes"

# Não aceitar o layout acadêmico no código ativo.
if grep -Rq "IP do servidor\|Organize suas tarefas de forma simples" \
  frontend/src backend/src --include='*.ts' --include='*.html' --include='*.java' 2>/dev/null; then
  fail "marcador do layout acadêmico encontrado no código ativo"
fi
ok "sem marcadores do layout acadêmico no código ativo"

git diff --check || fail "whitespace/erro de diff"

echo "== Angular build =="
(cd frontend && npm run build)
echo "== Angular testes =="
(cd frontend && npm test -- --watch=false --browsers=ChromeHeadless)
echo "== Backend testes =="
(cd backend && ./mvnw -q test -Dspring.profiles.active=local)

ok "gate Web/Admin concluído"
