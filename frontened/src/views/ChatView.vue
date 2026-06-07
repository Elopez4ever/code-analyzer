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
const sidebarOpen = ref(true)
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

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const clearMessages = () => {
  messages.value = []
  persistMessages()
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
  <!-- NOT using .page class — this component manages its own full-bleed layout -->
  <div class="chat-page-root">
    <div v-if="!project" class="chat-not-found">
      <h3>项目不存在</h3>
      <p>请返回项目列表重新选择。</p>
      <RouterLink to="/projects" class="btn btn-primary">返回项目列表</RouterLink>
    </div>

    <template v-else>
      <!-- Sidebar -->
      <aside class="chat-sidebar" :class="{ 'is-collapsed': !sidebarOpen }">
        <div class="sidebar-head">
          <button class="sidebar-btn" @click="sidebarOpen = false" title="收起侧边栏">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <line x1="9" y1="3" x2="9" y2="21" />
            </svg>
          </button>
          <button class="sidebar-btn" @click="clearMessages" title="清空对话">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
          </button>
        </div>

        <div class="sidebar-body">
          <div class="sidebar-project">
            <div class="sidebar-project-name">{{ project.name }}</div>
            <div class="sidebar-project-src">{{ project.source }}</div>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-section-title">项目信息</div>
            <div class="sidebar-info-row">
              <span>来源</span>
              <span class="sidebar-info-val">{{ project.sourceType }}</span>
            </div>
            <div class="sidebar-info-row">
              <span>状态</span>
              <StatusBadge :status="project.status" />
            </div>
            <div class="sidebar-info-row">
              <span>更新</span>
              <span class="sidebar-info-val">{{ formatDateTime(project.updatedAt) }}</span>
            </div>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-section-title">建议提问</div>
            <button class="sidebar-suggest" @click="input = '这个项目的核心模块有哪些？'">
              这个项目的核心模块有哪些？
            </button>
            <button class="sidebar-suggest" @click="input = '找出主要的调用链路。'">
              找出主要的调用链路。
            </button>
            <button class="sidebar-suggest" @click="input = '说明数据流转过程。'">
              说明数据流转过程。
            </button>
          </div>
        </div>

        <div class="sidebar-foot">
          <RouterLink to="/projects" class="sidebar-back">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            返回项目列表
          </RouterLink>
        </div>
      </aside>

      <!-- Main chat panel -->
      <div class="chat-main">
        <!-- Top bar -->
        <header class="chat-topbar">
          <button v-if="!sidebarOpen" class="sidebar-btn" @click="sidebarOpen = true" title="展开侧边栏">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <line x1="9" y1="3" x2="9" y2="21" />
            </svg>
          </button>
          <div class="topbar-model">
            <select v-model="selectedModel">
              <option v-for="opt in modelOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>
        </header>

        <!-- Warning -->
        <div v-if="!isReady" class="chat-warning">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          项目解析完成后即可开始问答
        </div>

        <!-- Messages area -->
        <div ref="messagesRef" class="chat-messages-area">
          <div v-if="messages.length === 0" class="chat-empty-state">
            <div class="chat-empty-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
            </div>
            <div class="chat-empty-title">有什么想问的吗？</div>
            <div class="chat-empty-sub">从项目结构、模块职责或调用链开始提问</div>
          </div>

          <template v-else>
            <div
              v-for="(msg, i) in messages"
              :key="i"
              class="msg-row"
              :class="msg.role"
            >
              <div class="msg-row-inner">
                <div class="msg-avatar" :class="msg.role">
                  {{ msg.role === 'user' ? '你' : 'AI' }}
                </div>
                <div class="msg-content">
                  <div class="msg-sender">{{ msg.role === 'user' ? '你' : 'Code Analyzer' }}</div>
                  <div class="msg-text">{{ msg.content }}</div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- Input area -->
        <div class="chat-input-area">
          <div class="chat-input-box">
            <textarea
              v-model="input"
              class="chat-textarea"
              :placeholder="isReady ? '给 Code Analyzer 发送消息' : '项目解析中，请稍候…'"
              :disabled="!isReady"
              rows="1"
              @keydown="handleKeydown"
              @input="e => { e.target.style.height = 'auto'; e.target.style.height = Math.min(e.target.scrollHeight, 180) + 'px' }"
            ></textarea>
            <button
              class="chat-send-btn"
              :disabled="!input.trim() || !isReady"
              @click="sendMessage"
              title="发送"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="12" y1="19" x2="12" y2="5" />
                <polyline points="5 12 12 5 19 12" />
              </svg>
            </button>
          </div>
          <div class="chat-input-hint">Code Analyzer 可能会犯错，请核查重要信息。</div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/*
 * This page fills the entire space provided by .app-main.
 * We override the parent .page constraints via the root element.
 */
.chat-page-root {
  display: flex;
  height: auto;
  min-height: calc(100vh - 148px);
  width: 100%;
  background: var(--bg);
  padding: 0;
  box-sizing: border-box;
  overflow: auto;
}

.chat-page-root .chat-not-found {
  min-height: calc(100vh - 148px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 12px;
  color: var(--muted);
}

/* ============ SIDEBAR ============ */
.chat-sidebar {
  width: 260px;
  min-width: 200px;
  max-width: 320px;
  flex-shrink: 1;
  flex-grow: 0;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border-right: 1px solid var(--border);
  overflow: hidden;
  transition: width 0.25s ease, opacity 0.25s ease;
}

.chat-sidebar.is-collapsed {
  width: 0;
  opacity: 0;
  pointer-events: none;
  border-right: none;
}

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  flex-shrink: 0;
}

.sidebar-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: var(--text);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: background 0.15s;
}

