<template>
  <div class="page upload-page">
    <!-- Upload result dialog -->
    <Transition name="fade">
      <div v-if="uploadResult" class="dialog-overlay" @click.self="dismissDialog">
        <div class="dialog-card">
          <!-- Loading / Progress state -->
          <template v-if="uploadResult.status === 'loading'">
            <!-- 钟表图标 -->
            <div class="dialog-icon" :class="{ 'dialog-icon-error': uploadError, 'dialog-icon-done': uploadCompleted }">
              <template v-if="uploadCompleted">
                <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </template>
              <template v-else-if="uploadError">
                <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="15" y1="9" x2="9" y2="15"/>
                  <line x1="9" y1="9" x2="15" y2="15"/>
                </svg>
              </template>
              <template v-else>
                <svg class="clock-icon" width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
              </template>
            </div>

            <!-- 标题 -->
            <p class="dialog-title">
              {{ uploadCompleted ? '处理完成' : uploadError ? '处理失败' : '正在处理' }}
            </p>

            <!-- 进度条 -->
            <div class="progress-bar-wrap">
              <div class="progress-bar">
                <div
                  class="progress-bar-fill"
                  :class="{
                    animated: !uploadError && !uploadCompleted,
                    error: uploadError,
                    done: uploadCompleted
                  }"
                  :style="{ width: progressBarWidth + '%' }"
                ></div>
              </div>
              <span class="progress-percent">{{ progressBarWidth }}%</span>
            </div>

            <!-- 状态描述 -->
            <p class="dialog-desc" :class="{ 'dialog-desc-error': uploadError }">
              {{ uploadCompleted ? '项目已就绪，可前往项目列表查看' : uploadError ? uploadError : '请稍候…' }}
            </p>

            <!-- 操作按钮 -->
            <div v-if="uploadError" class="dialog-actions">
              <button class="btn btn-secondary" @click="dismissUploadDialog">关闭</button>
              <button class="btn btn-primary" @click="handleZipUpload">重新上传</button>
            </div>
          </template>

          <!-- Success state -->
          <template v-else-if="uploadResult.status === 'success'">
            <div class="result-icon result-icon-success">
              <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
            <p class="dialog-title">上传成功</p>
            <!-- 完成进度条 -->
            <div class="progress-bar-wrap">
              <div class="progress-bar">
                <div class="progress-bar-fill done" style="width: 100%"></div>
              </div>
              <span class="progress-percent">100%</span>
            </div>
            <p class="dialog-desc">{{ uploadResult.message || '项目已成功创建，正在解析代码结构' }}</p>
            <div class="dialog-actions">
              <button class="btn btn-primary" @click="goToProjects">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                  <polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
                查看项目列表
              </button>
              <button class="btn btn-secondary" @click="dismissDialog">返回</button>
            </div>
          </template>

          <!-- Error state -->
          <template v-else-if="uploadResult.status === 'error'">
            <div class="result-icon result-icon-error">
              <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
            </div>
            <p class="dialog-title">上传失败</p>
            <p class="dialog-desc dialog-desc-error">{{ uploadResult.message || '发生未知错误，请稍后重试' }}</p>
            <div class="dialog-actions">
              <button class="btn btn-secondary" @click="dismissDialog">返回</button>
            </div>
          </template>
        </div>
      </div>
    </Transition>

    <div class="upload-container">
      <div class="upload-header">
        <h1 class="upload-title">上传项目</h1>
        <p class="upload-subtitle">上传成功后自动进入项目列表</p>
      </div>


      <div class="name-bar">
        <label class="form-label">项目名称</label>
        <input
          v-model="projectName"
          type="text"
          class="input"
          placeholder="输入项目名称（可选，留空自动使用仓库名或文件名）"
          :disabled="submitting"
        />
      </div>

      <div class="dual-panel">
        <div class="panel panel-left">
          <div class="panel-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
          </div>
          <h3 class="panel-title">Git 仓库导入</h3>
          <p class="panel-desc">粘贴仓库地址，系统将自动克隆并解析代码结构</p>
          <div class="panel-form">
            <input
              v-model="gitUrl"
              type="url"
              class="input"
              placeholder="https://github.com/org/repo.git"
              :disabled="submitting"
            />
            <button
              class="btn btn-primary btn-full"
              :disabled="!gitUrl.trim() || submitting"
              @click="handleGitSubmit"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
              开始导入
            </button>
          </div>
          <div class="panel-tip">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            支持 GitHub / GitLab / Bitbucket 等公开仓库
          </div>
        </div>

        <div class="divider-vertical">
          <span class="divider-text">或</span>
        </div>

        <div class="panel panel-right">
          <div class="panel-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
            </svg>
          </div>
          <h3 class="panel-title">ZIP 压缩包上传</h3>
          <p class="panel-desc">拖拽或选择 ZIP 文件，完成后自动解析项目</p>
          <div
            class="dropzone"
            :class="{ dragging: isDragging, disabled: submitting }"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
          >
            <div class="dropzone-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
              </svg>
            </div>
            <div class="dropzone-title">拖拽 ZIP 文件到这里</div>
            <div class="dropzone-subtitle">或点击按钮选择文件</div>
            <input ref="zipInput" type="file" accept=".zip" class="file-input" :disabled="submitting" @change="handleZipSelect" />
          </div>
          <div v-if="zipFile" class="file-row">
            <div class="file-info">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <span class="file-name">{{ zipFile.name }}</span>
            </div>
            <button class="btn btn-primary btn-full" :disabled="submitting" @click="handleZipUpload">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
              </svg>
              上传并解析
            </button>
          </div>
          <div class="panel-tip">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            仅支持 .zip 格式，大小不超过 50MB
          </div>
        </div>
      </div>

      <RouterLink to="/projects" class="link-projects">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        查看已有项目
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProjects } from '../store/projects'
import { useStompClient } from '../composables/useStompClient'
import { projectsApi } from '../api/project'

