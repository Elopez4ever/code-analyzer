import { ref } from 'vue'

// Global reactive toast state – one stack, managed in App.vue ToastContainer
const toasts = ref([])

let _id = 0
const DEFAULT_DURATION = 4000

/**
 * Show a toast notification.
 * @param {'success'|'error'} type
 * @param {string} message
 * @param {number} [duration]  ms, default 4000
 */
export function showToast(type, message, duration = DEFAULT_DURATION) {
  const id = ++_id
  toasts.value.push({ id, type, message })

  if (duration > 0) {
    setTimeout(() => {
      dismissToast(id)
    }, duration)
  }

  return id
}

export function showSuccessToast(message, duration) {
  return showToast('success', message, duration)
}

export function showErrorToast(message, duration) {
  return showToast('error', message, duration)
}

export function dismissToast(id) {
  const idx = toasts.value.findIndex((t) => t.id === id)
  if (idx !== -1) {
    toasts.value.splice(idx, 1)
  }
}

/**
 * Reactive reference to the current list of toasts.
 * Components that render toasts should use this.
 */
export function useToasts() {
  return { toasts, showToast, showSuccessToast, showErrorToast, dismissToast }
}
