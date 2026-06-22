<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import ParticleCanvas from '../components/ParticleCanvas.vue'

const root = ref(null)
const pipelineRef = ref(null)
let observer = null

// IntersectionObserver for scroll reveal animations
onMounted(() => {
  const targets = root.value?.querySelectorAll('.reveal') || []
  if (!('IntersectionObserver' in window)) {
    targets.forEach((target) => target.classList.add('is-visible'))
    return
  }

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          observer.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.15, rootMargin: '0px 0px -40px 0px' },
  )

  targets.forEach((target) => observer.observe(target))
})

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
})

function scrollToPipeline() {
  pipelineRef.value?.scrollIntoView({ behavior: 'smooth' })
}

// Floating tech icons
const floatingIcons = [
  { label: 'Java', cls: 'fi-java' },
  { label: 'Python', cls: 'fi-py' },
  { label: 'Vue.js', cls: 'fi-vue' },
  { label: 'PostgreSQL', cls: 'fi-pg' },
  { label: 'AI/LLM', cls: 'fi-ai' },
  { label: 'Git', cls: 'fi-git' },
  { label: 'Docker', cls: 'fi-dock' },
]

// Pipeline steps
const pipelineSteps = [
  {
    num: '01',
    icon: 'M12 3v13.5M7 11l5 5.5 5-5.5M20 21H4',
    title: '上传项目',
    desc: '支持 Git 仓库克隆或 ZIP 文件上传，自动识别 Spring Boot 等主流项目结构',
  },
  {
    num: '02',
    icon: 'M14.5 2h-9A1.5 1.5 0 0 0 4 3.5v17A1.5 1.5 0 0 0 5.5 22h13a1.5 1.5 0 0 0 1.5-1.5V7.5L14.5 2z M14 2v5.5a.5.5 0 0 0 .5.5H20 M8 13h8 M8 17h8 M8 9h2',
    title: '智能解析',
    desc: '按类与方法粒度切分代码块，提取包名、注解、方法签名等元数据',
  },
  {
    num: '03',
    icon: 'M9.663 17h4.673M12 3v1m-8 9h1m14 0h1M5.636 5.636l.707.707m11.314 11.314.707.707M5.636 18.364l.707-.707M4 12a8 8 0 1 0 16 0 8 8 0 0 0-16 0zm4 0a4 4 0 1 0 8 0 4 4 0 0 0-8 0z',
    title: 'LLM 智能摘要',
    desc: '调用大语言模型为每个代码块生成中文自然语言描述，让检索更精准、问答更有上下文',
  },
  {
    num: '04',
    icon: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.3 7l8.7 5 8.7-5',
    title: '向量嵌入',
    desc: 'LLM 生成中文摘要后，通过 Embedding 模型将语义转换为 1536 维向量',
  },
]

// Feature cards
const features = [
  {
    icon: 'M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z M4 22v-7',
    title: '多语言解析',
    desc: '内置 Java 解析器，支持 Spring Boot 项目自动检测。可扩展支持 Python、TypeScript 等多种语言',
  },
  {
    icon: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M12 22V12 M12 12a4 4 0 0 1 4-4 M8 8v8',
    title: '多维向量检索',
    desc: '基于 pgvector 的高效语义搜索，支持语言、类型、路径等多维度过滤，毫秒级返回最相关代码块',
  },
  {
    icon: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M12 22V12 M3.3 7l8.7 5 8.7-5',
    title: '分层代码切分',
    desc: '按类与方法粒度切分，保留完整结构信息与行号映射，支持精准定位到源码位置',
  },
  {
    icon: 'M13 2L3 14h9l-1 8 10-12h-9l1-8z M7 14l2-2 2 2',
    title: '多格式导入',
    desc: '支持 Git 仓库一键克隆或 ZIP 包上传，自动安全检查（防 Zip-Slip、格式校验、文件大小限制）',
  },
  {
    icon: 'M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z M4 22v-7 M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z',
    title: '异步流水线',
    desc: '解析 → 切分 → 增强 → 嵌入全异步处理，批量操作优化，支持大规模项目导入',
  },
  {
    icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
    title: 'AI 问答',
    desc: '基于 RAG 架构，自然语言提问，毫秒级语义检索 + LLM 上下文回答',
  },
]

