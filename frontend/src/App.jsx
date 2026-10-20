import { useEffect, useState } from 'react'
import mods from './modules.json'
import { api, session } from './api'
import { Reports, Assistant } from './Reports.jsx'
import { loadPrefs, applyPrefs, effective, Appearance } from './theme.jsx'
import { Icon, Num, inr, label, Toasts, useToast, Badge, Drawer, Modal, Empty, DataTable } from './ui.jsx'

const NAV = [
  { g: 'Sales', c: '#34d399', items: [['dashboard', 'Overview', 'home', '#34d399'], ['bills', 'Billing & Returns', 'receipt', '#60a5fa'], ['khata', 'Customer Khata', 'book', '#fbbf24'], ['payments', 'Payments', 'wallet', '#a78bfa']] },
  { g: 'Insights', c: '#f472b6', items: [['reports', 'Reports', 'trend', '#f472b6'], ['ai', 'AI Assistant', 'spark', '#22d3ee']] },
  { g: 'Inventory', c: '#fb923c', items: [['products', 'Catalog & Pricing', 'box', '#fb923c'], ['stock', 'Stock Ledger', 'layers', '#2dd4bf'], ['purchases', 'Purchasing', 'cart', '#f87171']] },
  { g: 'Settings', c: '#818cf8', items: [['shop-profile', 'Shop & Tax Profile', 'store', '#818cf8'], ['users', 'Staff Users', 'users', '#a3e635']] }
]
const CASHIER = ['bills', 'products', 'stock', 'khata', 'payments']
const MONEY = ['price', 'costPrice', 'amount', 'total', 'paid'], BADGES = ['type', 'status', 'mode', 'role']
const cell = (f, r) => { const v = r[f.name]; if (v == null || v === '') return '-'; if (MONEY.includes(f.name)) return inr(v); if (f.name === 'gstRate') return v + '%'; if (BADGES.includes(f.name)) return <Badge v={v} />; return v }
const day = d => new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
const Head = ({ title, sub, action }) => <div className="page-head"><div><h1>{title}</h1>{sub && <p className="mute">{sub}</p>}</div>{action}</div>

function Login({ onDone }) {
  const [reg, setReg] = useState(false), [f, setF] = useState({}), [err, setErr] = useState(''), [busy, setBusy] = useState(false)
  const set = k => e => setF({ ...f, [k]: e.target.value })
  const go = async e => { e.preventDefault(); setBusy(true); try { const s = await (reg ? api.register(f) : api.login(f)); session.set(s); onDone(s) } catch (x) { setErr(x.message) } setBusy(false) }
  return (<div className="auth">
    <section className="auth-art"><div className="logo big">C</div><h1>Run your shop<br />like a pro.</h1><p>GST billing, stock, khata and UPI collections in one secure workspace.</p>
      <ul><li><Icon n="receipt" />GST-ready invoices in seconds</li><li><Icon n="qr" />UPI QR and WhatsApp sharing</li><li><Icon n="users" />Roles for owner, admin and cashier</li></ul></section>
    <section className="auth-form"><form onSubmit={go}><h2>{reg ? 'Create your shop' : 'Welcome back'}</h2><p className="mute">{reg ? 'Set up a secure workspace in a minute.' : 'Sign in to continue.'}</p>
      {err && <p className="notice">{err}</p>}
      {reg && <label>Shop name<input value={f.shopName || ''} onChange={set('shopName')} required /></label>}
      {reg && <label>Your name<input value={f.name || ''} onChange={set('name')} /></label>}
      <label>Username<input autoComplete="username" value={f.username || ''} onChange={set('username')} required /></label>
      <label>Password<input type="password" autoComplete="current-password" minLength={6} value={f.password || ''} onChange={set('password')} required /></label>
      <button className="btn lg" disabled={busy}>{busy ? 'Please wait...' : reg ? 'Create shop account' : 'Sign in'}</button>
      <button type="button" className="link" onClick={() => { setReg(!reg); setErr('') }}>{reg ? 'I already have an account' : 'Register a new shop'}</button></form></section></div>)
}

