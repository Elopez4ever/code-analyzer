<template>
  <div class="project-list-page">
    <div class="panel">
      <div class="panel-header">
        <h2 class="panel-title">项目列表</h2>
      </div>

      <div v-if="error" class="empty-state">
        <p class="muted">加载失败：{{ error }}</p>
        <button class="btn btn-outline btn-sm" style="margin-top:12px" @click="fetchProjects(currentPage)">
          重试
        </button>
      </div>

      <template v-else>
        <div class="table-wrapper">
          <a-table
            :columns="columns"
            :data-source="sortedProjects"
            :loading="loading"
            :pagination="false"
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
                  <RouterLink :to="`/project/${record.id}`" class="btn btn-outline btn-sm">
                    详情
                  </RouterLink>
                  <button
                    class="btn btn-outline btn-danger btn-sm"
                    @click="removeProject(record.id)"
                  >
                    删除
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

        <div class="pagination-wrap">
          <a-pagination
            v-model:current="currentPage"
            :total="total"
            :page-size="pageSize"
            show-quick-jumper
            show-size-changer
            :show-total="(total) => `共 ${total} 个项目`"
            @change="fetchProjects"
            @showSizeChange="onPageSizeChange"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useProjects } from '../store/projects'
import StatusBadge from '../components/StatusBadge.vue'
import { formatDateTime } from '../utils/format'

const {
  sortedProjects,
  loading,
  error,
  currentPage,
  totalPages,
  total,
  fetchProjects,
  removeProject,
  retryProject,
} = useProjects()

const pageSize = ref(10)

function onPageSizeChange(page, size) {
  pageSize.value = size
  fetchProjects(1)
}

const columns = [
  { title: '项目名', dataIndex: 'name', key: 'name', width: 160 },
  { title: '来源', dataIndex: 'source', key: 'source', ellipsis: true, width: 300 },
  { title: '类型', dataIndex: 'sourceType', key: 'sourceType', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 160 },
  { title: '操作', key: 'actions', width: 180 },
]

onMounted(() => fetchProjects(1))
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
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;              /* 头部不缩放 */
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  margin: 0;
}

/* 表格容器：不滚动，自然高度 */
.table-wrapper {
  overflow: visible;
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

/* 空状态/错误提示 */
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

/* 操作按钮：单行排列 */
.action-row {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
}

/* 分页固定底部 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 24px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
  background: var(--surface);
}
</style>