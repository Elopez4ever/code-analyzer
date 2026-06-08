<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
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
const modelDropdownOpen = ref(false)
const modelBtnRef = ref(null)

// 虚拟滚动相关
const VISIBLE_BUFFER = 50 // 可见区域外额外渲染的条数
const visibleStart = ref(0)
const isAtBottom = ref(true)
const showScrollDown = ref(false)

const modelOptions = [
  { value: 'default', label: '默认模型', desc: '平衡速度与质量' },
  { value: 'quality', label: '高质量模型', desc: '更精确的代码分析' },
  { value: 'fast', label: '快速模型', desc: '极速响应，适合简单问题' },
]

const projectId = computed(() => route.params.projectId)
const project = computed(() => getProjectById(projectId.value))
const isReady = computed(() => project.value?.status === 'READY')
const modelLabel = computed(
  () => modelOptions.find((option) => option.value === selectedModel.value)?.label || '默认模型',
)

const storageKey = computed(() => `code-analyzer-chat-${projectId.value}`)
const modelStorageKey = computed(() => `code-analyzer-model-${projectId.value}`)

// 虚拟列表：只渲染尾部消息
const visibleMessages = computed(() => {
  const total = messages.value.length
  if (total <= VISIBLE_BUFFER) {
    visibleStart.value = 0
    return messages.value
  }
  const start = Math.max(0, visibleStart.value)
  return messages.value.slice(start)
})

const hasHiddenAbove = computed(() => visibleStart.value > 0)

const loadMessages = () => {
  try {
    const stored = localStorage.getItem(storageKey.value)
    messages.value = stored ? JSON.parse(stored) : []
  } catch {
    messages.value = []
  }
  // 初始化时显示最新的消息
  resetVisibleRange()
}

const resetVisibleRange = () => {
  const total = messages.value.length
  visibleStart.value = Math.max(0, total - VISIBLE_BUFFER)
}

const loadOlderMessages = () => {
  if (visibleStart.value <= 0) return
  visibleStart.value = Math.max(0, visibleStart.value - 30)
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
    messagesRef.value.scrollTo({
      top: messagesRef.value.scrollHeight,
      behavior: 'smooth',
    })
  }
  isAtBottom.value = true
  showScrollDown.value = false
}

