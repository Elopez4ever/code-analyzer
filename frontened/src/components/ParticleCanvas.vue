<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const canvasRef = ref(null)
let animId = null
let particles = []
let ctx = null

const PARTICLE_COUNT = 100
const colors = [
  'rgba(168, 85, 247, ',
  'rgba(96, 165, 250, ',
  'rgba(34, 211, 238, ',
  'rgba(139, 92, 246, ',
  'rgba(59, 130, 246, ',
]

function randomBetween(a, b) {
  return a + Math.random() * (b - a)
}

function createParticle(w, h) {
  const colorIndex = Math.floor(Math.random() * colors.length)
  return {
    x: Math.random() * w,
    y: Math.random() * h,
    vx: randomBetween(-0.35, 0.35),
    vy: randomBetween(-0.35, 0.35),
    r: randomBetween(2, 4.5),
    opacity: randomBetween(0.3, 0.7),
    color: colors[colorIndex],
    pulseSpeed: randomBetween(0.02, 0.05),
    pulseOffset: Math.random() * Math.PI * 2,
  }
}

function resize() {
  const canvas = canvasRef.value
  if (!canvas) return
  const dpr = window.devicePixelRatio || 1
  const w = window.innerWidth
  const h = window.innerHeight
  canvas.width = w * dpr
  canvas.height = h * dpr
  canvas.style.width = w + 'px'
  canvas.style.height = h + 'px'
  if (ctx) {
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  }
}

function init() {
  const canvas = canvasRef.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  resize()
  particles = Array.from({ length: PARTICLE_COUNT }, () =>
    createParticle(window.innerWidth, window.innerHeight),
  )
}

function draw() {
  if (!ctx) return
  const w = window.innerWidth
  const h = window.innerHeight
  const time = Date.now() * 0.001

  ctx.clearRect(0, 0, w, h)

  for (const p of particles) {
    p.x += p.vx
    p.y += p.vy

    if (p.x < -10) p.x = w + 10
    if (p.x > w + 10) p.x = -10
    if (p.y < -10) p.y = h + 10
    if (p.y > h + 10) p.y = -10

    const pulse = Math.sin(time * p.pulseSpeed * 60 + p.pulseOffset) * 0.3 + 0.7
    const currentOpacity = p.opacity * pulse
    const currentRadius = p.r * pulse

    ctx.beginPath()
    ctx.arc(p.x, p.y, currentRadius, 0, Math.PI * 2)
    ctx.fillStyle = p.color + currentOpacity + ')'
    ctx.fill()

    ctx.beginPath()
    ctx.arc(p.x, p.y, currentRadius * 2.5, 0, Math.PI * 2)
    ctx.fillStyle = p.color + (currentOpacity * 0.15) + ')'
    ctx.fill()
  }

  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < 140) {
        const alpha = (1 - dist / 140) * 0.15
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = particles[i].color + alpha + ')'
        ctx.lineWidth = 0.5
        ctx.stroke()
      }
    }
  }

  animId = requestAnimationFrame(draw)
}

onMounted(() => {
  init()
  draw()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  if (animId) cancelAnimationFrame(animId)
  window.removeEventListener('resize', resize)
})
</script>

<template>
  <canvas ref="canvasRef" class="particle-canvas" aria-hidden="true"></canvas>
</template>

<style scoped>
.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 0;
}
</style>