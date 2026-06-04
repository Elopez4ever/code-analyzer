<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useProjects } from '../store/projects'
import StatusBadge from '../components/StatusBadge.vue'
import { formatDateTime } from '../utils/format'

const route = useRoute()
const { getProjectById } = useProjects()
const messagesRef = ref(null)
const input = ref('')
const messages = ref([])
const selectedModel = ref('default')
const modelOptions = [
  { value: 'default', label: '默认模型' },
  { value: 'quality', label: '高质量模型' },
  { value: 'fast', label: '快速模型' },
]

const projectId = computed(() => route.params.projectId)
const project = computed(() => getProjectById(projectId.value))
const isReady = computed(() => project.value?.status === 'READY')
const modelLabel = computed(
  () => modelOptions.find((option) => option.value === selectedModel.value)?.label || '默认模型',
)

const storageKey = computed(() => `code-analyzer-chat-${projectId.value}`)
const modelStorageKey = computed(() => `code-analyzer-model-${projectId.value}`)

const loadMessages = () => {
  try {
    const stored = localStorage.getItem(storageKey.value)
    messages.value = stored ? JSON.parse(stored) : []
  } catch {
    messages.value = []
  }
}

const persistMessages = () => {
  localStorage.setItem(storageKey.value, JSON.stringify(messages.value))
}

const loadModel = () => {
  const stored = localStorage.getItem(modelStorageKey.value)
  selectedModel.value = stored || 'default'
}

const persistModel = () => {
  localStorage.setItem(modelStorageKey.value, selectedModel.value)
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const sendMessage = async () => {
  const content = input.value.trim()
  if (!content || !project.value) return
  const now = new Date().toISOString()
  messages.value.push({ role: 'user', content, createdAt: now })
  messages.value.push({
    role: 'assistant',
    content: `已收到问题（${modelLabel.value}），后端知识检索接入后将返回详细答案。`,
    createdAt: new Date().toISOString(),
  })
  input.value = ''
  persistMessages()
  await scrollToBottom()
}

watch(storageKey, () => {
  loadMessages()
  loadModel()
  scrollToBottom()
})

watch(selectedModel, () => {
  persistModel()
})

onMounted(() => {
  loadMessages()
  loadModel()
  scrollToBottom()
})
</script>

<template>
  <div class="page">
    <div v-if="!project" class="card empty-state">
      <h3>项目不存在</h3>
      <p class="muted">请返回项目列表重新选择。</p>
      <RouterLink to="/projects" class="btn btn-primary">返回项目列表</RouterLink>
    </div>

    <div v-else class="chat-layout chatgpt-layout">
      <aside class="card chat-sidebar">
        <h3 class="chat-title">{{ project.name }}</h3>
        <p class="muted">{{ project.source }}</p>
        <div class="chat-meta">
          <div>
            <div class="meta-label">来源</div>
            <div class="meta-value">{{ project.sourceType }}</div>
          </div>
          <div>
            <div class="meta-label">状态</div>
            <StatusBadge :status="project.status" />
          </div>
          <div>
            <div class="meta-label">更新时间</div>
            <div class="meta-value">{{ formatDateTime(project.updatedAt) }}</div>
          </div>
        </div>
        <div class="chat-tips">
          <div class="meta-label">建议提问</div>
          <ul>
            <li>这个项目的核心模块有哪些？</li>
            <li>找出主要的调用链路。</li>
            <li>说明数据流转过程。</li>
          </ul>
        </div>
      </aside>

      <section class="chat-panel chatgpt-panel">
        <header class="chat-panel-header chatgpt-header">
          <div>
            <h3>项目对话</h3>
            <p class="muted">聚焦当前项目的结构与代码细节。</p>
          </div>
          <div class="chat-header-actions">
            <label class="select-field">
              <span class="select-label">模型</span>
              <select v-model="selectedModel" class="select">
                <option v-for="option in modelOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
            <RouterLink to="/projects" class="btn btn-ghost">返回列表</RouterLink>
          </div>
        </header>

        <div v-if="!isReady" class="banner warning">
          <strong>解析未完成：</strong>
          <span>项目解析完成后即可开始问答。</span>
        </div>

        <div ref="messagesRef" class="chat-messages chatgpt-messages">
          <div v-if="messages.length === 0" class="chat-empty chatgpt-empty">
            有什么想问的吗？
            <div class="chatgpt-empty-subtitle">从项目结构、模块职责或调用链开始提问。</div>
          </div>
          <div v-for="(message, index) in messages" :key="index" class="chat-row" :class="message.role">
            <div class="chat-row-inner">
              <div class="chatgpt-avatar">{{ message.role === 'user' ? '你' : 'AI' }}</div>
              <div class="chat-row-content">
                <div class="chat-row-meta">
                  <span class="chat-name">{{ message.role === 'user' ? '你' : 'AI' }}</span>
                  <span class="chat-time">{{ formatDateTime(message.createdAt) }}</span>
                </div>
                <div class="chat-text">{{ message.content }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-composer">
          <div class="chat-composer-inner">
            <textarea
              v-model="input"
              class="chat-composer-input"
              placeholder="输入问题，例如：这个项目的入口文件在哪里？"
              :disabled="!isReady"
            ></textarea>
            <button class="btn btn-primary" :disabled="!input.trim() || !isReady" @click="sendMessage">
              发送
            </button>
          </div>
          <div class="chat-hint">{{ isReady ? 'Enter 发送' : '解析完成后可发送问题' }}</div>
        </div>
      </section>
    </div>
  </div>
</template>
