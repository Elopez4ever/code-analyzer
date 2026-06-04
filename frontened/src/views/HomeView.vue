<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const root = ref(null)
let observer

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
    { threshold: 0.2 },
  )

  targets.forEach((target) => observer.observe(target))
})

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect()
  }
})
</script>

<template>
  <div ref="root" class="page home">
    <section class="home-section home-hero">
      <div class="container">
        <div class="hero hero-centered">
          <h1>
            <span class="gradient-text">代码解析</span>与项目化问答平台
          </h1>
          <p class="hero-subtitle">
            上传项目后自动解析结构，项目列表清晰展示状态，准备好即可进入对话页问答。
          </p>
          <div class="hero-actions">
            <RouterLink to="/upload" class="btn btn-primary">快速开始</RouterLink>
            <RouterLink to="/projects" class="btn btn-ghost">查看项目</RouterLink>
          </div>
          <div class="hero-badges">
            <span class="badge-chip">Git URL</span>
            <span class="badge-chip">ZIP 上传</span>
            <span class="badge-chip">对话问答</span>
          </div>
        </div>
      </div>
    </section>

    <section class="home-section home-features reveal">
      <div class="container">
        <div class="feature-grid">
          <div class="card feature-card">
            <span class="feature-index">01</span>
            <h3>自动解析</h3>
            <p class="muted">结构化拆解项目代码，快速形成可检索的知识视图。</p>
          </div>
          <div class="card feature-card">
            <span class="feature-index">02</span>
            <h3>项目管理</h3>
            <p class="muted">清晰的项目列表与状态追踪，让解析进度一目了然。</p>
          </div>
          <div class="card feature-card">
            <span class="feature-index">03</span>
            <h3>对话问答</h3>
            <p class="muted">基于项目上下文进行问答，定位模块、链路更高效。</p>
          </div>
        </div>
      </div>
    </section>

    <section class="home-section home-callout reveal">
      <div class="container">
        <div class="hero-callout card hero-card-glow">
          <div>
            <div class="hero-card-title">使用指引</div>
            <p class="muted">
              1. 进入上传页面选择 Git 或 ZIP<br />
              2. 上传后自动跳转到项目列表<br />
              3. 解析就绪后点击进入对话
            </p>
          </div>
          <RouterLink to="/upload" class="btn btn-primary">立即上传</RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-hero {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  padding: 0 32px;
}

.home-hero .hero h1 {
  font-size: 56px;
  line-height: 1.2;
  margin: 0 0 16px;
}

.home-hero .hero-subtitle {
  font-size: 18px;
}

.home-hero .hero-actions {
  margin-top: 32px;
}

.home-features {
  padding: 80px 32px;
  border-top: 1px solid var(--border);
}

.home-callout {
  padding: 64px 32px 96px;
}

@media (max-width: 1024px) {
  .home-hero .hero h1 {
    font-size: 36px;
  }

  .home-features {
    padding: 56px 24px;
  }
}
</style>