// Stats data
const stats = [
  { target: 10000, suffix: '+', label: '代码块解析', icon: 'M14.5 2h-9A1.5 1.5 0 0 0 4 3.5v17A1.5 1.5 0 0 0 5.5 22h13a1.5 1.5 0 0 0 1.5-1.5V7.5L14.5 2z M14 2v5.5a.5.5 0 0 0 .5.5H20 M8 13h8 M8 17h8 M8 9h2' },
  { target: 5, suffix: ' 种', label: '语言支持', icon: 'M3 7v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2l-2-2H5a2 2 0 0 0-2 2z' },
  { target: 1536, suffix: ' 维', label: '向量嵌入', icon: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.3 7l8.7 5 8.7-5' },
  { target: 99, suffix: '%', label: '检索精度', icon: 'M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4 12 14.01l-3-3' },
]

// Animated counters
const counters = ref(stats.map(() => 0))

function animateCounters() {
  stats.forEach((stat, index) => {
    const duration = 2000
    const step = stat.target / (duration / 16)
    let current = 0

    function update() {
      current += step
      if (current >= stat.target) {
        counters.value[index] = stat.target
        return
      }
      counters.value[index] = Math.floor(current)
      requestAnimationFrame(update)
    }
    update()
  })
}

onMounted(() => {
  // Animate counters when stats section becomes visible
  const statsSection = root.value?.querySelector('.stats-section')
  if (!statsSection) return

  let hasAnimated = false
  const statsObserver = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && !hasAnimated) {
        hasAnimated = true
        animateCounters()
        statsObserver.disconnect()
      }
    },
    { threshold: 0.3 },
  )
  statsObserver.observe(statsSection)
})
</script>

<template>
  <div ref="root">
    <!-- Particle background -->
    <ParticleCanvas />

    <!-- ==================== HERO SECTION ==================== -->
    <section class="hero-section">
      <!-- Floating tech icons -->
      <div class="floating-icons">
        <span
          v-for="icon in floatingIcons"
          :key="icon.cls"
          class="float-icon"
          :class="icon.cls"
        >{{ icon.label }}</span>
      </div>

      <div class="hero-content">
        <div class="hero-badge reveal">
          <span class="badge-dot"></span>
          AI-Powered · RAG 代码智能检索平台
        </div>

        <h1 class="hero-title reveal">
          用 <span class="gradient-text">AI</span> 读懂你的<br />
          每一行代码
        </h1>

        <p class="hero-sub reveal">
          上传项目 · 自动解析代码结构 · 构建语义向量索引<br />
          像对话一样探索和理解你的代码库
        </p>

        <div class="hero-actions reveal">
          <RouterLink to="/upload" class="cta-btn cta-primary">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3v13.5M7 11l5 5.5 5-5.5M20 21H4" />
            </svg>
            立即开始
          </RouterLink>
          <RouterLink to="/projects" class="cta-btn cta-secondary">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="3" width="7" height="7" rx="1" />
              <rect x="3" y="14" width="7" height="7" rx="1" />
              <rect x="14" y="14" width="7" height="7" rx="1" />
            </svg>
            浏览项目
          </RouterLink>
        </div>

        <div class="hero-scroll reveal" @click="scrollToPipeline">
          <span>向下探索</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19" />
            <polyline points="19 12 12 19 5 12" />
          </svg>
        </div>
      </div>
    </section>

    <!-- ==================== PIPELINE SECTION ==================== -->
    <section ref="pipelineRef" class="pipeline-section">
      <div class="section-container">
        <div class="section-header reveal">
          <span class="section-eyebrow">How It Works</span>
          <h2 class="section-title">四步构建代码知识库</h2>
          <p class="section-desc">从源码到智能问答，全自动化流水线</p>
        </div>

        <div class="pipeline-track">
          <template v-for="(step, i) in pipelineSteps" :key="step.num">
            <div class="pipeline-step reveal" :style="{ transitionDelay: `${i * 0.1}s` }">
              <div class="step-number">{{ step.num }}</div>
              <div class="step-card">
                <div class="step-icon-wrap">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path :d="step.icon" />
                  </svg>
                </div>
                <h3 class="step-title">{{ step.title }}</h3>
                <p class="step-desc">{{ step.desc }}</p>
              </div>
            </div>
            <!-- Connector -->
            <div v-if="i < pipelineSteps.length - 1" class="pipeline-connector">
              <span class="connector-line"></span>
              <svg class="connector-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </div>
          </template>
        </div>
      </div>
    </section>

    <!-- ==================== FEATURES SECTION ==================== -->
    <section class="features-section">
      <div class="section-container">
        <div class="section-header reveal">
          <span class="section-eyebrow">Features</span>
          <h2 class="section-title">为开发者打造的智能分析引擎</h2>
        </div>

        <div class="features-grid">
          <div
            v-for="(feature, i) in features"
            :key="feature.title"
            class="feature-card reveal"
            :style="{ transitionDelay: `${i * 0.08}s` }"
          >
            <div class="feature-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path :d="feature.icon" />
              </svg>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== STATS SECTION ==================== -->
    <section class="stats-section">
      <div class="section-container">
        <div class="stats-grid">
          <div v-for="(stat, i) in stats" :key="stat.label" class="stat-item reveal" :style="{ transitionDelay: `${i * 0.1}s` }">
            <div class="stat-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path :d="stat.icon" />
              </svg>
            </div>
            <div class="stat-value">
              <span class="stat-number">{{ counters[i] }}</span>
              <span class="stat-suffix">{{ stat.suffix }}</span>
            </div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== CTA SECTION ==================== -->
    <section class="cta-section">
      <div class="section-container">
        <div class="cta-card reveal">
          <h2 class="cta-title">准备好探索你的代码了吗？</h2>
          <p class="cta-desc">拖入一个项目，让 AI 为你解读每一行代码的含义。</p>
          <div class="cta-buttons">
            <RouterLink to="/upload" class="cta-btn cta-primary">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 3v13.5M7 11l5 5.5 5-5.5M20 21H4" />
              </svg>
              上传项目
            </RouterLink>
            <RouterLink to="/projects" class="cta-btn cta-outline">
              查看已有项目
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </RouterLink>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== FOOTER ==================== -->
    <footer class="home-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="footer-logo">Code Analyzer</span>
          <span class="footer-tagline">Built with Vue + Ant Design</span>
        </div>
        <div class="footer-links">
          <a href="https://github.com/Elopez4ever/code-analyzer" target="_blank" rel="noreferrer">GitHub</a>
          <RouterLink to="/upload">上传</RouterLink>
          <RouterLink to="/projects">项目</RouterLink>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ==================== HERO SECTION ==================== */
