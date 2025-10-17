const backendBase = (import.meta.env?.VITE_BACKEND_BASE) || 'http://localhost:8080';

function h(tag, props = {}, ...children) {
  const el = Object.assign(document.createElement(tag), props);
  for (const c of children.flat()) {
    if (typeof c === 'string') el.appendChild(document.createTextNode(c));
    else if (c) el.appendChild(c);
  }
  return el;
}

function navigate(path) {
  history.pushState({}, '', path);
  render();
}

function parseHashParams() {
  const raw = location.hash.startsWith('#') ? location.hash.substring(1) : '';
  const params = new URLSearchParams(raw);
  const obj = {};
  for (const [k, v] of params.entries()) obj[k] = v;
  return obj;
}

function saveTokens({ accessToken, refreshToken, tokenType, expiresIn }) {
  const payload = { accessToken, refreshToken, tokenType, expiresIn, savedAt: Date.now() };
  localStorage.setItem('authTokens', JSON.stringify(payload));
  return payload;
}

function getTokens() {
  try { return JSON.parse(localStorage.getItem('authTokens') || 'null'); }
  catch { return null; }
}

async function callProtected() {
  const tokens = getTokens();
  if (!tokens?.accessToken) throw new Error('No access token');
  const res = await fetch(`${backendBase}/api/test/user`, {
    headers: { 'Authorization': `Bearer ${tokens.accessToken}` }
  });
  const data = await res.json();
  return { status: res.status, data };
}

function Home() {
  const btn = h('button', { onclick: () => { location.href = `${backendBase}/api/auth/login/google`; } }, 'Login con Google');
  const tokens = getTokens();
  const info = h('pre', {}, JSON.stringify(tokens, null, 2));
  const testBtn = h('button', { onclick: async () => {
    try {
      const { status, data } = await callProtected();
      out.textContent = JSON.stringify({ status, data }, null, 2);
    } catch (e) { out.textContent = e.message; }
  } }, 'Probar /api/test/user');
  const out = h('pre');

  return h('div', { className: 'card' },
    h('h2', {}, 'Inicio'),
    h('div', { className: 'row' }, btn, testBtn),
    h('h3', {}, 'Tokens guardados'),
    info,
    h('h3', {}, 'Respuesta protegida'),
    out,
    h('p', { className: 'muted' }, `Backend: ${backendBase}`)
  );
}

function Callback() {
  const p = parseHashParams();
  const tokens = saveTokens({
    accessToken: p.accessToken,
    refreshToken: p.refreshToken,
    tokenType: p.tokenType,
    expiresIn: Number(p.expiresIn)
  });
  const info = h('pre', {}, JSON.stringify(tokens, null, 2));
  const backBtn = h('button', { onclick: () => navigate('/') }, 'Volver a inicio');
  return h('div', { className: 'card' },
    h('h2', {}, 'Callback OAuth2'),
    h('p', {}, 'Tokens recibidos y guardados en LocalStorage.'),
    info,
    backBtn
  );
}

function Router() {
  const path = location.pathname;
  if (path === '/oauth2/callback') return Callback();
  return Home();
}

function render() {
  const root = document.getElementById('app');
  root.innerHTML = '';
  root.appendChild(Router());
}

window.addEventListener('popstate', render);
render();
