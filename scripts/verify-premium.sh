#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
CANON="/Users/rossiolourenco/Framework-Projetos/dev-standards"
MODE="${1:-full}"
MODE="${MODE#--}"

fail() { echo "FAIL: $*" >&2; exit 1; }
ok() { echo "OK: $*"; }

case "$MODE" in
  full|frontend|backend|admin|postgres|release) ;;
  *) echo "uso: $0 [full|frontend|backend|admin|postgres|release]" >&2; exit 2 ;;
esac

echo "== Organiza Aí Web/Admin — premium gate ($MODE) =="
echo "commit: $(git rev-parse --short HEAD 2>/dev/null || echo 'sem git')"
echo "branch: $(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'sem git')"

# Versão única e rastreável do release.
[ -f VERSION ] || fail "VERSION ausente"
RELEASE_VERSION="$(tr -d '[:space:]' < VERSION)"
[ -n "$RELEASE_VERSION" ] || fail "VERSION vazio"
normalizar() { printf '%s' "$1" | tr 'A-Z' 'a-z' | tr -d '.-'; }
FRONTEND_VERSION="$(node -e "console.log(require('./frontend/package.json').version)")"
[ "$(normalizar "$FRONTEND_VERSION")" = "$(normalizar "$RELEASE_VERSION")" ] \
  || fail "versão do frontend ($FRONTEND_VERSION) diferente de VERSION ($RELEASE_VERSION)"
BACKEND_VERSION="$(sed -n '/<artifactId>backend<\/artifactId>/{n;s/.*<version>\(.*\)<\/version>.*/\1/p;}' backend/pom.xml)"
[ "$(normalizar "$BACKEND_VERSION")" = "$(normalizar "$RELEASE_VERSION")" ] \
  || fail "versão do backend ($BACKEND_VERSION) diferente de VERSION ($RELEASE_VERSION)"
ok "versões de release consistentes ($RELEASE_VERSION)"

# Framework: arquivos essenciais, manifesto e fingerprint portáveis.
[ -f dev-standards/AI-GUIDELINES.md ] || fail "dev-standards/AI-GUIDELINES.md ausente"
[ -f dev-standards/MANIFEST.sha256 ] || fail "dev-standards/MANIFEST.sha256 ausente"
[ -f dev-standards/FINGERPRINT ] || fail "dev-standards/FINGERPRINT ausente"
[ -f STACK.md ] || fail "STACK.md ausente"
[ -f AGENTS.md ] || fail "AGENTS.md ausente"
[ -f opencode.json ] || fail "opencode.json ausente"
python3 -m json.tool opencode.json >/dev/null || fail "opencode.json inválido"

EXPECTED="$(tr -d '[:space:]' < dev-standards/FINGERPRINT)"
if command -v sha256sum >/dev/null 2>&1; then
  ACTUAL="$(sha256sum dev-standards/MANIFEST.sha256 | awk '{print $1}')"
  [ "$ACTUAL" = "$EXPECTED" ] || fail "fingerprint do manifesto divergente"
  (cd dev-standards && sha256sum -c MANIFEST.sha256 >/dev/null) || fail "manifesto do dev-standards inválido"
else
  ACTUAL="$(shasum -a 256 dev-standards/MANIFEST.sha256 | awk '{print $1}')"
  [ "$ACTUAL" = "$EXPECTED" ] || fail "fingerprint do manifesto divergente"
  (cd dev-standards && shasum -a 256 -c MANIFEST.sha256 >/dev/null) || fail "manifesto do dev-standards inválido"
fi
ok "dev-standards íntegro pelo manifesto/fingerprint"

# Diff opcional contra a fonte canônica, apenas quando ela existe localmente.
if [ -d "$CANON" ]; then
  diff -rq --exclude=.DS_Store --exclude=SOURCE.md --exclude=PROJECT-SCOPE.md \
    --exclude=MANIFEST.sha256 --exclude=FINGERPRINT "$CANON" dev-standards >/dev/null \
    || fail "dev-standards divergente da fonte canônica"
  ok "dev-standards idêntico à fonte canônica"
fi

