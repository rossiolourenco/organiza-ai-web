// Cria ou restaura uma conta DEMO identificada, somente em backend local isolado.
// Uso:
//   DEMO_PASSWORD=... node scripts/seed-demo.mjs
//   DEMO_PASSWORD=... DEMO_EMAIL=demo@exposicao.invalid node scripts/seed-demo.mjs --reset
// --reset remove apenas tarefas, categorias e hábitos da conta informada e recria o cenário.
const server = process.env.DEMO_API_URL || 'http://localhost:8080';
const password = process.env.DEMO_PASSWORD;
const reset = process.argv.includes('--reset');
if (!['localhost', '127.0.0.1'].includes(new URL(server).hostname))
  throw new Error('Este roteiro usa somente backend local isolado.');
if (!password || password.length < 8) throw new Error('Defina DEMO_PASSWORD com pelo menos 8 caracteres.');
const email = process.env.DEMO_EMAIL || `demo-${Date.now()}@exposicao.invalid`;
if (!email.endsWith('@exposicao.invalid'))
  throw new Error('Use um e-mail terminado em @exposicao.invalid para isolar a demonstração.');
let token;
let userId;

async function call(method, path, data) {
  const response = await fetch(`${server}/api${path}`, { method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    ...(data === undefined ? {} : { body: JSON.stringify(data) }) });
  if (!response.ok) throw new Error(`Falha no seed DEMO: ${method} ${path} → HTTP ${response.status}.`);
  return response.status === 204 ? null : response.json();
}
async function register() {
  await call('POST', '/usuarios', { nome: 'DEMO Exposição', email, senha: password });
}
async function login() {
  const user = await call('POST', '/usuarios/login', { email, senha: password });
  token = user.token; userId = user.id;
  return user;
}
function date(offset) {
  const d = new Date(); d.setDate(d.getDate() + offset);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
async function purge() {
  const tasks = await call('GET', `/tarefas?usuarioId=${userId}`);
  let removed = 0;
  for (const task of tasks) {
    await call('DELETE', `/tarefas/${task.id}?usuarioId=${userId}`);
    await call('DELETE', `/tarefas/${task.id}/definitivo?usuarioId=${userId}`);
    removed++;
  }
  const habits = await call('GET', `/habitos?usuarioId=${userId}`);
  for (const habit of habits) await call('DELETE', `/habitos/${habit.id}?usuarioId=${userId}`);
  const categories = await call('GET', `/categorias?usuarioId=${userId}`);
  for (const category of categories) await call('DELETE', `/categorias/${category.id}?usuarioId=${userId}`);
  console.log(`Reset: ${removed} tarefa(s), ${habits.length} hábito(s) e ${categories.length} categoria(s) removidos.`);
}

try { await register(); } catch { /* Conta já existe: segue para o login. */ }
await login();
if (reset) await purge();

const categoryNames = [['DEMO Estudos', '#635BFF'], ['DEMO Trabalho', '#0891B2'], ['DEMO Pessoal', '#16A34A']];
const categories = [];
const existingCategories = await call('GET', `/categorias?usuarioId=${userId}`);
for (const [nome, cor] of categoryNames) {
  const found = existingCategories.find(category => category.nome === nome);
  categories.push(found || await call('POST', `/categorias?usuarioId=${userId}`, { nome, cor }));
}

const details = (extra = {}) => ({ horario: '09:00', notas: 'DEMO: dado fictício criado para a exposição.',
  etiquetas: ['demo'], favorita: false, fixada: false, ordem: 0, recorrencia: 'NENHUMA',
  fusoHorario: 'America/Sao_Paulo', importante: null, urgente: null, duracaoEstimada: 30,
  subtarefas: [{ id: 'revisar', titulo: 'DEMO Revisar conteúdo', concluida: true },
    { id: 'ensaiar', titulo: 'DEMO Ensaiar explicação', concluida: false }], ...extra });

const tasks = [
  { titulo: 'DEMO Preparar apresentação', prioridade: 'ALTA', status: 'EM_ANDAMENTO', dataPrazo: date(0),
    categoria: categories[0], detalhes: details({ importante: true, urgente: true, duracaoEstimada: 45, fixada: true, favorita: true, ordem: 0 }) },
  { titulo: 'DEMO Revisar arquitetura', prioridade: 'ALTA', status: 'PENDENTE', dataPrazo: date(-2),
    categoria: categories[1], detalhes: details({ importante: true, urgente: true, duracaoEstimada: 60, ordem: 1 }) },
  { titulo: 'DEMO Enviar relatório', prioridade: 'MEDIA', status: 'PENDENTE', dataPrazo: date(1),
    categoria: categories[1], detalhes: details({ importante: true, urgente: false, duracaoEstimada: 30, ordem: 2 }) },
  { titulo: 'DEMO Organizar a semana', prioridade: 'MEDIA', status: 'CONCLUIDA', dataPrazo: date(-1),
    categoria: categories[2], detalhes: details({ importante: false, urgente: true, duracaoEstimada: 20, ordem: 3 }) },
  { titulo: 'DEMO Ler capítulo do livro', prioridade: 'BAIXA', status: 'PENDENTE', dataPrazo: date(3),
    categoria: categories[2], detalhes: details({ importante: true, urgente: false, duracaoEstimada: 25, ordem: 4 }) },
  { titulo: 'DEMO Planejar revisão semanal', prioridade: 'MEDIA', status: 'PENDENTE', dataPrazo: date(5),
    categoria: categories[0], detalhes: details({ importante: false, urgente: false, duracaoEstimada: 15, ordem: 5 }) },
  { titulo: 'DEMO Estudar para a prova', prioridade: 'ALTA', status: 'PENDENTE', dataPrazo: date(2),
    categoria: categories[0], detalhes: details({ importante: true, urgente: false, duracaoEstimada: 90, ordem: 6,
      recorrencia: 'SEMANAL' }) },
  { titulo: 'DEMO Responder mensagens', prioridade: 'BAIXA', status: 'PENDENTE', dataPrazo: null,
    categoria: null, detalhes: details({ importante: false, urgente: false, duracaoEstimada: 10, ordem: 7,
      subtarefas: [] }) },
];
const existingTasks = await call('GET', `/tarefas?usuarioId=${userId}`);
for (const task of tasks) {
  if (existingTasks.some(existing => existing.titulo === task.titulo)) continue;
  await call('POST', `/tarefas?usuarioId=${userId}`, {
    titulo: task.titulo, descricao: 'Dado de demonstração criado exclusivamente para a exposição.',
    prioridade: task.prioridade, status: task.status, dataPrazo: task.dataPrazo,
    categoria: task.categoria ? { id: task.categoria.id } : null, detalhes: task.detalhes,
  });
}

const habits = [
  { nome: 'DEMO Ler 10 páginas', descricao: 'Constância antes de intensidade.', frequencia: 'DIARIA',
    metaSemanal: 7, cor: '#635BFF', dias: [0, 1, 2, 3, 4] },
  { nome: 'DEMO Caminhar 20 minutos', descricao: 'Movimento diário ao ar livre.', frequencia: 'SEMANAL',
    metaSemanal: 3, cor: '#16A34A', dias: [1, 3, 5] },
  { nome: 'DEMO Revisar a semana', descricao: 'Planejar os próximos sete dias.', frequencia: 'SEMANAL',
    metaSemanal: 1, cor: '#D97706', dias: [5] },
];
const existingHabits = await call('GET', `/habitos?usuarioId=${userId}`);
for (const habit of habits) {
  let saved = existingHabits.find(existing => existing.nome === habit.nome);
  if (!saved) saved = await call('POST', `/habitos?usuarioId=${userId}`, { nome: habit.nome,
    descricao: habit.descricao, frequencia: habit.frequencia, metaSemanal: habit.metaSemanal,
    cor: habit.cor, ativo: true });
  for (const offset of habit.dias) {
    await call('POST', `/habitos/${saved.id}/registros?usuarioId=${userId}`, { data: date(-offset) });
  }
}

console.log(`Conta DEMO ${reset ? 'restaurada' : 'garantida'}: ${email}
Usuário: ${userId}
Servidor: ${server}
Senha: valor de DEMO_PASSWORD (nunca impressa).
Tarefas: ${tasks.length} · Categorias: ${categories.length} · Hábitos: ${habits.length}
Foco: histórico local, não semeado pelo servidor.`);