.hero-section {
  z-index: 1;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 120px 32px 80px;
  display: flex;
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #0f0a1e 0%, #1a1033 40%, #0d0d1a 100%);
}

.hero-content {
  z-index: 2;
  text-align: center;
  max-width: 800px;
  position: relative;
}

.hero-badge {
  color: var(--primary);
  backdrop-filter: blur(8px);
  background: rgba(99, 102, 241, 0.12);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 999px;
  align-items: center;
  gap: 8px;
  margin-bottom: 32px;
  padding: 7px 18px;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
}

.badge-dot {
  background: var(--primary);
  border-radius: 50%;
  width: 7px;
  height: 7px;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}

.hero-title {
  color: #ffffff;
  letter-spacing: -0.03em;
  margin: 0 0 24px;
  font-size: clamp(42px, 7vw, 80px);
  font-weight: 800;
  line-height: 1.08;
}

.hero-title .gradient-text {
  background: var(--primary-gradient);
  background-size: 200% 100%;
  -webkit-text-fill-color: transparent;
  -webkit-background-clip: text;
  background-clip: text;
  animation: gradientShift 4s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0%; }
  50% { background-position: 100%; }
}

.hero-sub {
  color: rgba(255, 255, 255, 0.7);
  max-width: 580px;
  margin: 0 auto 40px;
  font-size: clamp(16px, 2.2vw, 20px);
  line-height: 1.7;
}

.hero-actions {
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  display: flex;
}

.cta-btn {
  cursor: pointer;
  border: none;
  border-radius: 14px;
  align-items: center;
  gap: 10px;
  padding: 15px 32px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.25s;
  display: inline-flex;
  text-decoration: none;
}

.cta-primary {
  background: var(--primary-gradient);
  color: #fff;
  box-shadow: 0 6px 28px rgba(99, 102, 241, 0.45);
}

.cta-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 36px rgba(99, 102, 241, 0.55);
}