# Componentes premium, rotas e contrato do Admin.
[ -f frontend/src/app/pages/perfil/perfil.component.ts ] || fail "PerfilComponent ausente"
[ -f frontend/src/app/pages/home/home.component.ts ] || fail "HomeComponent ausente"
[ -f frontend/src/app/pages/home/home.premium.spec.ts ] || fail "teste estrutural da Home Premium ausente"
[ -f frontend/src/app/admin/admin-layout.component.ts ] || fail "Admin ausente"
[ -f frontend/src/app/services/admin.guard.ts ] || fail "adminGuard ausente"
[ -f frontend/src/app/services/admin.guard.spec.ts ] || fail "teste do adminGuard ausente"
grep -q "PAINEL PESSOAL" frontend/src/app/pages/home/home.component.html || fail "marcador Home premium ausente"
grep -q "Meu perfil" frontend/src/app/pages/perfil/perfil.component.html || fail "marcador Perfil ausente"
grep -q "Security Console" frontend/src/app/admin/admin-layout.component.html || fail "marcador Admin ausente"
grep -q 'path: "perfil"' frontend/src/app/app.routes.ts || fail "rota /perfil ausente"
grep -q 'path: "admin"' frontend/src/app/app.routes.ts || fail "rota /admin ausente"
grep -q "adminGuard" frontend/src/app/app.routes.ts || fail "adminGuard não aplicado nas rotas"
ok "componentes, rotas e Admin presentes"

# Não aceitar o layout acadêmico no código ativo.
if grep -Rq "IP do servidor\|Organize suas tarefas de forma simples" \
  frontend/src backend/src --include='*.ts' --include='*.html' --include='*.java' 2>/dev/null; then
  fail "marcador do layout acadêmico encontrado no código ativo"
fi
ok "sem marcadores do layout acadêmico no código ativo"

git diff --check || fail "whitespace/erro de diff"

if [ "$MODE" = "full" ] || [ "$MODE" = "frontend" ] || [ "$MODE" = "release" ]; then
  echo "== TypeScript =="
  (cd frontend && npx tsc --noEmit -p tsconfig.app.json)
  echo "== Angular build =="
  (cd frontend && npm run build)
  echo "== Angular testes =="
  (cd frontend && npm test -- --watch=false --browsers="${KARMA_BROWSER:-ChromeHeadless}")
  INDEX="frontend/dist/frontend/browser/index.html"
  [ -f "$INDEX" ] || fail "index.html do build ausente"
  for ASSET in $(grep -oE '(main|styles|polyfills)-[A-Z0-9]+\.(js|css)' "$INDEX"); do
    [ -f "frontend/dist/frontend/browser/$ASSET" ] || fail "asset ausente: $ASSET"
  done
  ok "build e assets do Angular presentes"
fi

if [ "$MODE" = "admin" ] || [ "$MODE" = "release" ]; then
  echo "== Teste focado do Admin/RBAC =="
  (cd frontend && npm test -- --watch=false --browsers="${KARMA_BROWSER:-ChromeHeadless}" \
    --include='src/app/services/admin.guard.spec.ts')
  ok "teste do adminGuard executado"
fi

if [ "$MODE" = "full" ] || [ "$MODE" = "backend" ] || [ "$MODE" = "release" ]; then
  echo "== Backend testes (H2) =="
  # O teste PostgreSQL roda no modo --postgres (job web-postgres), com Docker obrigatório.
  (cd backend && ./mvnw -B -q test -Dspring.profiles.active=local -Dtest='!PostgresIntegrationTests')
  echo "== Backend package =="
  (cd backend && ./mvnw -B -q -DskipTests package)
  ok "backend compilado e testado"
fi

if [ "$MODE" = "postgres" ] || [ "$MODE" = "release" ]; then
  echo "== Backend PostgreSQL real (Testcontainers) =="
  (cd backend && ./mvnw -B -Dtest=PostgresIntegrationTests test)
  RELATORIO="backend/target/surefire-reports/TEST-br.com.organizaai.backend.PostgresIntegrationTests.xml"
  [ -f "$RELATORIO" ] || fail "relatório do teste PostgreSQL ausente"
  grep -q 'skipped="0"' "$RELATORIO" || fail "teste PostgreSQL foi ignorado (Docker indisponível?)"
  grep -q 'failures="0"' "$RELATORIO" || fail "teste PostgreSQL com falhas"
  grep -q 'errors="0"' "$RELATORIO" || fail "teste PostgreSQL com erros"
  ok "PostgreSQL 17 validado em container isolado"
fi

ok "gate Web/Admin concluído ($MODE)"
