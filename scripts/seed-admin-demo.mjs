// Cenário administrativo DEMO. Requer o SUPER_ADMIN provisionado pelo bootstrap.
// Uso:
//   ADMIN_EMAIL=... ADMIN_PASSWORD=... DEMO_USER_PASSWORD=... node scripts/seed-admin-demo.mjs
//   ... --reset (anonimiza apenas as contas @exposicao.invalid e recria o cenário)
const server = process.env.DEMO_API_URL || 'http://localhost:8080';
const adminEmail = process.env.ADMIN_EMAIL;
const adminPassword = process.env.ADMIN_PASSWORD;
const userPassword = process.env.DEMO_USER_PASSWORD;
const reset = process.argv.includes('--reset');

if (!['localhost', '127.0.0.1'].includes(new URL(server).hostname))
  throw new Error('Este roteiro usa somente backend local isolado.');
if (!adminEmail || !adminPassword)
  throw new Error('Defina ADMIN_EMAIL e ADMIN_PASSWORD do SUPER_ADMIN provisionado.');
if (!userPassword || userPassword.length < 8)
  throw new Error('Defina DEMO_USER_PASSWORD com pelo menos 8 caracteres.');

let token;
async function call(method, path, body) {
  const resposta = await fetch(`${server}/api${path}`, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
  });
  if (!resposta.ok) {
    let detalhe = '';
    try { detalhe = (await resposta.json()).message || ''; } catch { /* corpo vazio */ }
    throw new Error(`${method} ${path} → HTTP ${resposta.status}${detalhe ? ': ' + detalhe : ''}`);
  }
  return resposta.status === 204 ? null : resposta.json();
}

const login = (email, senha) => call('POST', '/usuarios/login', { email, senha });

const contas = [
  { nome: 'Ana Souza', email: 'ana.souza@exposicao.invalid', papel: 'USER', status: 'ATIVO' },
  { nome: 'Bruno Lima', email: 'bruno.lima@exposicao.invalid', papel: 'USER', status: 'BLOQUEADO' },
  { nome: 'Carla Mendes', email: 'carla.mendes@exposicao.invalid', papel: 'ADMIN', status: 'ATIVO' },
  { nome: 'Diego Rocha', email: 'diego.rocha@exposicao.invalid', papel: 'USER', status: 'DESATIVADO' },
];

// Autentica como SUPER_ADMIN (bootstrap).
const admin = await login(adminEmail, adminPassword);
token = admin.token;
console.log(`SUPER_ADMIN autenticado: ${adminEmail}`);

if (reset) {
  const existentes = await call('GET', '/admin/usuarios?q=exposicao.invalid&size=100');
  let removidos = 0;
  for (const usuario of existentes.content || []) {
    if (contas.some(c => c.email === usuario.email) && usuario.status !== 'ANONIMIZADO') {
      await call('DELETE', `/admin/usuarios/${usuario.id}`);
      removidos++;
    }
  }
  console.log(`Reset: ${removidos} conta(s) DEMO anonimizada(s).`);
  await call('POST', '/admin/firewall/reset-demo');
}

const existentes = await call('GET', '/admin/usuarios?q=exposicao.invalid&size=100');
for (const conta of contas) {
  if ((existentes.content || []).some(u => u.email === conta.email)) {
    console.log(`Conta já existe: ${conta.email}`);
    continue;
  }
  // Cadastro público cria USER ativo; o papel/status é ajustado pelo SUPER_ADMIN.
  await call('POST', '/usuarios', { nome: conta.nome, email: conta.email, senha: userPassword });
  const lista = await call('GET', `/admin/usuarios?q=${encodeURIComponent(conta.email)}&size=10`);
  const usuario = (lista.content || []).find(u => u.email === conta.email);
  if (!usuario) throw new Error(`Conta criada, mas não localizada: ${conta.email}`);
  if (conta.papel !== 'USER') {
    await call('PATCH', `/admin/usuarios/${usuario.id}/papel`, { papel: conta.papel });
  }
  if (conta.status === 'BLOQUEADO') {
    await call('PATCH', `/admin/usuarios/${usuario.id}/status`, {
      status: 'BLOQUEADO', motivo: 'Revisão de segurança da exposição',
    });
  } else if (conta.status === 'DESATIVADO') {
    await call('PATCH', `/admin/usuarios/${usuario.id}/status`, {
      status: 'DESATIVADO', motivo: 'Conta de demonstração desativada',
    });
  }
  console.log(`Conta criada: ${conta.email} (${conta.papel}/${conta.status})`);
}

// Tentativas reais para alimentar a central de segurança.
for (let i = 0; i < 3; i++) {
  await fetch(`${server}/api/usuarios/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'ana.souza@exposicao.invalid', senha: 'senha-incorreta' }),
  });
}
for (let i = 0; i < 2; i++) {
  await fetch(`${server}/api/usuarios/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'intruso@exposicao.invalid', senha: 'tentativa-invalida' }),
  });
}
await login('ana.souza@exposicao.invalid', userPassword);
await login('carla.mendes@exposicao.invalid', userPassword);
console.log('Tentativas de login e sessões ativas criadas.');

// Firewall DEMO: regra adicional + aplicação simulada.
await call('POST', '/admin/firewall/reset-demo');
const regras = await call('GET', '/admin/firewall/regras');
if (!regras.some(r => r.nome === 'DEMO Bloquear SMTP')) {
  await call('POST', '/admin/firewall/regras', {
    nome: 'DEMO Bloquear SMTP', descricao: 'Serviço de e-mail legado bloqueado na simulação.',
    ipCidr: '0.0.0.0/0', porta: 25, protocolo: 'TCP', direcao: 'INBOUND', acao: 'DENY',
    prioridade: 60, ativa: true,
  });
}
await call('POST', '/admin/firewall/aplicar', { motivo: 'Cenário de exposição' });
console.log('Firewall DEMO aplicado (simulação persistida, sem alteração real).');

const resumo = await call('GET', '/admin/seguranca/overview');
console.log(`\nCenário ADMIN pronto.
Contas ativas: ${resumo.contasAtivas} · bloqueadas: ${resumo.contasBloqueadas}
Falhas 24h: ${resumo.falhas24h} · sessões ativas: ${resumo.sessoesAtivas}
Senha das contas DEMO: valor de DEMO_USER_PASSWORD (não impressa).
SUPER_ADMIN: ${adminEmail} (senha definida no bootstrap, não impressa).`);