const projectName = ref('')
const gitUrl = ref('')
const zipInput = ref(null)
const zipFile = ref(null)
const isDragging = ref(false)
const submitting = ref(false)
const uploadResult = ref(null) // null | { status, projectId?, message? }
const router = useRouter()
const { addProjectFromGit, addProjectFromZip, fetchProjects } = useProjects()

// STOMP WebSocket
const { subscribeProgress, initConnection } = useStompClient()

// 进度相关
const uploadProgress = reactive({ percent: 0, step: '', message: '' })
const uploadError = ref('')
const uploadCompleted = ref(false)
const uploadType = ref('zip') // 'zip' | 'git'
let unsubProgress = null

const ZIP_STEPS = [
  { key: 'EXTRACTING', label: '解压文件', range: [10, 60] },
  { key: 'SAVING', label: '保存项目信息', range: [60, 100] },
]

const GIT_STEPS = [
  { key: 'CLONING', label: '克隆仓库', range: [10, 60] },
  { key: 'SAVING', label: '保存项目信息', range: [60, 100] },
]

const stepDefs = computed(() => uploadType.value === 'git' ? GIT_STEPS : ZIP_STEPS)

const progressSteps = computed(() => {
  const s = uploadProgress.step
  const defs = stepDefs.value
  const idx = defs.findIndex((x) => x.key === s)
  return defs.map((x, i) => ({
    ...x,
    active: x.key === s,
    done: i < idx || (i === idx && uploadProgress.percent >= x.range[1]),
  }))
})

const progressBarWidth = computed(() => {
  if (uploadError.value || uploadCompleted.value) return 100
  return Math.max(uploadProgress.percent, 2)
})

const goToProjects = () => {
  dismissUploadDialog()
  router.push({ path: '/projects' })
}

const dismissDialog = () => {
  if (uploadResult.value?.status === 'loading' && !uploadError.value && !uploadCompleted.value) return
  dismissUploadDialog()
}

const dismissUploadDialog = () => {
  if (unsubProgress) { unsubProgress(); unsubProgress = null }
  uploadResult.value = null
  uploadError.value = ''
  uploadCompleted.value = false
  uploadProgress.percent = 0
  uploadProgress.step = ''
  uploadProgress.message = ''
}

