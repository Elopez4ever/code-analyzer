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
  <div ref="root" class="page-home-wrapper">
    <section class="home-section home-hero">
      <div class="container">
        <div class="hero hero-centered">
          <div class="hero-badge">AI-Powered Code Analysis</div>
          <h1>
            用 <span class="gradient-text">AI</span> 读懂你的代码库
          </h1>
          <p class="hero-subtitle">
            上传项目，自动解析代码结构，构建知识索引，然后像对话一样探索你的代码。
          </p>
          <div class="hero-actions">
            <RouterLink to="/upload" class="btn btn-primary btn-lg">
              <svg class="icon" viewBox="0 0 24 24" aria-hidden="true">
                <path fill="currentColor" d="M12 3a1 1 0 0 1 1 1v9.59l2.3-2.3a1 1 0 1 1 1.4 1.42l-4 4a1 1 0 0 1-1.4 0l-4-4a1 1 0 1 1 1.4-1.42l2.3 2.3V4a1 1 0 0 1 1-1Zm-7 14a1 1 0 0 1 1 1v2h14v-2a1 1 0 1 1 2 0v3a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1Z"/>
              </svg>
              立即开始
            </RouterLink>
            <RouterLink to="/projects" class="btn btn-ghost btn-lg">查看项目</RouterLink>
          </div>
          <div class="hero-badges">
            <span class="badge-chip">Git URL</span>
            <span class="badge-chip">ZIP 上传</span>
            <span class="badge-chip">智能问答</span>
            <span class="badge-chip">向量检索</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page-home-wrapper {
  min-height: calc(100vh - 64px);
  display: flex !important;
  flex-direction: column;
}

.page-home-wrapper .home-hero {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 32px;
  background: var(--bg);
}

.home-hero .hero {
  text-align: center;
  max-width: 900px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(99, 102, 241, 0.15);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 9999px;
  font-size: 14px;
  font-weight: 500;
  color: var(--primary);
  margin-bottom: 24px;
}

.home-hero .hero h1 {
  font-size: 64px;
  line-height: 1.1;
  margin: 0 0 20px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.02em;
}

.home-hero .hero h1 .gradient-text {
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.home-hero .hero-subtitle {
  font-size: 20px;
  color: var(--muted);
  max-width: 700px;
  margin: 0 auto;
  line-height: 1.6;
}

.home-hero .hero-actions {
  margin-top: 40px;
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
}

.btn-lg {
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-lg .icon {
  width: 20px;
  height: 20px;
}

.btn-primary.btn-lg {
  background: var(--primary-gradient);
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.4);
}

.btn-primary.btn-lg:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(99, 102, 241, 0.5);
}

.btn-ghost.btn-lg {
  background: var(--primary-gradient);
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.4);
  color: #ffffff;
}

.btn-ghost.btn-lg:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(99, 102, 241, 0.5);
}

.hero-badges {
  margin-top: 48px;
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.badge-chip {
  padding: 8px 16px;
  background: rgba(99, 102, 241, 0.12);
  border: 1px solid rgba(99, 102, 241, 0.4);
  border-radius: 9999px;
  font-size: 14px;
  color: var(--primary);
  font-weight: 500;
}

@media (max-width: 1024px) {
  .home-hero .hero h1 {
    font-size: 42px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .btn-lg {
    padding: 12px 24px;
    font-size: 15px;
  }
}

@media (max-width: 640px) {
  .home-hero .hero h1 {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .btn-lg {
    width: 100%;
    justify-content: center;
  }

  .hero-badges {
    gap: 8px;
  }

  .badge-chip {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>