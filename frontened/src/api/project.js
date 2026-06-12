const BASE = '/api'

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })
  const data = await res.json()
  if (!res.ok || data.code !== 200) {
    throw new Error(data.message || `请求失败 (${res.status})`)
  }
  return data.data
}

export const projectsApi = {
  getList: (page, size) =>
    request(`/project/?page=${page}&size=${size}`),

  createFromGit: (gitUrl, name) =>
    request('/project/', {
      method: 'POST',
      body: JSON.stringify({ gitUrl, name, uploadMethod: 1 }),
    }),

  createFromZip: (formData) =>
    fetch(`${BASE}/project/upload`, { method: 'POST', body: formData })
      .then(async (res) => {
        const data = await res.json()
        if (!res.ok || data.code !== 200) throw new Error(data.message || `上传失败 (${res.status})`)
        return data.data
      }),

  remove: (id) =>
    request(`/project/${id}`, { method: 'DELETE' }),

  retry: (id) =>
    request(`/project/${id}/retry`, { method: 'POST' }),
}