function Dashboard({ name, go }) {
  const [s, setS] = useState(null), [ai, setAi] = useState([]), [err, setErr] = useState('')
  useEffect(() => { api.summary().then(setS).catch(e => setErr(e.message)); api.insights().then(r => setAi(r.insights)).catch(() => {}) }, [])
  if (err) return <p className="notice">{err}</p>
  if (!s) return <div className="grid4">{[0, 1, 2, 3].map(i => <div key={i} className="card skel" />)}</div>
  const hr = new Date().getHours(), hi = hr < 12 ? 'Good morning' : hr < 17 ? 'Good afternoon' : 'Good evening'
  const days = Object.entries(s.daily), vals = days.map(d => Number(d[1])), max = Math.max(1, ...vals)
  const pts = vals.map((v, i) => [i * 100, 130 - (v / max) * 100]), line = pts.map((p, i) => (i ? 'L' : 'M') + p[0] + ' ' + p[1]).join(' ')
  const tot = s.topProducts.reduce((a, t) => a + Number(t.amount), 0), C = 2 * Math.PI * 52, COL = ['var(--accent)', 'var(--a2)', '#f59e0b', '#38bdf8', '#f472b6']
  let off = 0
  const td = vals[vals.length - 1] || 0, yd = vals[vals.length - 2] || 0
  const delta = yd > 0 ? <small className={'delta ' + (td >= yd ? 'up' : 'down')}>{td >= yd ? '\u25B2 ' : '\u25BC '}{Math.abs((td - yd) / yd * 100).toFixed(0)}% vs yesterday</small> : <small>Today {inr(td)}</small>
  const kpis = [['Sales', s.sales, 1, 'trend', 'accent'], ['Purchases', s.purchases, 1, 'cart', 'a2'], ['Khata due', s.khataDue, 1, 'book', 'warn'], ['Bills', s.bills, 0, 'receipt', 'a2']]
  return (<><Head title={hi + ', ' + name} sub="Here is how your shop is doing." action={<button className="btn" onClick={() => go('bills')}><Icon n="plus" size={16} />New bill</button>} />
    <div className="grid4">{kpis.map(([k, v, money, ic, c]) => <div key={k} className={'card kpi ' + c}><div className="chip"><Icon n={ic} /></div><span>{k}</span><b><Num v={v} money={money} /></b>{k === 'Sales' && delta}</div>)}</div>
    <div className="split">
      <div className="card pad"><div className="card-h"><h2>Sales, last 7 days</h2><span className="mute">{inr(vals.reduce((a, b) => a + b, 0))}</span></div>
        <svg viewBox="0 0 600 160" className="area" preserveAspectRatio="none"><defs><linearGradient id="g" x1="0" x2="0" y1="0" y2="1"><stop offset="0" stopColor="var(--accent)" stopOpacity=".35" /><stop offset="1" stopColor="var(--accent)" stopOpacity="0" /></linearGradient></defs>
          <path d={line + ' L600 160 L0 160Z'} fill="url(#g)" /><path d={line} fill="none" stroke="var(--accent)" strokeWidth="2.5" vectorEffect="non-scaling-stroke" /></svg>
        <div className="xlab">{days.map(d => <small key={d[0]}>{day(d[0])}</small>)}</div></div>
      <div className="card pad"><div className="card-h"><h2>Top products</h2></div>
        {s.topProducts.length === 0 ? <p className="mute">No sales yet.</p> : <div className="donut-wrap"><svg viewBox="0 0 140 140" className="donut"><circle cx="70" cy="70" r="52" className="ring" />
          {s.topProducts.map((t, i) => { const len = tot ? Number(t.amount) / tot * C : 0; const el = <circle key={t.name} cx="70" cy="70" r="52" stroke={COL[i]} strokeDasharray={len + ' ' + (C - len)} strokeDashoffset={-off} className="seg" />; off += len; return el })}</svg>
          <ul className="legend">{s.topProducts.map((t, i) => <li key={t.name}><i style={{ background: COL[i] }} />{t.name}<b>{inr(t.amount)}</b></li>)}</ul></div>}</div></div>
    <div className="card pad ai"><div className="card-h"><h2><Icon n="spark" size={16} /> Smart insights</h2><button className="link" onClick={() => go('ai')}>Ask AI</button></div>{ai.map(x => <p key={x} className="insight">{x}</p>)}</div></>)
}