.sidebar-btn:hover {
  background: var(--surface-muted);
}

.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sidebar-project {
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border);
}

.sidebar-project-name {
  font-weight: 700;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-project-src {
  font-size: 12px;
  color: var(--muted);
  margin-top: 4px;
  word-break: break-all;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sidebar-section-title {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--muted);
  font-weight: 600;
  padding-left: 4px;
  margin-bottom: 2px;
}

.sidebar-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 6px;
  font-size: 13px;
  color: var(--muted);
  border-radius: 6px;
}

.sidebar-info-val {
  font-weight: 600;
  color: var(--text);
}

.sidebar-suggest {
  text-align: left;
  border: none;
  background: var(--surface-muted);
  border-radius: 8px;
  padding: 9px 11px;
  font-size: 13px;
  color: var(--text);
  cursor: pointer;
  transition: background 0.15s;
  line-height: 1.4;
}

.sidebar-suggest:hover {
  background: var(--border);
}

.sidebar-foot {
  padding: 10px 12px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

.sidebar-back {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
  padding: 8px 6px;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}

.sidebar-back:hover {
  background: var(--surface-muted);
  color: var(--text);
}

/* ============ MAIN ============ */
.chat-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

/* Top bar */
.chat-topbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  flex-shrink: 0;
  min-height: 48px;
}

.topbar-model {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.topbar-model select {
  appearance: none;
  border: none;
  background: transparent;
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  padding: 6px 22px 6px 6px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.15s;
}

.topbar-model select:hover {
  background: var(--surface-muted);
}

.topbar-model svg {
  position: absolute;
  right: 4px;
  pointer-events: none;
  color: var(--muted);
}

/* Warning */
.chat-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
  padding: 8px 16px;
  font-size: 13px;
  color: var(--warning);
  background: rgba(245, 158, 11, 0.08);
  flex-shrink: 0;
}

/* Messages */
.chat-messages-area {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  min-height: 200px;
}

.chat-empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 40px 20px;
}

.chat-empty-icon {
  color: var(--muted);
  opacity: 0.4;
  margin-bottom: 8px;
}

.chat-empty-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
}

.chat-empty-sub {
  font-size: 14px;
  color: var(--muted);
}

.msg-row {
  padding: 12px 0;
}

.msg-row-inner {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
  width: 100%;
}

.msg-row.user .msg-row-inner {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  color: white;
}

.msg-avatar.user {
  background: #64748b;
}

.msg-avatar.assistant {
  background: var(--primary-gradient);
}

.msg-content {
  flex: 1;
  min-width: 0;
  max-width: 75%;
}

.msg-row.assistant .msg-content {
  background: rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.15);
  border-radius: 20px;
  padding: 14px 18px;
}

.msg-row.user .msg-content {
  background: var(--primary-gradient);
  border-radius: 20px;
  padding: 14px 18px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.3);
}

.msg-sender {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 6px;
}

.msg-row.user .msg-sender {
  display: none;
}

.msg-row.assistant .msg-sender {
  color: var(--primary);
}

.msg-text {
  font-size: 15px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-row.user .msg-text {
  color: white;
}

.msg-row.assistant .msg-text {
  color: var(--text);
}

/* Input */
.chat-input-area {
  flex-shrink: 0;
  padding: 12px 24px 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.chat-input-box {
  max-width: 720px;
  width: 100%;
  display: flex;
  align-items: flex-end;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 22px;
  padding: 8px 10px 8px 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.chat-input-box:focus-within {
  border-color: var(--primary);
  box-shadow: 0 2px 16px rgba(91, 108, 255, 0.1);
}

.chat-textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  min-height: 24px;
  max-height: 180px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text);
  padding: 5px 0;
  overflow-y: auto;
}

.chat-textarea:focus {
  outline: none;
}

.chat-textarea::placeholder {
  color: var(--muted);
}

.chat-send-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: var(--text);
  color: var(--bg);
  display: grid;
  place-items: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.15s, transform 0.15s;
}

.chat-send-btn:hover:not(:disabled) {
  transform: scale(1.06);
}

.chat-send-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

.chat-input-hint {
  font-size: 12px;
  color: var(--muted);
  text-align: center;
}

/* ============ RESPONSIVE ============ */
@media (max-width: 768px) {
  .chat-sidebar {
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 50;
    box-shadow: var(--shadow-lg);
  }

  .chat-sidebar.is-collapsed {
    box-shadow: none;
  }

  .msg-row-inner {
    padding: 0 14px;
  }

  .chat-input-area {
    padding: 10px 14px 14px;
  }
}
</style>