.cta-secondary {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.cta-secondary:hover {
  border-color: rgba(139, 92, 246, 0.6);
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
}

.cta-outline {
  color: #fff;
  box-shadow: none;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.cta-outline:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
}

.hero-scroll {
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  opacity: 0.6;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-top: 64px;
  font-size: 12px;
  transition: opacity 0.3s;
  display: flex;
}

.hero-scroll:hover {
  opacity: 0.9;
}

.hero-scroll svg {
  animation: bounceDown 2s ease-in-out infinite;
}

@keyframes bounceDown {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(6px); }
}

/* ==================== FLOATING ICONS ==================== */
.floating-icons {
  pointer-events: none;
  z-index: 1;
  position: absolute;
  inset: 0;
}

.float-icon {
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  opacity: 0.75;
  border-radius: 10px;
  padding: 8px 18px;
  font-family: 'Fira Code', 'Cascadia Code', monospace;
  font-size: 13px;
  font-weight: 600;
  animation: floatIcon 6s ease-in-out infinite;
  position: absolute;
}

.fi-java  { animation-delay: 0s;    top: 18%; left: 8%; }
.fi-py    { animation-delay: 1.2s;  top: 22%; right: 10%; }
.fi-vue   { animation-delay: 0.8s;  top: 55%; left: 6%; }
.fi-pg    { animation-delay: 2s;    bottom: 25%; right: 5%; }
.fi-ai    { animation-delay: 1.6s;  top: 48%; right: 15%; }
.fi-git   { animation-delay: 0.4s;  bottom: 35%; left: 12%; }
.fi-dock  { animation-delay: 2.4s;  bottom: 18%; right: 12%; }

@keyframes floatIcon {
  0%, 100% { transform: translateY(0) rotate(0); }
  25% { transform: translateY(-12px) rotate(1deg); }
  50% { transform: translateY(-4px) rotate(-0.5deg); }
  75% { transform: translateY(-16px) rotate(0.5deg); }
}

/* ==================== SHARED SECTION STYLES ==================== */
.section-container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 32px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-eyebrow {
  text-transform: uppercase;
  letter-spacing: 2px;
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
}

.section-title {
  color: var(--text);
  letter-spacing: -0.02em;
  margin: 12px 0 8px;
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 700;
}

.section-desc {
  color: var(--muted);
  max-width: 500px;
  margin: 0 auto;
  font-size: 16px;
}

/* ==================== PIPELINE SECTION ==================== */
.pipeline-section {
  z-index: 1;
  background: var(--surface-muted);
  padding: 100px 32px;
  position: relative;
}

.pipeline-track {
  flex-wrap: wrap;
  justify-content: center;
  align-items: flex-start;
  gap: 0;
  display: flex;
}

.pipeline-step {
  text-align: center;
  flex-direction: column;
  flex: 0 0 220px;
  align-items: center;
  display: flex;
}

.step-number {
  background: var(--primary-gradient);
  -webkit-text-fill-color: transparent;
  opacity: 0.8;
  -webkit-background-clip: text;
  background-clip: text;
  margin-bottom: 16px;
  font-family: 'Fira Code', 'Cascadia Code', monospace;
  font-size: 64px;
  font-weight: 800;
  line-height: 1;
}

.step-card {
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  border-radius: 16px;
  width: 100%;
  padding: 28px 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.step-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-6px);
}

.step-icon-wrap {
  width: 56px;
  height: 56px;
  color: var(--primary);
  background: rgba(99, 102, 241, 0.1);
  border-radius: 14px;
  place-items: center;
  margin: 0 auto 16px;
  transition: all 0.3s;
  display: grid;
}

.step-card:hover .step-icon-wrap {
  background: var(--primary-gradient);
  color: #fff;
}

.step-title {
  color: var(--text);
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 700;
}

.step-desc {
  color: var(--muted);
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
}

.pipeline-connector {
  flex-shrink: 0;
  align-items: center;
  margin-top: 36px;
  padding: 0 4px;
  display: flex;
}

.connector-line {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 2px;
  width: 40px;
  height: 2px;
}

.connector-arrow {
  color: var(--primary);
  opacity: 0.5;
}

/* ==================== FEATURES SECTION ==================== */
.features-section {
  z-index: 1;
  background: var(--bg);
  padding: 100px 32px;
  position: relative;
}

.features-grid {
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  display: grid;
}

.feature-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 32px 24px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  background: var(--primary-gradient);
  transform-origin: 0;
  height: 2px;
  transition: transform 0.4s;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  transform: scaleX(0);
}

