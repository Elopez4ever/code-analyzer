<template>
  <div class="project-list-page">
    <div class="panel">
      <div class="panel-header">
        <h2 class="panel-title">项目列表</h2>
        <div class="panel-header-right">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索项目名称"
            allow-clear
            class="search-input"
            @search="setProjectName"
          />
          <button class="btn btn-primary btn-sm upload-btn" @click="openUploadModal">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
            </svg>
            上传
          </button>
          <button
            v-if="selectedIds.length > 0"
            class="btn btn-danger batch-delete-btn"
            @click="handleBatchDelete"
          >
            批量删除 ({{ selectedIds.length }})
          </button>
        </div>
      </div>

      <div v-if="error" class="empty-state">
        <p class="muted">加载失败：{{ error }}</p>
        <button class="btn btn-outline btn-sm" style="margin-top:12px" @click="fetchProjects(currentPage)">
          重试
        </button>
      </div>

      <template v-else>
        <div class="table-wrapper">
          <transition name="page-fade" mode="out-in">
            <div :key="fetchCounter" class="table-transition-inner">
              <a-table
                :columns="columns"
                :data-source="sortedProjects"
                :loading="loading"
                :pagination="false"
                :row-selection="rowSelection"
                row-key="id"
                class="project-table"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'sourceType'">
                    <span class="pill">{{ record.sourceType }}</span>
                  </template>

                  <template v-else-if="column.key === 'status'">
                    <StatusBadge :status="record.status" />
                  </template>

                  <template v-else-if="column.key === 'updatedAt'">
                    {{ formatDateTime(record.updatedAt) }}
                  </template>

                  <template v-else-if="column.key === 'actions'">
                    <div class="action-row">
                      <button class="btn btn-outline btn-sm" @click="openEditModal(record)">
                        编辑
                      </button>
                      <button
                        v-if="record.status === 'FAILED'"
                        class="btn btn-outline btn-sm"
                        @click="retryProject(record.id)"
                      >
                        重试
                      </button>
                      <RouterLink
                        v-else-if="record.status === 'READY'"
                        class="btn btn-outline btn-accent btn-sm"
                        :to="`/chat/${record.id}`"
                      >
                        问答
                      </RouterLink>
                      <span v-else class="btn btn-outline btn-sm btn-disabled">问答</span>
                    </div>
                  </template>
                </template>
              </a-table>
            </div>
          </transition>
        </div>

        <div class="pagination-wrap">
          <a-pagination
            v-model:current="currentPage"
            :total="total"
            :page-size="10"
            show-quick-jumper
            :show-total="(total) => `共 ${total} 个项目`"
            @change="fetchProjects"
          />
        </div>
      </template>
    </div>

    <!-- 编辑弹框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑项目"
      :confirm-loading="editLoading"
      @ok="handleEditConfirm"
      @cancel="editModalVisible.value = false"
      ok-text="确定"
      cancel-text="取消"
    >
      <div class="edit-form">
        <div class="edit-form-item">
          <label class="edit-label">项目名</label>
          <a-input v-model:value="editForm.name" placeholder="请输入项目名" />
        </div>
        <div class="edit-form-item">
          <label class="edit-label">来源</label>
          <a-input v-model:value="editForm.source" placeholder="请输入来源地址" />
        </div>
        <div class="edit-form-item">
          <label class="edit-label">类型</label>
          <span class="pill">{{ editForm.sourceType }}</span>
        </div>
        <div class="edit-form-item">
          <label class="edit-label">状态</label>
          <StatusBadge :status="editForm.status" />
        </div>
        <div class="edit-form-item">
          <label class="edit-label">更新时间</label>
          <span class="edit-text">{{ formatDateTime(editForm.updatedAt) }}</span>
        </div>
      </div>
    </a-modal>

    <!-- 上传弹框 -->
    <a-modal
      v-model:open="uploadModalVisible"
      :title="uploadPhase === 'progress' ? '上传进度' : '上传项目'"
      :footer="null"
      :closable="uploadPhase !== 'uploading' && uploadPhase !== 'progress'"
      :mask-closable="uploadPhase !== 'uploading' && uploadPhase !== 'progress'"
      @cancel="closeUploadModal"
      width="480px"
    >
      <!-- 选择文件阶段 -->
      <template v-if="uploadPhase === 'selecting'">
        <div
          class="upload-dropzone"
          :class="{ dragging: isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleUploadDrop"
        >
          <div class="upload-dropzone-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
            </svg>
          </div>
          <div class="upload-dropzone-title">拖拽 ZIP 文件到这里</div>
          <div class="upload-dropzone-subtitle">或点击选择文件，最大 50MB</div>
          <input ref="uploadFileInput" type="file" accept=".zip" class="upload-file-input" @change="handleUploadFileSelect" />
        </div>

        <div v-if="uploadFile" class="upload-file-row">
          <div class="upload-file-info">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span class="upload-file-name">{{ uploadFile.name }}</span>
            <span class="upload-file-size">{{ formatFileSize(uploadFile.size) }}</span>
          </div>
        </div>

        <div class="upload-name-row">
          <label class="upload-label">项目名称</label>
          <a-input v-model:value="uploadProjectName" placeholder="可选，留空自动使用文件名" />
        </div>

        <div class="upload-actions">
          <button class="btn btn-outline btn-sm" @click="closeUploadModal">取消</button>
          <button
            class="btn btn-primary btn-sm"
            :disabled="!uploadFile"
            @click="startUpload"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
            </svg>
            上传并解析
          </button>
        </div>
      </template>

      <!-- 上传中 / 进度阶段 -->
      <template v-else-if="uploadPhase === 'uploading' || uploadPhase === 'progress'">
        <div class="upload-progress-area">
          <div class="upload-progress-header">
            <div class="upload-progress-file">{{ uploadFile?.name || '—' }}</div>
          </div>

          <!-- 步骤标签 -->
          <div class="upload-progress-step-tabs">
            <span
              v-for="step in progressSteps"
              :key="step.key"
              class="upload-progress-step-tab"
              :class="{ active: step.active, done: step.done }"
            >
              <span class="step-tab-dot">
                <svg v-if="step.done" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                <span v-else-if="step.active" class="step-tab-spinner"></span>
                <span v-else class="step-tab-ring"></span>
              </span>
              <span class="step-tab-label">{{ step.label }}</span>
            </span>
          </div>

          <!-- 进度条 -->
          <div class="upload-progress-bar-wrap">
            <div class="upload-progress-bar">
              <div
                class="upload-progress-bar-fill"
                :class="{
                  animated: uploadProgress.percent < 100 && !uploadError && !uploadCompleted,
                  error: uploadError,
                  done: uploadCompleted
                }"
                :style="{ width: (uploadError ? 100 : Math.max(uploadProgress.percent, 2)) + '%' }"
              ></div>
            </div>
            <div class="upload-progress-percent">{{ uploadProgress.percent }}%</div>
          </div>

          <!-- 实时进度信息（进度条下方） -->
          <div class="upload-progress-detail">
            <div class="progress-detail-header">进度详情</div>
            <div v-if="uploadCompleted" class="upload-progress-msg-row done">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              <span class="msg-row-text">解析完成，项目已就绪</span>
            </div>
            <div v-else-if="uploadProgress.message" class="upload-progress-msg-row">
              <span v-if="!uploadError" class="msg-row-spinner"></span>
              <span class="msg-row-text">{{ uploadProgress.message }}</span>
            </div>
            <div v-else class="upload-progress-msg-row">
              <span class="msg-row-spinner"></span>
              <span class="msg-row-text muted">等待服务器响应…</span>
            </div>
          </div>

          <!-- 错误信息 -->
          <div v-if="uploadError" class="upload-progress-error">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            {{ uploadError }}
          </div>

          <!-- 完成 / 失败操作 -->
          <div v-if="uploadPhase === 'progress' && !uploadError" class="upload-actions">
            <button class="btn btn-outline btn-sm" @click="closeUploadModal">关闭</button>
          </div>
          <div v-if="uploadError" class="upload-actions">
            <button class="btn btn-outline btn-sm" @click="closeUploadModal">关闭</button>
            <button class="btn btn-primary btn-sm" @click="startUpload">重新上传</button>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useProjects } from '../store/projects'