const handleScroll = () => {
  if (!messagesRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = messagesRef.value
  isAtBottom.value = scrollHeight - scrollTop - clientHeight < 60
  showScrollDown.value = !isAtBottom.value

  // 滚动到顶部时加载更多
  if (scrollTop < 80 && hasHiddenAbove.value) {
    const prevHeight = messagesRef.value.scrollHeight
    loadOlderMessages()
    nextTick(() => {
      // 保持滚动位置
      const newHeight = messagesRef.value.scrollHeight
      messagesRef.value.scrollTop = newHeight - prevHeight + scrollTop
    })
  }
}

const sendMessage = async () => {
  const content = input.value.trim()
  if (!content || !project.value) return
  const now = new Date().toISOString()
  messages.value.push({ role: 'user', content, createdAt: now })
  persistMessages()

  // 确保 visibleStart 跟上
  resetVisibleRange()
  await scrollToBottom()

  // 模拟 AI 回复（带打字效果）
  const replyText = `已收到问题（${modelLabel.value}），后端知识检索接入后将返回详细答案。`
  const replyMsg = { role: 'assistant', content: '', createdAt: new Date().toISOString(), typing: true }
  messages.value.push(replyMsg)
  resetVisibleRange()
  await scrollToBottom()

  // 逐字输出
  const idx = messages.value.length - 1
  for (let i = 0; i < replyText.length; i++) {
    await new Promise((r) => setTimeout(r, 18))
    messages.value[idx] = { ...messages.value[idx], content: replyText.slice(0, i + 1) }
    if (isAtBottom.value) {
      await nextTick()
      messagesRef.value?.scrollTo({ top: messagesRef.value.scrollHeight })
    }
  }
  messages.value[idx] = { ...messages.value[idx], typing: false }

  input.value = ''
  persistMessages()
  resetVisibleRange()
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
  visibleStart.value = 0
  persistMessages()
}

const selectModel = (value) => {
  selectedModel.value = value
  modelDropdownOpen.value = false
}

const handleClickOutside = (e) => {
  if (modelBtnRef.value && !modelBtnRef.value.contains(e.target)) {
    modelDropdownOpen.value = false
  }
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
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="chat-page-root">
    <div v-if="!project" class="chat-not-found">
      <h3>项目不存在</h3>
      <p>请返回项目列表重新选择。</p>
      <RouterLink to="/projects" class="btn btn-primary">返回项目列表</RouterLink>
    </div>

    <aside v-if="project" class="chat-sidebar" :class="{ 'is-collapsed': !sidebarOpen }">
      <div class="sidebar-head">
        <button class="sidebar-btn" @click="sidebarOpen = false" title="收起侧边栏">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" />
            <line x1="9" y1="3" x2="9" y2="21" />
          </svg>
        </button>
        <button class="sidebar-btn" @click="clearMessages" title="清空对话">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
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

    <div v-if="project" class="chat-main">
      <header class="chat-topbar">
        <button v-if="!sidebarOpen" class="sidebar-btn" @click="sidebarOpen = true" title="展开侧边栏">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" />
            <line x1="9" y1="3" x2="9" y2="21" />
          </svg>
        </button>

        <!-- 美化后的模型选择器 -->
        <div ref="modelBtnRef" class="model-selector">
          <button class="model-trigger" @click="modelDropdownOpen = !modelDropdownOpen">
            <div class="model-trigger-dot"></div>
            <span class="model-trigger-label">{{ modelLabel }}</span>
            <svg class="model-trigger-chevron" :class="{ open: modelDropdownOpen }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </button>

          <Transition name="dropdown">
            <div v-if="modelDropdownOpen" class="model-dropdown">
              <div class="model-dropdown-title">选择模型</div>
              <button
                v-for="opt in modelOptions"
                :key="opt.value"
                class="model-option"
                :class="{ active: selectedModel === opt.value }"
                @click="selectModel(opt.value)"
              >
                <div class="model-option-radio">
                  <div v-if="selectedModel === opt.value" class="model-option-radio-dot"></div>
                </div>
                <div class="model-option-info">
                  <div class="model-option-name">{{ opt.label }}</div>
                  <div class="model-option-desc">{{ opt.desc }}</div>
                </div>
                <svg v-if="selectedModel === opt.value" class="model-option-check" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </button>
            </div>
          </Transition>
        </div>
      </header>

      <div v-if="!isReady" class="chat-warning">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
        项目解析完成后即可开始问答
      </div>

      <div ref="messagesRef" class="chat-messages-area" @scroll="handleScroll">
        <!-- 加载更多提示 -->
        <div v-if="hasHiddenAbove" class="load-more-hint">
          <button class="load-more-btn" @click="loadOlderMessages">加载更早的消息</button>
        </div>

        <div v-if="messages.length === 0" class="chat-empty-state">
          <div class="chat-empty-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
          </div>
          <div class="chat-empty-title">有什么想问的吗？</div>
          <div class="chat-empty-sub">从项目结构、模块职责或调用链开始提问</div>
        </div>

        <TransitionGroup name="msg" tag="div" class="msg-list">
          <div
            v-for="(msg, i) in visibleMessages"
            :key="`${visibleStart + i}-${msg.createdAt}`"
            class="msg-row"
            :class="[msg.role, { typing: msg.typing }]"
          >
            <div class="msg-row-inner">
              <div class="msg-avatar" :class="msg.role">
                {{ msg.role === 'user' ? '你' : 'AI' }}
              </div>
              <div class="msg-content">
                <div class="msg-sender">{{ msg.role === 'user' ? '你' : 'Code Analyzer' }}</div>
                <div class="msg-text">{{ msg.content }}<span v-if="msg.typing" class="typing-cursor"></span></div>
              </div>
            </div>
          </div>
        </TransitionGroup>
      </div>

      <!-- 回到底部按钮 -->
      <Transition name="fade-up">
        <button v-if="showScrollDown" class="scroll-down-btn" @click="scrollToBottom">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19" />
            <polyline points="19 12 12 19 5 12" />
          </svg>
        </button>
      </Transition>

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
  </div>
</template>

<style scoped>
.chat-page-root {
  display: flex;
  height: 100%;
  width: 100%;
  background: var(--bg);
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}

.chat-not-found {
  height: 100%;
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
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border-right: 1px solid var(--border);
  overflow: hidden;
  transition: width 0.25s ease, opacity 0.25s ease;
}

.chat-sidebar.is-collapsed {
  width: 0;
  min-width: 0;
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
  height: 100%;
  overflow: hidden;
  position: relative;
}

/* Top bar */
.chat-topbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  flex-shrink: 0;
  min-height: 48px;
  border-bottom: 1px solid var(--border);
}

/* ============ MODEL SELECTOR ============ */
.model-selector {
  position: relative;
}

.model-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface);
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text);
}

