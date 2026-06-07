<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProjects } from '../store/projects'

const projectName = ref('')
const gitUrl = ref('')
const zipInput = ref(null)
const zipFile = ref(null)
const isDragging = ref(false)
const router = useRouter()
const { addProjectFromGit, addProjectFromZip } = useProjects()

const goToProjects = () => {
  router.push('/projects')
}

const handleGitSubmit = () => {
  const trimmed = gitUrl.value.trim()
  if (!trimmed) return
  addProjectFromGit(trimmed, projectName.value)
  gitUrl.value = ''
  projectName.value = ''
  goToProjects()
}

const handleZipSelect = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  zipFile.value = file
  // Auto-fill project name from file name if empty
  if (!projectName.value && file.name) {
    projectName.value = file.name.replace(/\.zip$/i, '')
  }
}

const handleZipUpload = () => {
  if (!zipFile.value) return
  addProjectFromZip(zipFile.value, projectName.value)
  zipFile.value = null
  projectName.value = ''
  if (zipInput.value) {
    zipInput.value.value = ''
  }
  goToProjects()
}

const handleDrop = (event) => {
  const file = event.dataTransfer?.files?.[0]
  isDragging.value = false
  if (!file) return
  if (!file.name.toLowerCase().endsWith('.zip')) return
  zipFile.value = file
  // Auto-fill project name from file name if empty
  if (!projectName.value && file.name) {
    projectName.value = file.name.replace(/\.zip$/i, '')
  }
}
</script>

<template>
  <div class="page upload-page">
    <div class="upload-container">
      <div class="upload-header">
        <h1 class="upload-title">上传项目</h1>
        <p class="upload-subtitle">上传成功后自动进入项目列表</p>
      </div>

      <div class="upload-card">
        <div class="form-group">
          <label class="form-label">项目名称</label>
          <input
            v-model="projectName"
            type="text"
            class="input"
            placeholder="输入项目名称（可选）"
          />
          <span class="form-hint">留空将自动使用仓库名或文件名</span>
        </div>

        <div class="divider">
          <span>方式一</span>
        </div>

        <div class="upload-section">
          <h3 class="section-title">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            Git 仓库导入
          </h3>
          <p class="section-desc">粘贴仓库地址，系统将自动克隆并解析代码</p>
          <div class="form-row">
            <input
              v-model="gitUrl"
              type="url"
              class="input"
              placeholder="https://github.com/org/repo.git"
            />
            <button class="btn btn-primary" :disabled="!gitUrl.trim()" @click="handleGitSubmit">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
              开始导入
            </button>
          </div>
        </div>

        <div class="divider">
          <span>方式二</span>
        </div>

        <div class="upload-section">
          <h3 class="section-title">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
            </svg>
            ZIP 压缩包上传
          </h3>
          <p class="section-desc">拖拽或选择 ZIP 文件，完成后自动解析</p>
          <div
            class="dropzone"
            :class="{ dragging: isDragging }"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
          >
            <div class="dropzone-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
              </svg>
            </div>
            <div class="dropzone-title">拖拽 ZIP 文件到这里</div>
            <div class="dropzone-subtitle">或点击下方按钮选择文件</div>
            <input ref="zipInput" type="file" accept=".zip" class="file-input" @change="handleZipSelect" />
          </div>
          <div v-if="zipFile" class="file-row">
            <div class="file-info">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <span class="file-name">{{ zipFile.name }}</span>
            </div>
            <button class="btn btn-primary" @click="handleZipUpload">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
              </svg>
              上传并解析
            </button>
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