import StatusBadge from '../components/StatusBadge.vue'
import { formatDateTime } from '../utils/format'
import { showSuccessToast } from '../utils/toast'
import { useStompClient } from '../composables/useStompClient'
import { projectsApi } from '../api/project'

const {
  sortedProjects,
  loading,
  error,
  currentPage,
  totalPages,
  total,
  projectName,
  fetchCounter,
  fetchProjects,
  setProjectName,
  removeProject,
  batchRemoveProject,
  retryProject,
  updateProject,
  addProjectFromZip,
} = useProjects()

const searchText = ref(projectName.value)
const selectedIds = ref([])

// 上传相关状态
const uploadModalVisible = ref(false)
const uploadPhase = ref('selecting') // selecting | uploading | progress
const uploadFile = ref(null)
const uploadFileInput = ref(null)
const uploadProjectName = ref('')
const isDragging = ref(false)
const uploadProgress = reactive({ percent: 0, step: '', message: '' })
const uploadError = ref('')
const uploadCompleted = ref(false)
const trackedProjectId = ref(null)

// STOMP WebSocket
const { subscribeProgress, initConnection } = useStompClient()

// 进度步骤定义（与后端 step 对应）
const STEP_DEFS = [
  { key: 'EXTRACTING', label: '解压文件', range: [10, 60] },
  { key: 'SAVING', label: '保存项目信息', range: [60, 100] },
]