const resetProgress = () => {
  uploadError.value = ''
  uploadCompleted.value = false
  uploadProgress.percent = 0
  uploadProgress.step = ''
  uploadProgress.message = ''
}

const handleGitSubmit = async () => {
  const trimmed = gitUrl.value.trim()
  if (!trimmed || submitting.value) return
  submitting.value = true
  uploadType.value = 'git'
  resetProgress()
  uploadResult.value = { status: 'loading' }

  try {
    await initConnection()

    unsubProgress = await subscribeProgress('/topic/progress', (msg) => {
      const tracked = uploadResult.value?.projectId
      if (tracked && msg.taskId && msg.taskId !== tracked && msg.taskId !== String(tracked)) return
      if (msg.taskId && !uploadResult.value?.projectId) {
        uploadResult.value = { ...uploadResult.value, projectId: msg.taskId }
      }
      uploadProgress.percent = msg.percent ?? uploadProgress.percent
      uploadProgress.step = msg.step ?? uploadProgress.step
      uploadProgress.message = msg.message ?? ''
      if (msg.step === 'ERROR') uploadError.value = msg.message || '上传处理失败'
      if (msg.step === 'COMPLETED') {
        uploadProgress.step = 'SAVING'
        uploadProgress.percent = 100
        uploadCompleted.value = true
      }
    }, { brokerURL: '/ws' })

    const raw = await projectsApi.createFromGit(trimmed, projectName.value || null)
    if (raw.projectId || raw.id) {
      uploadResult.value = { ...uploadResult.value, projectId: raw.projectId || raw.id }
      fetchProjects(1)
    }

    if (!uploadCompleted.value && !uploadError.value) {
      await new Promise((resolve) => {
        const t = setInterval(() => {
          if (uploadCompleted.value || uploadError.value) { clearInterval(t); resolve() }
        }, 200)
        setTimeout(() => { clearInterval(t); resolve() }, 300000)
      })
    }

    submitting.value = false

    if (uploadError.value) {
    } else {
      gitUrl.value = ''
      projectName.value = ''
      if (unsubProgress) { unsubProgress(); unsubProgress = null }
      uploadResult.value = { status: 'success', message: '项目已创建成功，正在后台解析代码结构' }
    }
  } catch (err) {
    submitting.value = false
    uploadError.value = err.message || '上传失败，请重试'
    if (!uploadResult.value?.projectId) {
      uploadResult.value = { status: 'loading' }
    }
  }
}

const handleZipSelect = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  zipFile.value = file
  if (!projectName.value && file.name) {
    projectName.value = file.name.replace(/\.zip$/i, '')
  }
}

const handleZipUpload = async () => {
  if (!zipFile.value || submitting.value) return
  submitting.value = true
  uploadType.value = 'zip'
  resetProgress()
  uploadResult.value = { status: 'loading' }

  try {
    await initConnection()

    // 先订阅 WebSocket 进度
    unsubProgress = await subscribeProgress('/topic/progress', (msg) => {
      const tracked = uploadResult.value?.projectId
      if (tracked && msg.taskId && msg.taskId !== tracked && msg.taskId !== String(tracked)) return
      if (msg.taskId && !uploadResult.value?.projectId) {
        uploadResult.value = { ...uploadResult.value, projectId: msg.taskId }
      }
      uploadProgress.percent = msg.percent ?? uploadProgress.percent
      uploadProgress.step = msg.step ?? uploadProgress.step
      uploadProgress.message = msg.message ?? ''
      if (msg.step === 'ERROR') uploadError.value = msg.message || '上传处理失败'
      if (msg.step === 'COMPLETED') {
        uploadProgress.step = 'SAVING'
        uploadProgress.percent = 100
        uploadCompleted.value = true
      }
    }, { brokerURL: '/ws' })

    // 发起上传
    const formData = new FormData()
    formData.append('file', zipFile.value)
    if (projectName.value) formData.append('projectName', projectName.value)

    const raw = await projectsApi.createFromZip(formData)
    if (raw.projectId || raw.id) {
      uploadResult.value = { ...uploadResult.value, projectId: raw.projectId || raw.id }
      fetchProjects(1)
    }

    // 等待 WebSocket 完成或出错，最多 120s
    if (!uploadCompleted.value && !uploadError.value) {
      await new Promise((resolve) => {
        const t = setInterval(() => {
          if (uploadCompleted.value || uploadError.value) { clearInterval(t); resolve() }
        }, 200)
        setTimeout(() => { clearInterval(t); resolve() }, 120000)
      })
    }

    submitting.value = false

    if (uploadError.value) {
      // 保持弹框显示错误
    } else {
      zipFile.value = null
      projectName.value = ''
      if (zipInput.value) zipInput.value.value = ''
      if (unsubProgress) { unsubProgress(); unsubProgress = null }
      uploadResult.value = { status: 'success', message: '项目已创建成功，正在后台解析代码结构' }
    }
  } catch (err) {
    submitting.value = false
    uploadError.value = err.message || '上传失败，请重试'
    if (!uploadResult.value?.projectId) {
      uploadResult.value = { status: 'loading' }
    }
  }
}