function Invoice({ b, shop, onClose, act }) {
  return (<Modal onClose={onClose} wide><div className="invoice">
    <div className="inv-head"><div><h3>{shop.name || 'Your shop'}</h3><p className="mute">{shop.address}</p>{shop.gstin && <p className="mute">GSTIN {shop.gstin}</p>}</div>
      <div className="inv-no"><b>{b.billNo}</b><span className="mute">{new Date(b.createdAt || Date.now()).toLocaleString('en-IN')}</span><Badge v={b.status} /></div></div>
    <p className="mute">Billed to <b>{b.customerName || 'Walk-in customer'}</b> {b.customerPhone}</p>
    <table><thead><tr><th>Item</th><th className="num">Qty</th><th className="num">Price</th><th className="num">GST</th><th className="num">Amount</th></tr></thead>
      <tbody>{b.items.map((l, i) => <tr key={i}><td>{l.name}</td><td className="num">{l.qty}</td><td className="num">{inr(l.price)}</td><td className="num">{l.gstRate || 0}%</td><td className="num">{inr(l.lineTotal)}</td></tr>)}</tbody></table>
    <div className="inv-tot"><div><span>Subtotal</span><b>{inr(b.subtotal)}</b></div><div><span>GST</span><b>{inr(b.gstTotal)}</b></div><div className="grand"><span>Total</span><b>{inr(b.total)}</b></div>
      <div><span>Paid ({b.mode})</span><b>{inr(b.paid)}</b></div><div><span>Balance</span><b>{inr(b.total - b.paid)}</b></div></div></div>
    <div className="modal-actions no-print"><button className="btn ghost" onClick={() => window.print()}><Icon n="print" size={16} />Print</button>
      <button className="btn ghost" onClick={() => act('upi', b.id)}><Icon n="qr" size={16} />UPI QR</button><button className="btn" onClick={() => act('wa', b.id)}><Icon n="chat" size={16} />WhatsApp</button></div></Modal>)
}