const progressSteps = computed(() => {
  const currentStep = uploadProgress.step
  const currentIdx = STEP_DEFS.findIndex((s) => s.key === currentStep)
  return STEP_DEFS.map((s, i) => ({
    ...s,
    active: s.key === currentStep,
    done: i < currentIdx || (i === currentIdx && uploadProgress.percent >= s.range[1]),
  }))
})

let unsubProgress = null

watch(searchText, (val) => {
  if (val === projectName.value) return
  setProjectName(val)
})

const rowSelection = computed(() => ({
  selectedRowKeys: selectedIds.value,
  onChange: (keys) => {
    selectedIds.value = keys
  },
}))

async function handleBatchDelete() {
  const ids = [...selectedIds.value]
  try {
    await batchRemoveProject(ids)
    selectedIds.value = []
    showSuccessToast(`已删除 ${ids.length} 个项目`)
    await fetchProjects(currentPage.value)
    // 当前页为空但仍有数据时，回到第一页
    if (sortedProjects.value.length === 0 && total.value > 0) {
      fetchProjects(1)
    }
  } catch {
    // error handled in store
  }
}

const columns = [
  { title: '项目名', dataIndex: 'name', key: 'name', width: 160 },
  { title: '来源', dataIndex: 'source', key: 'source', ellipsis: true, width: 300 },
  { title: '类型', dataIndex: 'sourceType', key: 'sourceType', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 160 },
  { title: '操作', key: 'actions', width: 140 },
]

// 编辑弹框
const editModalVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: null,
  name: '',
  source: '',
  sourceType: '',
  status: '',
  updatedAt: '',
})

function openEditModal(record) {
  editForm.id = record.id
  editForm.name = record.name
  editForm.source = record.source
  editForm.sourceType = record.sourceType
  editForm.status = record.status
  editForm.updatedAt = record.updatedAt
  editModalVisible.value = true
}

async function handleEditConfirm() {
  editLoading.value = true
  try {
    await updateProject(editForm.id, {
      name: editForm.name,
      gitUrl: editForm.source,
    })
    editModalVisible.value = false
  } catch {
    // error handled in store
  } finally {
    editLoading.value = false
  }
}

// ── 上传弹框 ──

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}

function openUploadModal() {
  resetUploadState()
  uploadModalVisible.value = true
}

function closeUploadModal() {
  if (unsubProgress) {
    unsubProgress()
    unsubProgress = null
  }
  uploadModalVisible.value = false
  uploadFile.value = null
  uploadProjectName.value = ''
  uploadError.value = ''
  uploadCompleted.value = false
  uploadPhase.value = 'selecting'
  uploadProgress.percent = 0
  uploadProgress.step = ''
  uploadProgress.message = ''
}

function resetUploadState() {
  uploadFile.value = null
  uploadProjectName.value = ''
  uploadError.value = ''
  uploadCompleted.value = false
  uploadPhase.value = 'selecting'
  uploadProgress.percent = 0
  uploadProgress.step = ''
  uploadProgress.message = ''
  trackedProjectId.value = null
  if (uploadFileInput.value) {
    uploadFileInput.value.value = ''
  }
}

function handleUploadFileSelect(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploadFile.value = file
  if (!uploadProjectName.value && file.name) {
    uploadProjectName.value = file.name.replace(/\.zip$/i, '')
  }
}

function handleUploadDrop(event) {
  isDragging.value = false
  const file = event.dataTransfer?.files?.[0]
  if (!file) return
  if (!file.name.toLowerCase().endsWith('.zip')) return
  uploadFile.value = file
  if (!uploadProjectName.value && file.name) {
    uploadProjectName.value = file.name.replace(/\.zip$/i, '')
  }
}

