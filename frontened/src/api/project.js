import { showErrorToast } from '../utils/toast'

const BASE = '/api'

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })
  const json = await res.json()

  // Trigger toast based on response code
  if (json.code !== 200) {
    showErrorToast(json.message || `请求失败 (${res.status})`)
  }

  if (!res.ok || json.code !== 200) {
    throw new Error(json.message || `请求失败 (${res.status})`)
  }
  // Prefer the `data` envelope; fall back to full JSON when backend omits it
  return json.data ?? json
}

export const projectsApi = {
  getList: (page, size, projectName) => {
    let url = `/project/?page=${page}&size=${size}`
    if (projectName) url += `&projectName=${encodeURIComponent(projectName)}`
    return request(url)
  },

  createFromGit: (gitUrl, name) =>
    request('/project/create', {
      method: 'POST',
      body: JSON.stringify({ gitUrl, name, uploadMethod: 1 }),
    }),

  createFromZip: (formData) =>
    fetch(`${BASE}/project/upload`, { method: 'POST', body: formData })
      .then(async (res) => {
        const data = await res.json()

        // Trigger toast based on response code
        if (data.code !== 200) {
          showErrorToast(data.message || `上传失败 (${res.status})`)
        }

        if (!res.ok || data.code !== 200) throw new Error(data.message || `上传失败 (${res.status})`)
        return data.data ?? data
      }),

  remove: (id) =>
    request(`/project/${id}`, { method: 'DELETE' }),

  batchRemove: (projectIds) =>
    request('/project/delete', {
      method: 'POST',
      body: JSON.stringify({ projectIds }),
    }),

  retry: (id) =>
    request(`/project/${id}/retry`, { method: 'POST' }),

  update: (id, payload) =>
    request(`/project/modify`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
}