<script setup>
import { computed } from 'vue'
import { useProjects } from '../store/projects'
import StatusBadge from '../components/StatusBadge.vue'
import { formatDateTime } from '../utils/format'

const { projects, removeProject, retryProject, hasProjects } = useProjects()

const sortedProjects = computed(() =>
  [...projects.value].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)),
)
</script>

<template>
  <div class="page project-list-page">
    <div class="panel">
      <div class="panel-header">
        <h2 class="panel-title">项目列表</h2>
      </div>

      <div v-if="!hasProjects" class="empty-state">
        <p class="muted">暂无项目，先在上传页提交 Git URL 或 ZIP 文件。</p>
      </div>

      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>项目名</th>
              <th>来源</th>
              <th>类型</th>
              <th>状态</th>
              <th>更新时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="project in sortedProjects" :key="project.id">
              <td class="col-name">{{ project.name }}</td>
              <td class="col-source">{{ project.source }}</td>
              <td>
                <span class="pill">{{ project.sourceType }}</span>
              </td>
              <td>
                <StatusBadge :status="project.status" />
              </td>
              <td class="col-time">{{ formatDateTime(project.updatedAt) }}</td>
              <td class="col-actions">
                <RouterLink
                  v-if="project.status === 'READY'"
                  class="btn btn-outline btn-accent btn-xs"
                  :to="`/chat/${project.id}`"
                >
                  问答
                </RouterLink>
                <span v-else class="btn btn-outline btn-xs disabled">问答</span>
                <button
                  v-if="project.status === 'FAILED'"
                  class="btn btn-outline btn-xs"
                  @click="retryProject(project.id)"
                >
                  重试
                </button>
                <button
                  class="btn btn-outline btn-xs btn-danger"
                  @click="removeProject(project.id)"
                >
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.project-list-page {
  max-width: none;
  padding: 24px;
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
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  margin: 0;
}

.table-wrap {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table thead {
  background: var(--surface-muted);
}

.data-table th {
  text-align: left;
  padding: 10px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.4px;
  white-space: nowrap;
  border-bottom: 1px solid var(--border);
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  white-space: nowrap;
}

.data-table tbody tr:hover {
  background: var(--surface-muted);
}

.col-name {
  font-weight: 600;
}

.col-source {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--muted);
}

.col-time {
  color: var(--muted);
  font-size: 12px;
}

.col-actions {
  text-align: right;
  white-space: nowrap;
}

td.col-actions {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
  align-items: center;
}

.btn-xs {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 6px;
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text);
}

.btn-outline:hover {
  background: var(--surface-muted);
}

.btn-accent {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-accent:hover {
  background: rgba(91, 108, 255, 0.08);
}

.btn-danger {
  border-color: rgba(239, 68, 68, 0.5);
  color: var(--danger);
}

.btn-danger:hover {
  background: rgba(239, 68, 68, 0.08);
}

.empty-state {
  padding: 64px 32px;
  text-align: center;
}

.pill {
  padding: 3px 10px;
  font-size: 11px;
}

@media (max-width: 768px) {
  .project-list-page {
    padding: 16px;
  }

  .data-table th,
  .data-table td {
    padding: 10px 12px;
  }
}
</style>