async function startUpload() {
  if (!uploadFile.value) return
  uploadError.value = ''
  uploadCompleted.value = false
  uploadPhase.value = 'uploading'
  uploadProgress.percent = 0
  uploadProgress.step = ''
  uploadProgress.message = ''

  try {
    // 1. 初始化 WebSocket 连接
    await initConnection()

    // 2. 先订阅进度（在发起 HTTP 请求之前），避免丢失后端同步阶段的进度消息
    unsubProgress = await subscribeProgress('/topic/progress', (msg) => {
      // 只要 taskId 匹配，或者还没有分配 taskId 时（上传请求尚未返回），都接收进度
      if (trackedProjectId.value && msg.taskId &&
          msg.taskId !== trackedProjectId.value &&
          msg.taskId !== String(trackedProjectId.value)) return

      // 收到 taskId 时同步记录（后端可能先推送进度再返回 HTTP 响应）
      if (msg.taskId && !trackedProjectId.value) {
        trackedProjectId.value = msg.taskId
      }

      uploadProgress.percent = msg.percent ?? uploadProgress.percent
      uploadProgress.step = msg.step ?? uploadProgress.step
      uploadProgress.message = msg.message ?? ''

      // 检查是否出错
      if (msg.step === 'ERROR') {
        uploadError.value = msg.message || '上传处理失败'
        uploadPhase.value = 'progress'
      }

      // 检查是否完成（收到 COMPLETED 消息）
      if (msg.step === 'COMPLETED') {
        uploadPhase.value = 'progress'
        uploadProgress.step = 'SAVING'
        uploadProgress.percent = 100
        uploadCompleted.value = true
      }
    }, { brokerURL: '/ws' })

    // 3. 发起上传
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    if (uploadProjectName.value) formData.append('projectName', uploadProjectName.value)

    const raw = await projectsApi.createFromZip(formData)
    const projectId = raw.projectId || raw.id

    if (projectId) {
      trackedProjectId.value = projectId
      uploadPhase.value = 'progress'

      // 刷新项目列表
      fetchProjects(currentPage.value)
      showSuccessToast('项目上传成功，正在后台解析')
    } else {
      // 没有 projectId，可能是旧版后端，直接标记完成
      uploadPhase.value = 'progress'
      uploadProgress.percent = 100
      uploadProgress.step = 'SAVING'
      uploadProgress.message = '上传完成'
      fetchProjects(currentPage.value)
      showSuccessToast('项目上传成功')
    }
  } catch (err) {
    uploadError.value = err.message || '上传失败，请重试'
    uploadPhase.value = 'progress'
  }
}

onMounted(() => fetchProjects(1))

onBeforeUnmount(() => {
  if (unsubProgress) {
    unsubProgress()
    unsubProgress = null
  }
})
</script>

<style scoped>
.project-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
}

.panel {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
  height: 56px;
  box-sizing: border-box;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  margin: 0;
  flex-shrink: 0;
}

.panel-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.search-input {
  width: 220px;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  font-size: 12px;
  line-height: 20px;
  border-radius: 6px;
  flex-shrink: 0;
  white-space: nowrap;
}

.batch-delete-btn {
  padding: 2px 10px;
  font-size: 12px;
  line-height: 20px;
  border-radius: 6px;
  flex-shrink: 0;
  white-space: nowrap;
}

