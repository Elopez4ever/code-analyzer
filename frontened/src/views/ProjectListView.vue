<template>
  <div class="project-list-page">
    <div class="panel">
      <div class="panel-header">
        <h2 class="panel-title">项目列表</h2>
        <button
          v-if="selectedIds.length > 0"
          class="btn btn-danger batch-delete-btn"
          @click="handleBatchDelete"
        >
          批量删除 ({{ selectedIds.length }})
        </button>
      </div>

      <div v-if="error" class="empty-state">
        <p class="muted">加载失败：{{ error }}</p>
        <button class="btn btn-outline btn-sm" style="margin-top:12px" @click="fetchProjects(currentPage)">
          重试
        </button>
      </div>

      <template v-else>
        <div class="table-wrapper">
          <transition name="page-fade">
            <div :key="currentPage" class="table-transition-inner">
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
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
  updateProject,
} = useProjects()

const selectedIds = ref([])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedIds.value,
  onChange: (keys) => {
    selectedIds.value = keys
  },
}))

async function handleBatchDelete() {
  const ids = [...selectedIds.value]
  for (const id of ids) {
    await removeProject(id)
  }
  selectedIds.value = []
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
</style>