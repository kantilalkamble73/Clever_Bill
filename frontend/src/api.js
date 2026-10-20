const BASE = import.meta.env.VITE_API || 'http://localhost:8080/api'
export const session = {
  get: () => JSON.parse(localStorage.getItem('cp-session') || 'null'),
  set: s => localStorage.setItem('cp-session', JSON.stringify(s)),
  clear: () => localStorage.removeItem('cp-session')
}
async function call(path, opt = {}) {
  const s = session.get()
  const r = await fetch(BASE + path, { ...opt, headers: { 'Content-Type': 'application/json', ...(s && { Authorization: 'Bearer ' + s.token }), ...opt.headers } })
  const body = await r.json().catch(() => null)
  if (r.status === 401 && s) { session.clear(); location.reload() }
  if (!r.ok) throw new Error(body?.message || 'Request failed (' + r.status + ')')
  return body
}
const post = (p, b, h) => call(p, { method: 'POST', body: JSON.stringify(b), headers: h })
export const api = {
  login: b => post('/auth/login', b), register: b => post('/auth/register', b),
  list: p => call('/' + p),
  save: (p, row, key) => call('/' + p + (row.id ? '/' + row.id : ''), { method: row.id ? 'PUT' : 'POST', body: JSON.stringify(row), headers: key && !row.id ? { 'Idempotency-Key': key } : undefined }),
  remove: (p, id) => call('/' + p + '/' + id, { method: 'DELETE' }),
  summary: () => call('/reports/summary'), detail: (f, t) => call('/reports/detail?from=' + f + '&to=' + t), ask: q => post('/ai/ask', { question: q }), insights: () => call('/ai/insights'), balance: () => call('/stock-balance'),
  bill: (b, key) => post('/bills', b, { 'Idempotency-Key': key }), ret: id => call('/bills/' + id + '/return', { method: 'POST' }),
  upi: id => call('/integrations/upi-link?billId=' + id), wa: id => call('/integrations/whatsapp-link?billId=' + id)
}
