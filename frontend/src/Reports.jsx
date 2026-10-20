import { useEffect, useRef, useState } from 'react'
import { api } from './api'
import { Icon, inr, useToast, Head, Badge, Num } from './ui.jsx'
const iso = d => d.toISOString().slice(0, 10)
const ago = n => { const d = new Date(); d.setDate(d.getDate() - n); return iso(d) }
const short = s => new Date(s).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })

function Area({ data }) {
  const v = data.map(d => Number(d[1])), max = Math.max(1, ...v), n = Math.max(1, v.length - 1)
  const line = v.map((x, i) => (i ? 'L' : 'M') + (i * 600 / n) + ' ' + (130 - x / max * 110)).join(' ')
  return (<><svg viewBox="0 0 600 150" className="area" preserveAspectRatio="none"><defs><linearGradient id="g2" x1="0" x2="0" y1="0" y2="1"><stop offset="0" stopColor="var(--accent)" stopOpacity=".35" /><stop offset="1" stopColor="var(--accent)" stopOpacity="0" /></linearGradient></defs>
    <path d={line + ' L600 150 L0 150Z'} fill="url(#g2)" /><path d={line} fill="none" stroke="var(--accent)" strokeWidth="2.5" vectorEffect="non-scaling-stroke" /></svg>
    <div className="xlab"><small>{data[0] && short(data[0][0])}</small><small>{data.length > 0 && short(data[data.length - 1][0])}</small></div></>)
}
const Bars = ({ rows, fmt }) => { const max = Math.max(1, ...rows.map(r => Number(r.amount))); return rows.length === 0 ? <p className="mute">Nothing to show for this period.</p> :
  rows.map(r => <div className="lrow" key={r.name}><div><span>{r.name}</span><b>{fmt ? fmt(r) : inr(r.amount)}</b></div><div className="meter"><i style={{ width: Number(r.amount) / max * 100 + '%' }} /></div></div>) }