.feature-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-4px);
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-icon {
  width: 48px;
  height: 48px;
  color: var(--primary);
  background: rgba(99, 102, 241, 0.1);
  border-radius: 12px;
  place-items: center;
  margin-bottom: 20px;
  transition: all 0.3s;
  display: grid;
}

.feature-card:hover .feature-icon {
  background: var(--primary-gradient);
  color: #fff;
  transform: scale(1.05);
}

.feature-title {
  color: var(--text);
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 700;
}

.feature-desc {
  color: var(--muted);
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
}

/* ==================== STATS SECTION ==================== */
.stats-section {
  z-index: 1;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.06) 0%, rgba(139, 92, 246, 0.06) 100%);
  padding: 80px 32px;
  position: relative;
}

.stats-grid {
  text-align: center;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  display: grid;
}

.stat-item {
  padding: 20px;
}

.stat-icon {
  color: var(--primary);
  margin-bottom: 16px;
  display: inline-flex;
}

.stat-value {
  justify-content: center;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
  display: flex;
}

.stat-number {
  color: var(--text);
  letter-spacing: -0.02em;
  font-family: 'Fira Code', 'Cascadia Code', monospace;
  font-size: 42px;
  font-weight: 800;
  line-height: 1;
}

.stat-suffix {
  color: var(--primary);
  font-size: 16px;
  font-weight: 600;
}

.stat-label {
  color: var(--muted);
  font-size: 14px;
  font-weight: 500;
}

/* ==================== CTA SECTION ==================== */
.cta-section {
  z-index: 1;
  background: var(--bg);
  padding: 100px 32px;
  position: relative;
}

.cta-card {
  text-align: center;
  color: #fff;
  backdrop-filter: blur(10px);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.95) 0%, rgba(139, 92, 246, 0.95) 100%);
  border-radius: 24px;
  padding: 60px 40px;
  box-shadow: 0 20px 60px rgba(99, 102, 241, 0.4);
}

.cta-title {
  margin: 0 0 12px;
  font-size: clamp(26px, 3.5vw, 38px);
  font-weight: 700;
}

.cta-desc {
  opacity: 0.85;
  margin: 0 0 32px;
  font-size: 16px;
  line-height: 1.6;
}

.cta-buttons {
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  display: flex;
}

/* ==================== FOOTER ==================== */
.home-footer {
  z-index: 1;
  border-top: 1px solid var(--border);
  background: var(--surface-muted);
  padding: 40px 32px;
  position: relative;
}

.footer-content {
  color: var(--muted);
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  max-width: 1120px;
  margin: 0 auto;
  font-size: 13px;
  display: flex;
}

.footer-brand {
  flex-direction: column;
  gap: 2px;
  display: flex;
}

.footer-logo {
  color: var(--text);
  font-size: 15px;
  font-weight: 700;
}

.footer-tagline {
  font-size: 12px;
}

.footer-links {
  gap: 20px;
  display: flex;
}

.footer-links a {
  color: var(--muted);
  transition: color 0.2s;
  text-decoration: none;
}

.footer-links a:hover {
  color: var(--primary);
}

/* ==================== REVEAL ANIMATION ==================== */
.reveal {
  opacity: 0;
  transition: opacity 0.7s cubic-bezier(0.16, 1, 0.3, 1), transform 0.7s cubic-bezier(0.16, 1, 0.3, 1);
  transform: translateY(28px);
}

.reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* ==================== RESPONSIVE ==================== */
@media (max-width: 900px) {
  .pipeline-track {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .pipeline-step {
    flex: none;
    width: 100%;
    max-width: 360px;
  }

  .pipeline-connector {
    margin-top: 4px;
    transform: rotate(90deg);
  }

  .features-grid {
    grid-template-columns: 1fr;
    max-width: 480px;
    margin: 0 auto;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .floating-icons {
    display: none;
  }
}

@media (max-width: 640px) {
  .hero-section {
    padding: 80px 20px 60px;
  }

  .section-container {
    padding: 0 20px;
  }

  .pipeline-section,
  .features-section,
  .stats-section,
  .cta-section {
    padding: 60px 20px;
  }

  .cta-card {
    padding: 40px 24px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-number {
    font-size: 34px;
  }

  .footer-content {
    text-align: center;
    flex-direction: column;
  }
}
</style>
