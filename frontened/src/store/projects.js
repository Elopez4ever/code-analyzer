import { computed, reactive, readonly } from 'vue'

const STORAGE_KEY = 'code-analyzer-projects'
const parsingTimers = new Map()

const state = reactive({
  items: loadProjects(),
})

function loadProjects() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored ? JSON.parse(stored) : []
  } catch {
    return []
  }
}

function persist() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items))
}

function createId() {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
}

function deriveNameFromGit(url) {
  const trimmed = url.trim().replace(/\/+$/, '')
  const segment = trimmed.split('/').pop() || 'git-project'
  return segment.replace(/\.git$/i, '') || 'git-project'
}

function deriveNameFromZip(fileName) {
  return fileName.replace(/\.zip$/i, '') || 'zip-project'
}

function updateProject(id, updates) {
  const index = state.items.findIndex((item) => item.id === id)
  if (index === -1) return
  state.items[index] = { ...state.items[index], ...updates, updatedAt: new Date().toISOString() }
  persist()
}

function scheduleParsing(id) {
  if (parsingTimers.has(id)) {
    clearTimeout(parsingTimers.get(id))
  }
  const timer = setTimeout(() => {
    updateProject(id, { status: 'READY' })
    parsingTimers.delete(id)
  }, 2400)
  parsingTimers.set(id, timer)
}

function addProject(project) {
  state.items.push(project)
  persist()
  scheduleParsing(project.id)
}

function addProjectFromGit(url, customName) {
  const name = customName?.trim() || deriveNameFromGit(url)
  const project = {
    id: createId(),
    name,
    source: url,
    sourceType: 'Git',
    status: 'PARSING',
    updatedAt: new Date().toISOString(),
  }
  addProject(project)
}

function addProjectFromZip(file, customName) {
  const name = customName?.trim() || deriveNameFromZip(file.name)
  const project = {
    id: createId(),
    name,
    source: file.name,
    sourceType: 'ZIP',
    status: 'PARSING',
    updatedAt: new Date().toISOString(),
  }
  addProject(project)
}

function retryProject(id) {
  updateProject(id, { status: 'PARSING' })
  scheduleParsing(id)
}

function removeProject(id) {
  const index = state.items.findIndex((item) => item.id === id)
  if (index === -1) return
  state.items.splice(index, 1)
  persist()
}

function getProjectById(id) {
  return state.items.find((item) => item.id === id)
}

state.items.filter((item) => item.status === 'PARSING').forEach((item) => scheduleParsing(item.id))

export function useProjects() {
  const projects = computed(() => state.items)
  const hasProjects = computed(() => state.items.length > 0)
  return {
    projects: readonly(projects),
    hasProjects,
    addProjectFromGit,
    addProjectFromZip,
    retryProject,
    removeProject,
    getProjectById,
  }
}