export function Reports() {
  const toast = useToast(), [from, setFrom] = useState(ago(29)), [to, setTo] = useState(iso(new Date())), [d, setD] = useState(null)
  useEffect(() => { setD(null); api.detail(from, to).then(setD).catch(e => { toast(e.message, 'err'); setD(false) }) }, [from, to])
  const preset = n => { setFrom(ago(n - 1)); setTo(iso(new Date())) }
  const csv = () => {
    const head = ['Bill', 'Date', 'Customer', 'Total', 'GST', 'Paid', 'Mode', 'Status']
    const lines = [head, ...d.rows.map(r => [r.billNo, r.date, r.customer, r.total, r.gst, r.paid, r.mode, r.status])].map(r => r.map(x => '"' + String(x ?? '').replace(/"/g, '""') + '"').join(','))
    const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([lines.join('\n')], { type: 'text/csv' })); a.download = 'bills-' + from + '-to-' + to + '.csv'; a.click()
  }
  const modes = d ? Object.entries(d.byMode).map(([name, amount]) => ({ name, amount })) : []
  return (<><Head title="Reports" sub="Sales, GST, stock and khata in one place." action={d && <button className="btn ghost" onClick={csv}><Icon n="print" size={16} />Export CSV</button>} />
    <div className="range" style={{ marginBottom: 18 }}>{[[7, '7 days'], [30, '30 days'], [90, '90 days']].map(([n, l]) => <button key={n} className="pill" onClick={() => preset(n)}>{l}</button>)}
      <input type="date" value={from} max={to} onChange={e => setFrom(e.target.value)} /><span className="mute">to</span><input type="date" value={to} min={from} onChange={e => setTo(e.target.value)} /></div>
    {!d ? <div className="grid4">{[0, 1, 2, 3].map(i => <div key={i} className="card skel" />)}</div> : <>
      <div className="grid4">
        <div className="card kpi accent"><div className="chip"><Icon n="trend" /></div><span>Net sales</span><b><Num v={d.sales} money /></b><small>{d.bills} bills</small></div>
        <div className="card kpi a2"><div className="chip"><Icon n="receipt" /></div><span>GST collected</span><b><Num v={d.gst} money /></b></div>
        <div className="card kpi warn"><div className="chip"><Icon n="wallet" /></div><span>Average bill</span><b><Num v={d.avg} money /></b></div>
        <div className="card kpi a2"><div className="chip"><Icon n="trend" /></div><span>Gross profit</span><b><Num v={d.profit} money /></b><small>Where cost price is set</small></div>
        <div className="card kpi danger"><div className="chip"><Icon n="undo" /></div><span>Returns</span><b><Num v={d.returns} money /></b></div></div>
      <div className="split"><div className="card pad"><div className="card-h"><h2>Sales trend</h2></div><Area data={Object.entries(d.daily)} /></div>
        <div className="card pad"><div className="card-h"><h2>Collected by payment mode</h2></div><Bars rows={modes} /></div></div>
      <div className="split"><div className="card pad"><div className="card-h"><h2>Top products</h2></div><Bars rows={d.topProducts} fmt={r => r.qty + ' sold, ' + inr(r.amount)} /></div>
        <div className="card pad"><div className="card-h"><h2>Khata outstanding</h2></div><Bars rows={d.khataOwing} /></div></div>
      <div className="card pad"><div className="card-h"><h2>Low stock (5 or fewer)</h2></div>
        {d.lowStock.length === 0 ? <p className="mute">All products are well stocked.</p> : d.lowStock.map(p => <div className="lrow" key={p.name}><div><span>{p.name}</span><Badge v={p.balance <= 0 ? 'OUT' : p.balance + ' left'} /></div></div>)}</div></>}</>)
}

export function Assistant() {
  const toast = useToast(), end = useRef(null), [q, setQ] = useState(''), [busy, setBusy] = useState(false), [ins, setIns] = useState([])
  const [msgs, setM] = useState([{ r: 'ai', t: 'Hi! I can read your sales, stock and khata numbers. Ask me anything about your shop, in English or Hinglish.' }])
  useEffect(() => { api.insights().then(r => setIns(r.insights)).catch(() => {}) }, [])
  useEffect(() => { end.current?.scrollIntoView({ behavior: 'smooth' }) }, [msgs, busy])
  const send = async text => {
    const t = (text ?? q).trim(); if (!t || busy) return
    setQ(''); setM(m => [...m, { r: 'me', t }]); setBusy(true)
    try { const r = await api.ask(t); setM(m => [...m, { r: 'ai', t: r.answer }]) } catch (e) { setM(m => [...m, { r: 'ai', t: e.message, err: true }]); toast(e.message, 'err') }
    setBusy(false)
  }
  const ideas = ['Summarize my last 30 days', 'Which products should I restock?', 'Who should I remind about khata?', 'How can I increase my profit?', 'Is my GST collection normal?']
  return (<><Head title="AI Assistant" sub="Ask questions about your own shop data." />
    <div className="chat-wrap"><div className="card chat"><div className="msgs">
      {msgs.map((m, i) => <div key={i} className={'msg ' + m.r}>{m.r === 'ai' && <div className="avatar ai-av"><Icon n="spark" size={16} /></div>}<div className={'bubble' + (m.err ? ' err' : '')}>{m.t}</div></div>)}
      {busy && <div className="msg ai"><div className="avatar ai-av"><Icon n="spark" size={16} /></div><div className="bubble dots"><span /><span /><span /></div></div>}<div ref={end} /></div>
      <form className="composer" onSubmit={e => { e.preventDefault(); send() }}><input placeholder="Ask about sales, stock, khata..." value={q} onChange={e => setQ(e.target.value)} /><button className="btn" disabled={busy || !q.trim()}>Send</button></form></div>
      <div style={{ display: 'grid', gap: 16 }}><div className="card pad"><div className="card-h"><h2>Try asking</h2></div><div className="ideas">{ideas.map(x => <button key={x} className="idea" onClick={() => send(x)}>{x}</button>)}</div></div>
        <div className="card pad"><div className="card-h"><h2>Quick insights</h2></div>{ins.map(x => <p key={x} className="insight">{x}</p>)}</div></div></div></>)
}
