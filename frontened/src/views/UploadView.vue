<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProjects } from '../store/projects'

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
  addProjectFromGit(trimmed)
  gitUrl.value = ''
  goToProjects()
}

const handleZipSelect = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  zipFile.value = file
}

const handleZipUpload = () => {
  if (!zipFile.value) return
  addProjectFromZip(zipFile.value)
  zipFile.value = null
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
}
</script>

<template>
  <div class="page upload-page">
    <header class="page-header">
      <div>
        <h2>上传项目</h2>
        <p class="muted">上传成功后自动进入项目列表。</p>
      </div>
      <RouterLink to="/projects" class="btn btn-ghost">查看项目列表</RouterLink>
    </header>

    <section class="upload-grid">
      <div class="card">
        <h3>Git URL 导入</h3>
        <p class="muted">粘贴仓库地址，系统将自动克隆并解析。</p>
        <div class="form-row">
          <input
            v-model="gitUrl"
            type="url"
            class="input"
            placeholder="https://github.com/org/repo.git"
          />
          <button class="btn btn-primary" :disabled="!gitUrl.trim()" @click="handleGitSubmit">
            开始导入
          </button>
        </div>
        <p class="hint">私有仓库需在后端配置访问凭证。</p>
      </div>

      <div class="card">
        <h3>ZIP 压缩包上传</h3>
        <p class="muted">拖拽或选择 ZIP 文件，完成后自动解析。</p>
        <div
          class="dropzone"
          :class="{ dragging: isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
        >
          <div class="dropzone-title">拖拽 ZIP 文件到这里</div>
          <div class="dropzone-subtitle">或点击下方选择文件</div>
          <input ref="zipInput" type="file" accept=".zip" class="file-input" @change="handleZipSelect" />
        </div>
        <div class="file-row">
          <span class="file-name">{{ zipFile?.name || '未选择文件' }}</span>
          <button class="btn btn-primary" :disabled="!zipFile" @click="handleZipUpload">上传并解析</button>
        </div>
      </div>
    </section>
  </div>
</template>