.btn-danger {
  background: var(--danger, #e53e3e);
  color: #fff;
  border: 1px solid var(--danger, #e53e3e);
}

.btn-danger:hover {
  background: #c53030;
  border-color: #c53030;
}

.table-wrapper {
  overflow: visible;
}

.table-transition-inner {
  width: 100%;
  min-height: 500px;
}

/* 翻页过渡动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.project-table :deep(.ant-table) {
  background: transparent;
  font-size: 14px;
  color: var(--text);
}

.project-table :deep(.ant-table-thead > tr > th) {
  background: var(--surface-muted);
  border-bottom: 1px solid var(--border);
  color: var(--muted);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.project-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--border);
  white-space: nowrap;
  color: var(--text);
  font-weight: 500;
}

.project-table :deep(.ant-table-tbody > tr:hover > td) {
  background: var(--surface-muted);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: var(--text);
}

.empty-state .muted {
  font-size: 14px;
  color: var(--text);
  opacity: 0.85;
  margin: 0;
}

.action-row {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 24px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
  background: var(--surface);
}

.edit-form {
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.edit-form-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.edit-label {
  width: 70px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--muted);
  text-align: right;
}

.edit-text {
  font-size: 14px;
  color: var(--text);
}

/* ============ 上传弹框样式 ============ */

.upload-dropzone {
  border: 2px dashed var(--border);
  border-radius: 12px;
  padding: 32px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s;
  background: var(--surface-muted, var(--surface));
  position: relative;
  margin-bottom: 16px;
}

.upload-dropzone:hover {
  border-color: var(--primary-light, #a5b4fc);
  background: var(--surface);
}

.upload-dropzone.dragging {
  border-color: var(--primary);
  background: rgba(99, 102, 241, 0.08);
  box-shadow: 0 0 0 6px rgba(99, 102, 241, 0.08);
  transform: scale(1.01);
}

.upload-dropzone-icon {
  color: var(--muted);
  margin-bottom: 8px;
  transition: color 0.25s;
}

.upload-dropzone.dragging .upload-dropzone-icon {
  color: var(--primary);
}

.upload-dropzone-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
}

.upload-dropzone-subtitle {
  font-size: 12px;
  color: var(--muted);
}

.upload-file-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.upload-file-row {
  margin-bottom: 16px;
}

.upload-file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(22, 163, 74, 0.08);
  border: 1.5px solid rgba(22, 163, 74, 0.2);
  border-radius: 8px;
  color: var(--success, #166534);
}

.upload-file-name {
  font-size: 13px;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-file-size {
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
}

.upload-name-row {
  margin-bottom: 20px;
}

.upload-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6px;
}

.upload-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* ============ 上传进度样式 ============ */

.upload-progress-area {
  padding: 8px 0;
}

.upload-progress-header {
  margin-bottom: 12px;
}

.upload-progress-file {
  font-size: 13px;
  font-weight: 500;
  color: var(--muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.upload-progress-bar {
  flex: 1;
  height: 8px;
  background: var(--surface-muted, #f3f4f6);
  border-radius: 4px;
  overflow: hidden;
}

.upload-progress-bar-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 4px;
  transition: width 0.4s ease;
}

.upload-progress-bar-fill.animated {
  background: linear-gradient(90deg, var(--primary), var(--primary-hover, #6366f1), var(--primary));
  background-size: 200% 100%;
  animation: progress-shimmer 1.5s ease infinite;
}

.upload-progress-bar-fill.error {
  background: var(--danger, #dc2626);
  animation: none;
}

.upload-progress-bar-fill.done {
  background: var(--success, #16a34a);
  animation: none;
}

/* 进度条下方的进度详情 */
.upload-progress-detail {
  margin-top: 16px;
  padding: 10px 12px;
  background: var(--surface-muted, rgba(0,0,0,0.02));
  border: 1px solid var(--border);
  border-radius: 8px;
  min-height: 52px;
}

.progress-detail-header {
  font-size: 11px;
  font-weight: 600;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.upload-progress-msg-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 2px;
  min-height: 18px;
}

.msg-row-spinner {
  width: 10px;
  height: 10px;
  border: 2px solid rgba(99, 102, 241, 0.3);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  flex-shrink: 0;
}

.msg-row-text {
  font-size: 11px;
  color: var(--muted);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-row-text.muted {
  opacity: 0.6;
  font-style: italic;
}

.upload-progress-msg-row.done {
  color: var(--success, #16a34a);
}

.upload-progress-msg-row.done .msg-row-text {
  color: var(--success, #16a34a);
  font-weight: 600;
}

@keyframes progress-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.upload-progress-percent {
  font-size: 13px;
  font-weight: 700;
  color: var(--text);
  min-width: 36px;
  text-align: right;
}

.upload-progress-step-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 14px;
}

.upload-progress-step-tab {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--muted);
  transition: color 0.25s;
}

.upload-progress-step-tab.active {
  color: var(--primary);
}

.upload-progress-step-tab.done {
  color: var(--success, #16a34a);
}

.step-tab-dot {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
}

.step-tab-ring {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid var(--border);
}

.step-tab-spinner {
  width: 12px;
  height: 12px;
  border: 2px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.step-tab-label {
  white-space: nowrap;
  font-weight: 500;
}

.upload-progress-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: rgba(220, 38, 38, 0.06);
  border: 1px solid rgba(220, 38, 38, 0.15);
  border-radius: 8px;
  color: var(--danger, #dc2626);
  font-size: 13px;
  margin-bottom: 16px;
}

.upload-progress-error svg {
  flex-shrink: 0;
  color: var(--danger, #dc2626);
}
</style>
