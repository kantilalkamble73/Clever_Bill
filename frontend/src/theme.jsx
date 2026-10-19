import { Icon, Drawer } from './ui.jsx'
export const PRESETS = [['Emerald', '#34d399', '#6366f1'], ['Indigo', '#818cf8', '#22d3ee'], ['Rose', '#fb7185', '#a78bfa'], ['Amber', '#fbbf24', '#f97316'], ['Sky', '#38bdf8', '#818cf8'], ['Violet', '#a78bfa', '#f472b6'], ['Orange', '#fb923c', '#facc15'], ['Teal', '#2dd4bf', '#60a5fa']]
export const FONTS = [['Modern', 'Plus Jakarta Sans', 'Plus Jakarta Sans'], ['Clean', 'Inter', 'Inter'], ['Elegant', 'Manrope', 'Sora'], ['Techy', 'Space Grotesk', 'Space Grotesk']]
export const DEFAULTS = { font: 'Modern', size: 'm', mode: 'dark', accent: '#34d399', a2: '#6366f1', density: 'comfortable', collapsed: false }
export const loadPrefs = () => { try { return { ...DEFAULTS, ...JSON.parse(localStorage.getItem('cp-prefs') || '{}') } } catch { return DEFAULTS } }
const rgb = h => [1, 3, 5].map(i => parseInt(h.slice(i, i + 2), 16))
const hex = a => '#' + a.map(v => Math.round(Math.max(0, Math.min(255, v))).toString(16).padStart(2, '0')).join('')
const mix = (h, t, p) => hex(rgb(h).map((v, i) => v + (t[i] - v) * p))
const lum = h => { const [r, g, b] = rgb(h).map(v => { v /= 255; return v <= .03928 ? v / 12.92 : ((v + .055) / 1.055) ** 2.4 }); return .2126 * r + .7152 * g + .0722 * b }
export function hueShift(h, deg) {
  const [r, g, b] = rgb(h).map(v => v / 255), mx = Math.max(r, g, b), mn = Math.min(r, g, b), l = (mx + mn) / 2
  let hh = 0, s = 0
  if (mx !== mn) { const d = mx - mn; s = l > .5 ? d / (2 - mx - mn) : d / (mx + mn); hh = mx === r ? (g - b) / d + (g < b ? 6 : 0) : mx === g ? (b - r) / d + 2 : (r - g) / d + 4; hh *= 60 }
  hh = (hh + deg + 360) % 360
  const f = n => { const k = (n + hh / 30) % 12, a = s * Math.min(l, 1 - l); return (l - a * Math.max(-1, Math.min(k - 3, 9 - k, 1))) * 255 }
  return hex([f(0), f(8), f(4)])
}
export const effective = p => p.mode === 'auto' ? (matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark') : p.mode
export function applyPrefs(p) {
  const r = document.documentElement, t = effective(p), light = t === 'light'
  const acc = light ? mix(p.accent, [0, 0, 0], .2) : p.accent, a2 = light ? mix(p.a2, [0, 0, 0], .12) : p.a2
  r.dataset.theme = t; r.dataset.density = p.density
  r.style.setProperty('--accent', acc); r.style.setProperty('--a2', a2); r.style.setProperty('--ink', lum(acc) > .4 ? '#06130f' : '#fff')
  const F = FONTS.find(f => f[0] === p.font) || FONTS[0], fb = ", 'Noto Sans Devanagari', system-ui, sans-serif"
  r.style.setProperty('--font-body', "'" + F[1] + "'" + fb); r.style.setProperty('--font-head', "'" + F[2] + "'" + fb)
  r.style.setProperty('--fs', { s: '13.5px', m: '14.5px', l: '16px' }[p.size] || '14.5px')
  localStorage.setItem('cp-prefs', JSON.stringify(p))
}
export function Appearance({ prefs, set, onClose }) {
  return (<Drawer title="Appearance" onClose={onClose}><div className="ap">
    <h4>Mode</h4><div className="seg">{['dark', 'light', 'auto'].map(m => <button key={m} className={prefs.mode === m ? 'on' : ''} onClick={() => set({ mode: m })}>{m[0].toUpperCase() + m.slice(1)}</button>)}</div>
    <h4>Accent colour</h4><div className="swatches">{PRESETS.map(([n, a, b]) => <button key={n} title={n} aria-label={n} className={'sw' + (prefs.accent === a ? ' on' : '')} style={{ background: `linear-gradient(135deg,${a},${b})` }} onClick={() => set({ accent: a, a2: b })}>{prefs.accent === a && <Icon n="check" size={16} />}</button>)}</div>
    <label className="custom">Custom colour<input type="color" value={prefs.accent} onChange={e => set({ accent: e.target.value, a2: hueShift(e.target.value, 60) })} /></label>
    <h4>Density</h4><div className="seg" style={{ gridTemplateColumns: '1fr 1fr' }}>{['comfortable', 'compact'].map(m => <button key={m} className={prefs.density === m ? 'on' : ''} onClick={() => set({ density: m })}>{m[0].toUpperCase() + m.slice(1)}</button>)}</div>
    <h4>Font style</h4><div className="fonts">{FONTS.map(([n, b, h]) => <button key={n} className={'fbtn' + (prefs.font === n ? ' on' : '')} onClick={() => set({ font: n })}><span style={{ fontFamily: `'${h}', sans-serif` }}>Aa</span><b style={{ fontFamily: `'${b}', sans-serif` }}>{n}</b></button>)}</div>
    <h4>Text size</h4><div className="seg">{[['s', 'Small'], ['m', 'Default'], ['l', 'Large']].map(([k, l]) => <button key={k} className={prefs.size === k ? 'on' : ''} onClick={() => set({ size: k })}>{l}</button>)}</div>
    <h4>Preview</h4><div className="card pad prev"><div className="chip" style={{ '--c': 'var(--accent)' }}><Icon n="trend" /></div><b style={{ fontFamily: 'var(--font-head)', fontSize: 22 }}>{'\u20B9'}12,450</b><button className="btn">Primary</button><span className="badge b-paid">PAID</span><span className="badge b-due">DUE</span></div>
    <button className="btn ghost" style={{ marginTop: 18, width: '100%' }} onClick={() => set({ mode: 'dark', accent: DEFAULTS.accent, a2: DEFAULTS.a2, density: 'comfortable', font: 'Modern', size: 'm' })}>Reset to default</button></div></Drawer>)
}
