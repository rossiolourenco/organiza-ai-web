#!/usr/bin/env bash
# Gera os artefatos do Release Candidate fora do Git (dist-release/).
# Não faz deploy e não toca em banco. Uso: bash scripts/build-release.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

VERSION="$(tr -d '[:space:]' < VERSION)"
OUT="dist-release/$VERSION"
SHA="sha256sum"
command -v sha256sum >/dev/null 2>&1 || SHA="shasum -a 256"

echo "== Release $VERSION =="
rm -rf "$OUT"
mkdir -p "$OUT"

echo "== Frontend (build de produção) =="
(cd frontend && npm ci --no-audit --no-fund && npm run build)
[ -f frontend/dist/frontend/browser/index.html ] || { echo "build do frontend ausente" >&2; exit 1; }
tar -czf "$OUT/organiza-ai-web-$VERSION.tar.gz" -C frontend/dist/frontend/browser .

echo "== Backend (jar) =="
(cd backend && ./mvnw -B -q -DskipTests clean package)
JAR="$(ls backend/target/backend-*.jar | grep -v '\.original$' | head -1)"
[ "$(ls backend/target/backend-*.jar | grep -vc '\.original$')" -eq 1 ] || { echo "mais de um jar no target; limpe o target" >&2; exit 1; }
[ -n "$JAR" ] || { echo "jar do backend ausente" >&2; exit 1; }
cp "$JAR" "$OUT/organiza-ai-backend-$VERSION.jar"

echo "== Banco (schema + migrations) =="
tar -czf "$OUT/organiza-ai-database-$VERSION.tar.gz" database/schema.sql database/migrations

echo "== Checksums =="
(cd "$OUT" && $SHA organiza-ai-web-"$VERSION".tar.gz organiza-ai-backend-"$VERSION".jar \
  organiza-ai-database-"$VERSION".tar.gz > SHA256SUMS)
cat "$OUT/SHA256SUMS"
echo "OK: artefatos em $OUT (não versionados)"