function Pos({ canReturn }) {
  const toast = useToast(), blank = { customerName: '', customerPhone: '', paid: '', mode: 'CASH' }
  const [products, setP] = useState([]), [bal, setBal] = useState({}), [bills, setBills] = useState([]), [shop, setShop] = useState({}), [cart, setCart] = useState([])
  const [c, setC] = useState(blank), [key, setKey] = useState(crypto.randomUUID()), [q, setQ] = useState(''), [busy, setBusy] = useState(false), [inv, setInv] = useState(null), [qr, setQr] = useState(null)
  const load = () => Promise.all([api.list('products'), api.balance(), api.list('bills'), api.list('shop-profile')]).then(([p, b, bl, sh]) => { setP(p); setBal(b); setBills(bl); setShop(sh[0] || {}) }).catch(e => toast(e.message, 'err'))
  useEffect(() => { load() }, [])
  const add = p => setCart(cs => cs.find(x => x.p.id === p.id) ? cs.map(x => x.p.id === p.id ? { ...x, qty: x.qty + 1 } : x) : [...cs, { p, qty: 1 }])
  const chg = (id, d) => setCart(cs => cs.map(x => x.p.id === id ? { ...x, qty: x.qty + d } : x).filter(x => x.qty > 0))
  const sub = cart.reduce((s, x) => s + x.p.price * x.qty, 0), gst = cart.reduce((s, x) => s + x.p.price * x.qty * (x.p.gstRate || 0) / 100, 0), total = sub + gst
  const paid = c.paid === '' ? total : Math.min(Number(c.paid), total)
  const submit = async () => {
    setBusy(true)
    try { const b = await api.bill({ ...c, paid: c.paid === '' ? null : Number(c.paid), items: cart.map(x => ({ productId: x.p.id, qty: x.qty })) }, key)
      setCart([]); setC(blank); setKey(crypto.randomUUID()); toast('Bill ' + b.billNo + ' created'); setInv(b); load() } catch (e) { toast(e.message, 'err') }
    setBusy(false)
  }
  const act = (t, id) => (t === 'upi' ? api.upi : api.wa)(id).then(r => r.link.startsWith('http') ? window.open(r.link, '_blank') : setQr(r)).catch(e => toast(e.message, 'err'))
  const list = products.filter(p => !q || (p.name + (p.sku || '')).toLowerCase().includes(q.toLowerCase()))
  const cols = [{ k: 'billNo', h: 'Bill', r: r => <span className="mono">{r.billNo}</span> }, { k: 'customerName', h: 'Customer', r: r => r.customerName || 'Walk-in' }, { k: 'createdAt', h: 'Date', r: r => day(r.createdAt) },
    { k: 'total', h: 'Total', num: true, r: r => inr(r.total) }, { k: 'paid', h: 'Paid', num: true, r: r => inr(r.paid) }, { k: 'status', h: 'Status', r: r => <Badge v={r.status} /> }]
  return (<><Head title="Billing & Returns" sub="Tap products to build a bill, then charge." />
    <div className="pos">
      <div><div className="search lg"><Icon n="search" size={16} /><input placeholder="Search products or SKU" value={q} onChange={e => setQ(e.target.value)} /></div>
        {products.length === 0 ? <Empty icon="box" title="No products yet" hint="Add products in Catalog & Pricing to start billing." /> :
          <div className="tiles">{list.map(p => { const st = bal[p.id] ?? 0; return (<button key={p.id} className="tile" onClick={() => add(p)}>
            <div className="tile-top"><span className="avatar">{(p.name || '?')[0]}</span><span className={'stock' + (st <= 5 ? ' low' : '')}>{st} in stock</span></div>
            <b>{p.name}</b><span className="price">{inr(p.price)}</span><small>GST {p.gstRate || 0}%{p.unit ? ' / ' + p.unit : ''}</small></button>) })}</div>}</div>
      <aside className="card cart"><div className="card-h"><h2>Current bill</h2>{cart.length > 0 && <button className="link" onClick={() => setCart([])}>Clear</button>}</div>
        {cart.length === 0 ? <p className="mute pad0">Cart is empty.</p> : cart.map(x => <div className="cl" key={x.p.id}><div><b>{x.p.name}</b><small>{inr(x.p.price)} each</small></div>
          <div className="stepper"><button onClick={() => chg(x.p.id, -1)}>{'\u2212'}</button><span>{x.qty}</span><button onClick={() => chg(x.p.id, 1)}>+</button></div><b className="num">{inr(x.p.price * x.qty)}</b></div>)}
        <div className="sum"><div><span>Subtotal</span><b>{inr(sub)}</b></div><div><span>GST</span><b>{inr(gst)}</b></div><div className="grand"><span>Total</span><b>{inr(total)}</b></div></div>
        <div className="seg">{['CASH', 'UPI', 'CARD'].map(m => <button key={m} className={c.mode === m ? 'on' : ''} onClick={() => setC({ ...c, mode: m })}>{m}</button>)}</div>
        <div className="form2"><label>Customer<input value={c.customerName} onChange={e => setC({ ...c, customerName: e.target.value })} /></label>
          <label>Phone<input value={c.customerPhone} onChange={e => setC({ ...c, customerPhone: e.target.value })} /></label></div>
        <label>Paid now<input type="number" placeholder="Full amount" value={c.paid} onChange={e => setC({ ...c, paid: e.target.value })} /></label>
        {cart.length > 0 && total - paid > 0 && <p className="hint">{inr(total - paid)} will be added to Khata.</p>}
        <button className="btn lg" disabled={!cart.length || busy} onClick={submit}>{busy ? 'Creating...' : 'Charge ' + inr(total)}</button></aside></div>
    <h2 className="sec">Recent bills</h2>
    {bills.length === 0 ? <Empty icon="receipt" title="No bills yet" hint="Your first bill will appear here." /> :
      <DataTable cols={cols} rows={bills} actions={r => <><button className="icon-btn" title="View invoice" onClick={() => setInv(r)}><Icon n="eye" size={16} /></button>
        {canReturn && r.status !== 'RETURNED' && <button className="icon-btn danger" title="Return bill" onClick={() => api.ret(r.id).then(() => { toast('Bill returned'); load() }).catch(e => toast(e.message, 'err'))}><Icon n="undo" size={16} /></button>}</>} />}
    {inv && <Invoice b={inv} shop={shop} onClose={() => setInv(null)} act={act} />}
    {qr && <Modal onClose={() => setQr(null)}><div className="qr-box"><h3>Collect {inr(qr.amount)}</h3><img alt="UPI QR" src={'https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=' + encodeURIComponent(qr.link)} /><p className="mute">Scan with any UPI app</p></div></Modal>}</>)
}

