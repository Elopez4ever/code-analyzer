<script setup>
import { computed, onMounted, ref } from 'vue'

const theme = ref('light')
const isDark = computed(() => theme.value === 'dark')

const applyTheme = (value) => {
  theme.value = value
  document.documentElement.dataset.theme = value
  localStorage.setItem('theme', value)
}

const startThemeTransition = () => {
  document.documentElement.classList.add('theme-transition')
  window.setTimeout(() => {
    document.documentElement.classList.remove('theme-transition')
  }, 350)
}

const toggleTheme = () => {
  startThemeTransition()
  applyTheme(isDark.value ? 'light' : 'dark')
}

onMounted(() => {
  const saved = localStorage.getItem('theme')
  const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)')?.matches
  applyTheme(saved || (prefersDark ? 'dark' : 'light'))
})
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <div class="brand-mark">CA</div>
        <div>
          <div class="brand-title">Code Analyzer</div>
          <div class="brand-subtitle">代码知识库与问答平台</div>
        </div>
      </div>
      <nav class="nav">
        <RouterLink to="/" class="nav-link">首页</RouterLink>
        <RouterLink to="/upload" class="nav-link">上传项目</RouterLink>
        <RouterLink to="/projects" class="nav-link">项目列表</RouterLink>
      </nav>
      <div class="header-actions">
        <button class="icon-btn" type="button" :aria-label="isDark ? '切换浅色' : '切换深色'" @click="toggleTheme">
          <svg v-if="isDark" viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M12 3.5a.75.75 0 0 1 .75.75V6a.75.75 0 0 1-1.5 0V4.25a.75.75 0 0 1 .75-.75Zm0 14.5a.75.75 0 0 1 .75.75v1.75a.75.75 0 0 1-1.5 0V19.75a.75.75 0 0 1 .75-.75Zm8.5-6a.75.75 0 0 1-.75.75H18a.75.75 0 0 1 0-1.5h1.75a.75.75 0 0 1 .75.75ZM6 12a.75.75 0 0 1-.75.75H3.5a.75.75 0 0 1 0-1.5h1.75A.75.75 0 0 1 6 12Zm11.03-5.03a.75.75 0 0 1 1.06 0l1.24 1.24a.75.75 0 0 1-1.06 1.06l-1.24-1.24a.75.75 0 0 1 0-1.06ZM5.67 17.23a.75.75 0 0 1 1.06 0l1.24 1.24a.75.75 0 1 1-1.06 1.06l-1.24-1.24a.75.75 0 0 1 0-1.06ZM17.09 16.17a.75.75 0 0 1 1.06 0l1.24 1.24a.75.75 0 1 1-1.06 1.06l-1.24-1.24a.75.75 0 0 1 0-1.06ZM5.67 6.77a.75.75 0 0 1 1.06 0l1.24 1.24a.75.75 0 0 1-1.06 1.06L5.67 7.83a.75.75 0 0 1 0-1.06ZM12 8a4 4 0 1 1 0 8 4 4 0 0 1 0-8Z"
              fill="currentColor"
            />
          </svg>
          <svg v-else viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M12.46 3.5a.75.75 0 0 1 .74.75v.47a7.28 7.28 0 0 0 6.2 7.2.75.75 0 0 1 .64.86 7.75 7.75 0 1 1-7.58-9.78Z"
              fill="currentColor"
            />
          </svg>
        </button>
        <a
          class="icon-btn"
          href="https://github.com/Elopez4ever/code-analyzer"
          target="_blank"
          rel="noreferrer"
          aria-label="GitHub"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="currentColor"
              d="M12 2C6.48 2 2 6.58 2 12.25c0 4.5 2.87 8.31 6.84 9.66.5.1.68-.22.68-.48 0-.24-.01-.88-.01-1.73-2.78.62-3.37-1.37-3.37-1.37-.45-1.18-1.1-1.5-1.1-1.5-.9-.64.07-.63.07-.63 1 .07 1.53 1.06 1.53 1.06.9 1.58 2.36 1.12 2.94.86.09-.67.35-1.12.63-1.38-2.22-.26-4.56-1.14-4.56-5.07 0-1.12.39-2.03 1.03-2.75-.1-.26-.45-1.33.1-2.76 0 0 .84-.27 2.75 1.05a9.3 9.3 0 0 1 5 0c1.9-1.32 2.75-1.05 2.75-1.05.55 1.43.2 2.5.1 2.76.64.72 1.03 1.63 1.03 2.75 0 3.94-2.35 4.8-4.58 5.06.36.33.68.98.68 1.98 0 1.43-.01 2.58-.01 2.93 0 .26.18.59.69.48 3.96-1.35 6.83-5.16 6.83-9.66C22 6.58 17.52 2 12 2Z"
            />
          </svg>
        </a>
      </div>
    </header>

    <main class="app-main">
      <RouterView v-slot="{ Component, route }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>

    <footer class="app-footer">
      <span>© 2026 Code Analyzer</span>
      <span>让代码结构与知识沉淀更简单</span>
    </footer>
  </div>
</template>
