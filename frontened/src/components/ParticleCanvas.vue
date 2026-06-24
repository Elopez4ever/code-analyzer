<script setup>
/**
 * 精简背景 —— 60粒子 + 2光球 + 光标聚光灯
 * 去掉了网格、减少了shadowBlur调用、优化连线算法
 */
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({ theme: { type: String, default: 'dark' } })
const canvasRef = ref(null)
let animId, ctx, ps
let mx = -600, my = -600, smx = -600, smy = -600
let mActive = false

const W = () => window.innerWidth, H = () => window.innerHeight
const N = 60
const rnd = (a, b) => a + Math.random() * (b - a)

const S = {
  dark: {
    bg: '#04030a',
    orbColors: [[99, 102, 241], [139, 92, 246]],
    orbAlpha: [0.10, 0.04],
    pColors: [[129, 140, 248], [167, 139, 250], [34, 211, 238], [244, 114, 182], [56, 189, 248], [192, 132, 252]],
    link: '129,140,248',
    mouseLink: '168,139,250',
    spot: ['rgba(129,140,248,0.07)', 'rgba(139,92,246,0.03)', 'rgba(0,0,0,0)'],
    glowAlpha: '0.35',
  },
  light: {
    bg: '#f8f7fc',
    orbColors: [[99, 102, 241], [139, 92, 246]],
    orbAlpha: [0.04, 0.02],
    pColors: [[99, 102, 241], [139, 92, 246], [59, 130, 246], [168, 85, 247], [37, 99, 235], [124, 58, 237]],
    link: '99,102,241',
    mouseLink: '139,92,246',
    spot: ['rgba(99,102,241,0.04)', 'rgba(139,92,246,0.02)', 'rgba(0,0,0,0)'],
    glowAlpha: '0.2',
  },
}
let C = S[props.theme] || S.dark
watch(() => props.theme, t => { C = S[t] || S.dark })

/* orbs */
const orbs = [
  { x: 0.2, y: 0.25, r: 0.4, ph: 0 },
  { x: 0.75, y: 0.6, r: 0.34, ph: 2.5 },
]

/* particles */
function makeP(w, h) {
  const ci = Math.floor(Math.random() * C.pColors.length)
  const a = Math.random() * Math.PI * 2, s = rnd(0.08, 0.25)
  return { x: Math.random()*w, y: Math.random()*h, vx: Math.cos(a)*s, vy: Math.sin(a)*s, r: rnd(1.5,4), op: rnd(0.2,0.5), ci, ps: rnd(0.008,0.025), po: Math.random()*Math.PI*2, fl: 0, flT: rnd(2,10) }
}

function resize() {
  const c = canvasRef.value
  if (!c) return
  const dpr = devicePixelRatio || 1
  const w = W(), h = H()
  c.width = w * dpr; c.height = h * dpr
  c.style.width = w + 'px'; c.style.height = h + 'px'
  if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
}

function init() {
  const c = canvasRef.value
  if (!c) return
  ctx = c.getContext('2d')
  resize()
  ps = Array.from({ length: N }, () => makeP(W(), H()))
}

/* mouse — 较粗粒度节流 */
let lastMM = 0
function onMM(e) {
  const now = performance.now()
  if (now - lastMM < 24) return
  lastMM = now
  mx = e.clientX; my = e.clientY
  if (!mActive) mActive = true
}
function onML() { mActive = false }

/* ── orbs ──────────────────── */
function drawOrbs(w, h, t) {
  for (let i = 0; i < orbs.length; i++) {
    const o = orbs[i]
    const ox = (o.x + Math.sin(t * 0.25 + o.ph) * 0.06) * w
    const oy = (o.y + Math.cos(t * 0.2 + o.ph) * 0.06) * h
    const or = o.r * Math.min(w, h) * 1.2
    const g = ctx.createRadialGradient(ox, oy, 0, ox, oy, or)
    const [cr, cg, cb] = C.orbColors[i % 2]
    g.addColorStop(0, `rgba(${cr},${cg},${cb},${C.orbAlpha[0]})`)
    g.addColorStop(0.5, `rgba(${cr},${cg},${cb},${C.orbAlpha[1]})`)
    g.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.fillStyle = g
    ctx.beginPath(); ctx.arc(ox, oy, or, 0, Math.PI * 2); ctx.fill()
  }
}

/* ── spotlight ──────────────── */
function drawSpot() {
  if (!mActive) return
  smx += (mx - smx) * 0.05
  smy += (my - smy) * 0.05
  const g = ctx.createRadialGradient(smx, smy, 30, smx, smy, 380)
  g.addColorStop(0, C.spot[0])
  g.addColorStop(0.4, C.spot[1])
  g.addColorStop(1, C.spot[2])
  ctx.fillStyle = g
  ctx.beginPath(); ctx.arc(smx, smy, 380, 0, Math.PI * 2); ctx.fill()
}