function Module({ m, cashier }) {
  const toast = useToast(), [rows, setRows] = useState(null), [form, setForm] = useState(null), [idem, setIdem] = useState(''), [lk, setLk] = useState({})
  const ro = cashier && m.path === 'products', fields = m.fields, shown = fields.filter(f => !f.hide)
  const load = () => api.list(m.path).then(setRows).catch(e => { setRows([]); toast(e.message, 'err') })
  useEffect(() => { load(); fields.filter(f => f.lookup).forEach(f => api.list(f.lookup).then(l => setLk(x => ({ ...x, [f.lookup]: l }))).catch(() => {})) }, [])
  const open = (r = {}) => { const d = {}; fields.forEach(f => { if (f.options && !r.id) d[f.name] = f.options[0] }); setForm({ ...d, ...r }); setIdem(crypto.randomUUID()) }
  const nameOf = (f, v) => (lk[f.lookup] || []).find(x => x.id === v)?.name ?? v
  const submit = async e => {
    e.preventDefault(); const row = { ...form }
    fields.forEach(f => { if (f.type === 'number' && row[f.name] !== undefined && row[f.name] !== '') row[f.name] = Number(row[f.name]) })
    try { await api.save(m.path, row, idem); toast(form.id ? 'Updated' : 'Saved'); setForm(null); load() } catch (x) { toast(x.message, 'err') }
  }
  const cols = shown.map(f => ({ k: f.name, h: f.label || label(f.name), num: MONEY.includes(f.name) || ['gstRate', 'qty'].includes(f.name), r: r => f.lookup ? (r[f.name] == null ? '-' : nameOf(f, r[f.name])) : cell(f, r) }))
  const ic = NAV.flatMap(g => g.items).find(i => i[0] === m.path)?.[2] || 'box'
  const input = f => f.lookup ? <select value={form[f.name] ?? ''} onChange={e => setForm({ ...form, [f.name]: e.target.value })}><option value="">Select product</option>{(lk[f.lookup] || []).map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select>
    : f.options ? <select value={form[f.name] ?? f.options[0]} onChange={e => setForm({ ...form, [f.name]: e.target.value })}>{f.options.map(o => <option key={o}>{o}</option>)}</select>
    : <input type={f.type} step="any" placeholder={f.placeholder} value={form[f.name] ?? ''} onChange={e => setForm({ ...form, [f.name]: e.target.value })} />
  return (<><Head title={m.title} sub={rows ? rows.length + ' records' : ' '} action={!ro && <button className="btn" onClick={() => open()}><Icon n="plus" size={16} />New</button>} />
    {rows === null ? <div className="card skel tall" /> : rows.length === 0 ? <Empty icon={ic} title={'No ' + m.title.toLowerCase() + ' yet'} hint="Create the first record to get started." action={!ro && <button className="btn" onClick={() => open()}><Icon n="plus" size={16} />Add new</button>} /> :
      <DataTable cols={cols} rows={rows} actions={r => <>{!ro && !m.noEdit && <button className="icon-btn" title="Edit" onClick={() => open(r)}><Icon n="edit" size={16} /></button>}
        {!cashier && <button className="icon-btn danger" title="Delete" onClick={() => window.confirm('Delete this record?') && api.remove(m.path, r.id).then(() => { toast('Deleted'); load() }).catch(e => toast(e.message, 'err'))}><Icon n="trash" size={16} /></button>}</>} />}
    {form && <Drawer title={(form.id ? 'Edit ' : 'New ') + m.title} onClose={() => setForm(null)}><form onSubmit={submit}>
      {fields.map(f => <label key={f.name}>{f.label || label(f.name)}{input(f)}</label>)}
      <div className="drawer-foot"><button type="button" className="btn ghost" onClick={() => setForm(null)}>Cancel</button><button className="btn">{form.id ? 'Save changes' : 'Create'}</button></div></form></Drawer>}</>)
}

function Palette({ items, go, onClose }) {
  const [q, setQ] = useState(''), [i, setI] = useState(0), list = items.filter(x => x[1].toLowerCase().includes(q.toLowerCase()))
  const key = e => { if (e.key === 'ArrowDown') setI(Math.min(i + 1, list.length - 1)); if (e.key === 'ArrowUp') setI(Math.max(i - 1, 0)); if (e.key === 'Enter' && list[i]) { go(list[i][0]); onClose() } }
  return (<div className="overlay top" onClick={onClose}><div className="palette card" onClick={e => e.stopPropagation()}>
    <div className="search lg"><Icon n="search" size={16} /><input autoFocus placeholder="Jump to a page..." value={q} onChange={e => { setQ(e.target.value); setI(0) }} onKeyDown={key} /></div>
    {list.map((x, k) => <button key={x[0]} className={'pal' + (k === i ? ' on' : '')} style={x[3] ? { '--c': x[3] } : undefined} onClick={() => { go(x[0]); onClose() }}><Icon n={x[2]} />{x[1]}</button>)}</div></div>)
}

export default function App() {
  const [s, setS] = useState(session.get()), [view, setView] = useState(session.get()?.role === 'CASHIER' ? 'bills' : 'dashboard')
  const [open, setOpen] = useState(false), [pal, setPal] = useState(false), [prefs, setPrefs] = useState(loadPrefs), [ap, setAp] = useState(false), [toasts, setT] = useState([])
  useEffect(() => { applyPrefs(prefs) }, [prefs])
  useEffect(() => { const mq = matchMedia('(prefers-color-scheme: light)'), f = () => applyPrefs(prefs); mq.addEventListener('change', f); return () => mq.removeEventListener('change', f) }, [prefs])
  useEffect(() => {
    const mv = e => { const t = e.target.closest?.('.kpi,.tile'); if (t) { const r = t.getBoundingClientRect(); t.style.setProperty('--mx', e.clientX - r.left + 'px'); t.style.setProperty('--my', e.clientY - r.top + 'px') } }
    addEventListener('mousemove', mv); return () => removeEventListener('mousemove', mv)
  }, [])
  useEffect(() => {
    const k = e => { if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') { e.preventDefault(); setPal(p => !p) } if (e.key === 'Escape') setPal(false) }
    addEventListener('keydown', k); return () => removeEventListener('keydown', k)
  }, [])
  const setP = patch => setPrefs(p => ({ ...p, ...patch }))
  const push = (m, t = 'ok') => { const id = Math.random(); setT(x => [...x, { id, m, t }]); setTimeout(() => setT(x => x.filter(y => y.id !== id)), 3500) }
  const stack = <div className="toasts">{toasts.map(t => <div key={t.id} className={'toast ' + t.t}>{t.m}</div>)}</div>
  if (!s) return <Toasts.Provider value={push}><Login onDone={x => { setS(x); setView(x.role === 'CASHIER' ? 'bills' : 'dashboard') }} />{stack}</Toasts.Provider>
  const cashier = s.role === 'CASHIER'
  const allowed = i => i[0] === 'users' ? s.role === 'OWNER' : i[0] === 'dashboard' ? !cashier : !cashier || CASHIER.includes(i[0])
  const groups = NAV.map(g => ({ ...g, items: g.items.filter(allowed) })).filter(g => g.items.length), flat = groups.flatMap(g => g.items)
  const go = p => {
    if (p === 'action:appearance') return setAp(true)
    if (p === 'action:theme') return setP({ mode: effective(prefs) === 'dark' ? 'light' : 'dark' })
    if (p === 'action:logout') { session.clear(); return setS(null) }
    setView(p); setOpen(false)
  }, m = mods.find(x => x.path === view), cur = flat.find(i => i[0] === view)
  return (<Toasts.Provider value={push}><div className={'app' + (open ? ' nav-open' : '') + (prefs.collapsed && innerWidth > 860 ? ' collapsed' : '')}>
    <aside className="side"><div className="brand"><div className="logo">C</div><b>Clever POS</b></div>
      <nav>{groups.map(g => <div key={g.g} className="ng" style={{ '--c': g.c }}><div className="grp"><i className="gdot" />{g.g}</div>{g.items.map(i => <button key={i[0]} title={i[1]} className={'nav-i' + (view === i[0] ? ' on' : '')} style={{ '--c': i[3] }} onClick={() => go(i[0])}><i className="ni"><Icon n={i[2]} size={17} /></i><span>{i[1]}</span></button>)}</div>)}</nav>
      <div className="me"><div className="avatar">{(s.name || '?')[0].toUpperCase()}</div><div><b>{s.name}</b><small>{s.role}</small></div>
        <button className="icon-btn" title="Sign out" onClick={() => { session.clear(); setS(null) }}><Icon n="logout" size={16} /></button></div></aside>
    <div className="scrim" onClick={() => setOpen(false)} />
    <div className="main"><header className="top"><button className="icon-btn menu" onClick={() => innerWidth <= 860 ? setOpen(true) : setP({ collapsed: !prefs.collapsed })} aria-label="Menu"><Icon n="menu" /></button><span className="crumb">{cur?.[1]}</span>
      <button className="cmdk" onClick={() => setPal(true)}><Icon n="search" size={15} />Search or jump to<kbd>Ctrl K</kbd></button>
      <button className="icon-btn" title="Appearance and colours" onClick={() => setAp(true)}><Icon n="palette" /></button>
      <button className="icon-btn" title="Toggle dark / light" onClick={() => go('action:theme')}><Icon n={effective(prefs) === 'dark' ? 'sun' : 'moon'} /></button></header>
      <main key={view} className="page">{view === 'dashboard' ? <Dashboard name={s.name} go={go} /> : view === 'bills' ? <Pos canReturn={!cashier} /> : view === 'reports' ? <Reports /> : view === 'ai' ? <Assistant /> : <Module m={m} cashier={cashier} />}</main></div>
    {ap && <Appearance prefs={prefs} set={setP} onClose={() => setAp(false)} />}{pal && <Palette items={[...flat, ['action:appearance', 'Appearance and colours', 'palette'], ['action:theme', 'Toggle dark / light', 'sun'], ['action:logout', 'Sign out', 'logout']]} go={go} onClose={() => setPal(false)} />}{stack}</div></Toasts.Provider>)
}
