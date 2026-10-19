import { createContext, useContext, useEffect, useMemo, useState } from 'react'
const P = {
  home: 'M3 12l9-9 9 9M5 10v10h5v-6h4v6h5V10', receipt: 'M6 2h12v20l-3-2-3 2-3-2-3 2zM9 7h6M9 11h6', box: 'M21 8l-9-5-9 5v8l9 5 9-5zM3 8l9 5 9-5M12 13v8',
  layers: 'M12 2l10 6-10 6L2 8zM2 14l10 6 10-6', cart: 'M3 4h2l2.5 11h10L20 7H6M9 20h.01M17 20h.01', book: 'M4 4h12a3 3 0 0 1 3 3v13H7a3 3 0 0 1-3-3zM4 17a3 3 0 0 1 3-3h12',
  wallet: 'M3 7h16a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3 7l2-3h12v3M16 13h2', store: 'M3 9l2-5h14l2 5M3 9h18M4 9v11h16V9M9 20v-6h6v6',
  users: 'M16 20v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8M22 20v-2a4 4 0 0 0-3-3.9M16 2.1a4 4 0 0 1 0 7.8',
  search: 'M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16M21 21l-4.3-4.3', plus: 'M12 5v14M5 12h14', x: 'M18 6L6 18M6 6l12 12',
  sun: 'M12 17a5 5 0 1 0 0-10 5 5 0 0 0 0 10M12 1v2M12 21v2M4.2 4.2l1.4 1.4M18.4 18.4l1.4 1.4M1 12h2M21 12h2M4.2 19.8l1.4-1.4M18.4 5.6l1.4-1.4',
  moon: 'M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z', logout: 'M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9',
  spark: 'M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8zM19 16l.8 2.2L22 19l-2.2.8L19 22l-.8-2.2L16 19l2.2-.8z',
  print: 'M6 9V2h12v7M6 18H4v-7h16v7h-2M6 14h12v8H6z', qr: 'M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h3v3h-3zM20 14v.01M14 20h3M20 17v4',
  chat: 'M21 11.5a8.5 8.5 0 0 1-12.6 7.4L3 20l1.2-5.2A8.5 8.5 0 1 1 21 11.5', undo: 'M3 7v6h6M3 13a9 9 0 1 0 3-7', trash: 'M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14',
  edit: 'M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z', menu: 'M3 6h18M3 12h18M3 18h18', trend: 'M3 17l6-6 4 4 8-8M15 7h6v6', check: 'M20 6L9 17l-5-5', palette: 'M12 3a9 9 0 1 0 0 18c1.1 0 2-.9 2-2 0-.5-.2-1-.5-1.3-.3-.3-.5-.8-.5-1.2 0-1.1.9-2 2-2h2.3A3.7 3.7 0 0 0 21 11 9 9 0 0 0 12 3M7.5 11h.01M10 7.5h.01M14 7.5h.01M16.5 11h.01', eye: 'M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6'
}
export const Icon = ({ n, size = 18 }) => (<svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={P[n]} /></svg>)
export const inr = n => '\u20B9' + Number(n || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })
export const label = s => s.replace(/([A-Z])/g, ' $1').replace(/^./, c => c.toUpperCase())
export const Toasts = createContext(() => {})
export const useToast = () => useContext(Toasts)
export const Badge = ({ v }) => <span className={'badge b-' + String(v).toLowerCase()}>{v}</span>
export const Drawer = ({ title, onClose, children }) => (<div className="overlay" onClick={onClose}><aside className="drawer" onClick={e => e.stopPropagation()}>
  <header><h3>{title}</h3><button className="icon-btn" onClick={onClose} aria-label="Close"><Icon n="x" /></button></header><div className="drawer-body">{children}</div></aside></div>)
export const Modal = ({ onClose, children, wide }) => (<div className="overlay center" onClick={onClose}><div className={'modal card' + (wide ? ' wide' : '')} onClick={e => e.stopPropagation()}>
  <button className="icon-btn close no-print" onClick={onClose} aria-label="Close"><Icon n="x" /></button>{children}</div></div>)
export const Empty = ({ icon, title, hint, action }) => (<div className="card empty"><div className="empty-ico"><Icon n={icon} size={26} /></div><h3>{title}</h3><p>{hint}</p>{action}</div>)
export function DataTable({ cols, rows, actions, pageSize = 8 }) {
  const [q, setQ] = useState(''), [sort, setSort] = useState(null), [pg, setPg] = useState(0)
  const f = useMemo(() => {
    let r = rows.filter(x => !q || JSON.stringify(Object.values(x)).toLowerCase().includes(q.toLowerCase()))
    if (sort) r = [...r].sort((a, b) => { const x = a[sort.k] ?? '', y = b[sort.k] ?? ''; return (x > y ? 1 : x < y ? -1 : 0) * sort.d })
    return r
  }, [rows, q, sort])
  const pages = Math.max(1, Math.ceil(f.length / pageSize)), cur = Math.min(pg, pages - 1), view = f.slice(cur * pageSize, cur * pageSize + pageSize)
  return (<div className="card table-card">
    <div className="toolbar"><div className="search"><Icon n="search" size={16} /><input placeholder="Search records" value={q} onChange={e => { setQ(e.target.value); setPg(0) }} /></div></div>
    <div className="table-wrap"><table><thead><tr>{cols.map(c => <th key={c.k} className={c.num ? 'num' : ''} onClick={() => setSort(s => ({ k: c.k, d: s?.k === c.k ? -s.d : 1 }))}>{c.h}{sort?.k === c.k ? (sort.d > 0 ? ' \u2191' : ' \u2193') : ''}</th>)}{actions && <th />}</tr></thead>
      <tbody>{view.map(r => <tr key={r.id}>{cols.map(c => <td key={c.k} className={c.num ? 'num' : ''}>{c.r ? c.r(r) : (r[c.k] ?? '-')}</td>)}{actions && <td className="act">{actions(r)}</td>}</tr>)}</tbody></table>
      {f.length === 0 && <div className="empty-inline">No matching records</div>}</div>
    <div className="pager"><span>{f.length} records</span><div><button className="icon-btn" disabled={cur === 0} onClick={() => setPg(cur - 1)}>{'\u2039'}</button><span>{cur + 1} / {pages}</span><button className="icon-btn" disabled={cur >= pages - 1} onClick={() => setPg(cur + 1)}>{'\u203A'}</button></div></div></div>)
}
export const Head = ({ title, sub, action }) => <div className="page-head"><div><h1>{title}</h1>{sub && <p className="mute">{sub}</p>}</div>{action}</div>
export function Num({ v, money }) {
  const [x, setX] = useState(0)
  useEffect(() => {
    const n = Number(v) || 0; let raf, t0
    const f = t => { t0 ??= t; const p = Math.min(1, (t - t0) / 700); setX(p < 1 ? n * (1 - Math.pow(1 - p, 3)) : n); if (p < 1) raf = requestAnimationFrame(f) }
    raf = requestAnimationFrame(f); return () => cancelAnimationFrame(raf)
  }, [v])
  return money ? inr(Math.round(x * 100) / 100) : Math.round(x).toLocaleString('en-IN')
}
