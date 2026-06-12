import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },
  css: {
    preprocessorOptions: {},
  },
  build: {
    rolldownOptions: {
      onLog(level, log, defaultHandler) {
        // ant-design-vue v4 uses CSS-in-JS; suppress missing static CSS warnings
        if (log.code === 'UNRESOLVED_IMPORT' && log.message?.includes('ant-design-vue')) return
        defaultHandler(level, log)
      },
    },
  },
})