/* ── particles ──────────────── */
function drawP(w, h, t) {
  for (const p of ps) {
    // 光球微引力
    for (let i = 0; i < orbs.length; i++) {
      const o = orbs[i]
      const ox = (o.x + Math.sin(t * 0.25 + o.ph) * 0.06) * w
      const oy = (o.y + Math.cos(t * 0.2 + o.ph) * 0.06) * h
      const dx = ox - p.x, dy = oy - p.y, d = Math.hypot(dx, dy)
      if (d < o.r * Math.min(w, h) * 1.2 && d > 1) { const f = 0.00012; p.vx += (dx/d)*f; p.vy += (dy/d)*f }
    }
    // 光标微引力
    if (mActive) {
      const dx = mx - p.x, dy = my - p.y, d = Math.hypot(dx, dy)
      if (d < 240 && d > 1) { const f = (1 - d/240) * 0.014; p.vx += (dx/d)*f; p.vy += (dy/d)*f }
    }
    p.x += p.vx; p.y += p.vy
    p.vx *= 0.994; p.vy *= 0.994
    p.vx += rnd(-0.004, 0.004); p.vy += rnd(-0.004, 0.004)
    const sp = Math.hypot(p.vx, p.vy)
    if (sp > 1.1) { p.vx = (p.vx/sp)*1.1; p.vy = (p.vy/sp)*1.1 }
    if (p.x < -10) p.x = w + 10; if (p.x > w + 10) p.x = -10
    if (p.y < -10) p.y = h + 10; if (p.y > h + 10) p.y = -10

    p.flT -= 0.016
    if (p.flT <= 0) { p.fl = 1; p.flT = rnd(3, 12) }
    p.fl = Math.max(0, p.fl - 0.018)

    const pulse = Math.sin(t * p.ps * 60 + p.po) * 0.3 + 0.7
    const alpha = Math.min(1, p.op * pulse + p.fl * 0.4)
    const r = p.r * (0.6 + pulse * 0.4 + p.fl * 0.3)
    const [cr, cg, cb] = C.pColors[p.ci]

    // 只在需要时开shadowBlur——邻近鼠标或闪烁时
    const nearMouse = mActive && Math.hypot(mx - p.x, my - p.y) < 300
    if (nearMouse || p.fl > 0.3) {
      ctx.shadowBlur = 10
      ctx.shadowColor = `rgba(${cr},${cg},${cb},${C.glowAlpha})`
    }
    ctx.beginPath(); ctx.arc(p.x, p.y, r, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${cr},${cg},${cb},${alpha})`
    ctx.fill()
    ctx.shadowBlur = 0

    // 中心点
    ctx.beginPath(); ctx.arc(p.x, p.y, r * 0.22, 0, Math.PI * 2)
    ctx.fillStyle = C === S.dark
      ? `rgba(255,255,255,${Math.min(1, alpha * 1.3)})`
      : `rgba(99,102,241,${Math.min(1, alpha * 1.1)})`
    ctx.fill()
  }

  // 连线——缩短距离、减少计算
  const LINK = 110
  for (let i = 0; i < ps.length; i++) {
    for (let j = i + 1; j < ps.length; j++) {
      const dx = ps[i].x - ps[j].x, dy = ps[i].y - ps[j].y
      if (Math.abs(dx) > LINK || Math.abs(dy) > LINK) continue // 快速剔除
      const d = Math.hypot(dx, dy)
      if (d < LINK) {
        const a = (1 - d / LINK) * 0.08
        ctx.beginPath(); ctx.moveTo(ps[i].x, ps[i].y); ctx.lineTo(ps[j].x, ps[j].y)
        ctx.strokeStyle = `rgba(${C.link},${a})`
        ctx.lineWidth = 0.4; ctx.stroke()
      }
    }
    // 鼠标连线
    if (mActive) {
      const dx = mx - ps[i].x, dy = my - ps[i].y
      if (Math.abs(dx) > 180 || Math.abs(dy) > 180) continue
      const d = Math.hypot(dx, dy)
      if (d < 180) {
        const a = (1 - d / 180) * 0.12
        ctx.beginPath(); ctx.moveTo(ps[i].x, ps[i].y); ctx.lineTo(mx, my)
        ctx.strokeStyle = `rgba(${C.mouseLink},${a})`
        ctx.lineWidth = 0.5; ctx.stroke()
      }
    }
  }
}

/* ── loop ───────────────────── */
function draw() {
  if (!ctx) return
  const w = W(), h = H(), t = performance.now() * 0.001
  ctx.fillStyle = C.bg; ctx.fillRect(0, 0, w, h)
  drawOrbs(w, h, t)
  drawSpot()
  drawP(w, h, t)
  animId = requestAnimationFrame(draw)
}

onMounted(() => { init(); draw(); window.addEventListener('resize', resize); window.addEventListener('mousemove', onMM, { passive: true }); window.addEventListener('mouseleave', onML) })
onBeforeUnmount(() => { if (animId) cancelAnimationFrame(animId); window.removeEventListener('resize', resize); window.removeEventListener('mousemove', onMM); window.removeEventListener('mouseleave', onML) })
</script>

<template>
  <canvas ref="canvasRef" class="bg-canvas" aria-hidden="true" />
</template>

<style scoped>
.bg-canvas { position: fixed; inset: 0; width: 100vw; height: 100vh; pointer-events: none; z-index: 0; }
</style>