const handleDrop = (event) => {
  const file = event.dataTransfer?.files?.[0]
  isDragging.value = false
  if (!file) return
  if (!file.name.toLowerCase().endsWith('.zip')) return
  zipFile.value = file
  if (!projectName.value && file.name) {
    projectName.value = file.name.replace(/\.zip$/i, '')
  }
}

onBeforeUnmount(() => {
  if (unsubProgress) { unsubProgress(); unsubProgress = null }
})
</script>

<style scoped>
.upload-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: transparent;
  position: relative;
}

/* ── Dialog overlay ── */
.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
}

.dialog-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 48px 56px;
  max-width: 440px;
  width: 90vw;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.12);
}

.dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 18px;
}

.dialog-desc {
  font-size: 13px;
  color: var(--muted);
  margin: 0 0 20px;
  line-height: 1.6;
}

.dialog-desc-error {
  color: var(--danger, #dc2626);
}

.dialog-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
  flex-wrap: wrap;
  justify-content: center;
}

/* Result icons */
.result-icon {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.result-icon-success {
  background: var(--success-muted, rgba(22, 163, 74, 0.12));
  color: var(--success, #16a34a);
}

.result-icon-error {
  background: var(--danger-muted, rgba(220, 38, 38, 0.1));
  color: var(--danger, #dc2626);
}

/* Progress bar in dialog */
.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  margin-bottom: 14px;
}

.progress-bar {
  flex: 1;
  height: 10px;
  background: var(--surface-muted, #f3f4f6);
  border-radius: 5px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 5px;
  transition: width 0.4s ease;
}

.progress-bar-fill.animated {
  background: linear-gradient(90deg, var(--primary), var(--primary-hover, #6366f1), var(--primary));
  background-size: 200% 100%;
  animation: shimmer 1.5s ease infinite;
}

.progress-bar-fill.error { background: var(--danger, #dc2626); animation: none; }
.progress-bar-fill.done { background: var(--success, #16a34a); animation: none; }

.progress-percent {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
  min-width: 36px;
  text-align: right;
}

/* 加载态钟表图标 */
.dialog-icon {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  background: var(--primary-muted, rgba(99, 102, 241, 0.1));
  color: var(--primary);
  transition: background 0.3s, color 0.3s;
}

.dialog-icon-done {
  background: var(--success-muted, rgba(22, 163, 74, 0.12));
  color: var(--success, #16a34a);
}

.dialog-icon-error {
  background: var(--danger-muted, rgba(220, 38, 38, 0.1));
  color: var(--danger, #dc2626);
}

.clock-icon {
  animation: clock-tick 2s ease-in-out infinite;
}

@keyframes clock-tick {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(3deg); }
  75% { transform: rotate(-3deg); }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Fade transition for dialog overlay */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ── Existing upload layout ── */
.upload-container {
  width: 100%;
  max-width: 960px;
}

.upload-header {
  text-align: center;
  margin-bottom: 28px;
}

.upload-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}

.upload-subtitle {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

.name-bar {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  min-width: 80px;
}

.name-bar .input {
  flex: 1;
  border: 1.5px solid var(--border);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--surface);
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
}

.name-bar .input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.name-bar .input::placeholder {
  color: var(--muted);
  font-size: 13px;
}

.dual-panel {
  display: flex;
  gap: 0;
  align-items: stretch;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
}

.panel {
  flex: 1;
  padding: 28px 24px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  background: var(--surface);
}

.divider-vertical {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  width: 1px;
  background: var(--border);
  flex-shrink: 0;
}

.divider-text {
  position: absolute;
  background: var(--surface);
  padding: 8px 10px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  color: var(--muted);
  border: 1.5px solid var(--border);
  white-space: nowrap;
}

.panel-icon {
  width: 60px;
  height: 60px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.panel-left .panel-icon {
  background: var(--primary-muted, rgba(99, 102, 241, 0.1));
  color: var(--primary);
}

.panel-right .panel-icon {
  background: var(--warning-muted, rgba(245, 158, 11, 0.1));
  color: var(--warning, #f59e0b);
}

.panel-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.panel-desc {
  font-size: 13px;
  color: var(--muted);
  margin: 0 0 20px;
  line-height: 1.5;
}

.panel-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input {
  width: 100%;
  border: 1.5px solid var(--border);
  border-radius: 8px;
  padding: 11px 14px;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--surface);
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
  box-sizing: border-box;
}

.input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.input::placeholder {
  color: var(--muted);
  font-size: 13px;
}

.input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 11px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  background: var(--primary-hover, #4f46e5);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);
}

.btn-primary:disabled {
  background: var(--disabled-bg, #d1d5db);
  color: var(--disabled-text, #9ca3af);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.btn-secondary {
  background: var(--surface);
  color: var(--text-primary);
  border: 1.5px solid var(--border);
}

.btn-secondary:hover {
  background: var(--surface-muted, #f9fafb);
  border-color: var(--primary-light, #a5b4fc);
}

.btn-full {
  width: 100%;
}

.dropzone {
  width: 100%;
  border: 2px dashed var(--border);
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s;
  background: var(--surface-muted, var(--surface));
  position: relative;
  box-sizing: border-box;
}

.dropzone:hover {
  border-color: var(--primary-light, #a5b4fc);
  background: var(--surface);
}

.dropzone.dragging {
  border-color: var(--primary);
  background: var(--primary-muted, rgba(99, 102, 241, 0.08));
  box-shadow: 0 0 0 6px rgba(99, 102, 241, 0.08);
  transform: scale(1.01);
}

.dropzone.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.dropzone-icon {
  color: var(--muted);
  margin-bottom: 8px;
  transition: color 0.25s;
}

.dropzone.dragging .dropzone-icon {
  color: var(--primary);
}

.dropzone-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.dropzone-subtitle {
  font-size: 12px;
  color: var(--muted);
}

.file-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.file-input:disabled {
  cursor: not-allowed;
}

.file-row {
  width: 100%;
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--success-muted, rgba(22, 163, 74, 0.1));
  border: 1.5px solid var(--success-light, #bbf7d0);
  border-radius: 8px;
  color: var(--success, #166534);
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.panel-tip {
  margin-top: auto;
  padding-top: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--muted);
}

.panel-tip svg {
  flex-shrink: 0;
  color: var(--muted);
}

.link-projects {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--primary);
  text-decoration: none;
  transition: color 0.2s;
}

.link-projects:hover {
  color: var(--primary-hover, #4f46e5);
  text-decoration: underline;
}

@media (max-width: 700px) {
  .dual-panel {
    flex-direction: column;
  }

  .divider-vertical {
    width: 100%;
    height: 1px;
  }

  .divider-text {
    position: absolute;
    padding: 4px 12px;
    border-radius: 20px;
  }

  .panel {
    padding: 20px 16px;
  }

  .name-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .name-bar .form-label {
    min-width: auto;
  }

  .upload-title {
    font-size: 22px;
  }
}

</style>
