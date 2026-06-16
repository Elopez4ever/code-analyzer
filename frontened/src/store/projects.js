// src/store/projects.js (or .ts)
import { computed, reactive } from 'vue'
import { projectsApi } from '../api/project'

const STATUS_MAP = {
  '0': 'PARSING',
  '1': 'READY',
  '-1': 'FAILED',
}

const SOURCE_TYPE_MAP = {
  0: 'ZIP',
  1: 'GIT',
}

let searchTimer = null

const state = reactive({
  items: [],
  total: 0,
  currentPage: 1,
  pageSize: 10,
  pages: 0,
  loading: false,
  error: null,
  projectName: '',
  fetchCounter: 0,
})

function normalizeProject(raw) {
  return {
    id: raw.projectId,
    name: raw.name,
    status: STATUS_MAP[String(raw.status)] ?? 'PARSING',
    sourceType: SOURCE_TYPE_MAP[raw.uploadMethod] ?? 'ZIP',
    source: raw.gitUrl ?? '暂未配置',
    updatedAt: raw.updatedAt,
  }
}

async function fetchProjects(page = 1) {
  state.fetchCounter += 1
  state.loading = true
  state.error = null
  try {
    const { records, total, current, pages } = await projectsApi.getList(
      page,
      state.pageSize,
      state.projectName || undefined,
    )
    state.items = records.map(normalizeProject)
    state.total = total
    state.currentPage = current
    state.pages = pages
  } catch (err) {
    state.error = err.message
  } finally {
    state.loading = false
  }
}

function setProjectName(name, immediate = false) {
  state.projectName = name
  clearTimeout(searchTimer)
  if (immediate) {
    fetchProjects(1)
  } else {
    searchTimer = setTimeout(() => {
      fetchProjects(1)
    }, 300)
  }
}

async function addProjectFromGit(gitUrl, name) {
  try {
    const raw = await projectsApi.createFromGit(gitUrl, name)
    state.items.unshift(normalizeProject(raw))
    state.total += 1
    return { success: true }
  } catch (err) {
    state.error = err.message
    return { success: false, message: err.message }
  }
}

async function addProjectFromZip(file, name) {
  const formData = new FormData()
  formData.append('file', file)
  if (name) formData.append('name', name)
  try {
    const raw = await projectsApi.createFromZip(formData)
    state.items.unshift(normalizeProject(raw))
    state.total += 1
    return { success: true }
  } catch (err) {
    state.error = err.message
    return { success: false, message: err.message }
  }
}

async function removeProject(id) {
  try {
    await projectsApi.remove(id)
    const idx = state.items.findIndex((p) => p.id === id)
    if (idx !== -1) {
      state.items.splice(idx, 1)
      state.total -= 1
    }
  } catch (err) {
    state.error = err.message
  }
}

async function batchRemoveProject(ids) {
  try {
    await projectsApi.batchRemove(ids)
    state.items = state.items.filter((p) => !ids.includes(p.id))
    state.total = state.items.length
  } catch (err) {
    state.error = err.message
    throw err
  }
}

async function retryProject(id) {
  try {
    await projectsApi.retry(id)
    const project = state.items.find((p) => p.id === id)
    if (project) project.status = 'PARSING'
  } catch (err) {
    state.error = err.message
  }
}

async function updateProject(id, payload) {
  try {
    const raw = await projectsApi.update(id, payload)
    const idx = state.items.findIndex((p) => p.id === id)
    if (idx !== -1) {
      state.items[idx] = normalizeProject(raw)
    }
  } catch (err) {
    state.error = err.message
    throw err
  }
}

function getProjectById(id) {
  return state.items.find((p) => String(p.id) === String(id))
}

export function useProjects() {
  return {
    projects: computed(() => state.items),
    sortedProjects: computed(() =>
      [...state.items].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)),
    ),
    hasProjects: computed(() => state.items.length > 0),
    loading: computed(() => state.loading),
    error: computed(() => state.error),
    currentPage: computed(() => state.currentPage),
    totalPages: computed(() => state.pages),
    total: computed(() => state.total),
    fetchProjects,
    setProjectName,
    projectName: computed(() => state.projectName),
    fetchCounter: computed(() => state.fetchCounter),
    addProjectFromGit,
    addProjectFromZip,
    removeProject,
    batchRemoveProject,
    retryProject,
    updateProject,
    getProjectById,
  }
}