.model-trigger:hover {
  border-color: var(--primary);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

.model-trigger-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.4);
}

.model-trigger-label {
  font-size: 13px;
  font-weight: 600;
}

.model-trigger-chevron {
  transition: transform 0.2s ease;
  color: var(--muted);
}

.model-trigger-chevron.open {
  transform: rotate(180deg);
}

.model-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  min-width: 240px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  z-index: 100;
}

[data-theme='dark'] .model-dropdown {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
}

.model-dropdown-title {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--muted);
  font-weight: 600;
  padding: 6px 10px 8px;
}

.model-option {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 10px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  text-align: left;
  color: var(--text);
}

.model-option:hover {
  background: var(--surface-muted);
}

.model-option.active {
  background: rgba(99, 102, 241, 0.08);
}

.model-option-radio {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid var(--border);
  display: grid;
  place-items: center;
  flex-shrink: 0;
  transition: border-color 0.15s;
}

.model-option.active .model-option-radio {
  border-color: var(--primary);
}

.model-option-radio-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
}

.model-option-info {
  flex: 1;
  min-width: 0;
}

.model-option-name {
  font-size: 13px;
  font-weight: 600;
}

.model-option-desc {
  font-size: 11px;
  color: var(--muted);
  margin-top: 2px;
}

.model-option-check {
  color: var(--primary);
  flex-shrink: 0;
}

/* Dropdown animation */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.97);
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
  min-height: 0;
  scroll-behavior: auto;
}

.msg-list {
  display: flex;
  flex-direction: column;
}

/* Load more */
.load-more-hint {
  display: flex;
  justify-content: center;
  padding: 12px;
  flex-shrink: 0;
}

.load-more-btn {
  border: none;
  background: var(--surface-muted);
  color: var(--muted);
  font-size: 12px;
  font-weight: 500;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.load-more-btn:hover {
  background: var(--border);
  color: var(--text);
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

/* Message transition */
.msg-enter-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.msg-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.msg-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.msg-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.msg-row {
  padding: 14px 0;
  flex-shrink: 0;
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
  background: rgba(99, 102, 241, 0.06);
  border: 1px solid rgba(99, 102, 241, 0.12);
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

/* Typing cursor */
.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background: var(--primary);
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* Scroll to bottom button */
.scroll-down-btn {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
  display: grid;
  place-items: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 10;
}

.scroll-down-btn:hover {
  transform: translateX(-50%) translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.fade-up-enter-active,
.fade-up-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}

/* Input */
.chat-input-area {
  flex-shrink: 0;
  padding: 12px 24px 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  border-top: 1px solid var(--border);
  background: var(--bg);
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