<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true,
  },
})

const statusMap = {
  PARSING: { label: '解析中', tone: 'warning' },
  READY: { label: '已就绪', tone: 'success' },
  FAILED: { label: '失败', tone: 'danger' },
}

const info = computed(() => statusMap[props.status] || { label: props.status, tone: 'default' })
</script>

<template>
  <span class="badge" :class="`badge-${info.tone}`">{{ info.label }}</span>
</template>
