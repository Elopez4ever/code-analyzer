import { ref, onBeforeUnmount } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

// 全局单例：整个应用共享一个 STOMP 连接
let client = null
let connectionPromise = null
const subscriberCount = ref(0)

/**
 * 创建或复用全局 STOMP 客户端
 * @param {{ brokerURL?: string, debug?: boolean }} options
 * @returns {import('@stomp/stompjs').Client}
 */
function getOrCreateClient(options = {}) {
  if (client && client.connected) return client

  const brokerURL = options.brokerURL || '/ws'

  client = new Client({
    webSocketFactory: () => new SockJS(brokerURL),
    debug: options.debug ? console.debug : () => {},
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  })

  client.onConnect = () => {
    console.log('[STOMP] 已连接')
  }

  client.onStompError = (frame) => {
    console.error('[STOMP] 协议错误:', frame.headers?.message || frame)
  }

  client.onWebSocketClose = () => {
    console.log('[STOMP] WebSocket 已关闭')
  }

  return client
}

/**
 * 确保连接已建立，返回 Promise
 */
function ensureConnected(options) {
  const c = getOrCreateClient(options)
  if (c.connected) return Promise.resolve(c)

  if (!connectionPromise) {
    connectionPromise = new Promise((resolve, reject) => {
      c.onConnect = (...args) => {
        console.log('[STOMP] 已连接')
        resolve(c)
        // 恢复原始 onConnect
        c.onConnect = () => {}
      }
      c.onStompError = (frame) => {
        const msg = frame.headers?.message || 'STOMP 协议错误'
        reject(new Error(msg))
        connectionPromise = null
      }
      c.activate()
    })
  }

  return connectionPromise
}

/**
 * 订阅指定 topic，返回 unsubscribe 函数
 */
async function subscribe(topic, callback, options = {}) {
  const c = await ensureConnected(options)
  const sub = c.subscribe(topic, (message) => {
    try {
      const body = JSON.parse(message.body)
      callback(body, message)
    } catch {
      callback(message.body, message)
    }
  })
  subscriberCount.value++
  return () => {
    sub.unsubscribe()
    subscriberCount.value--
  }
}

/**
 * Vue composable：在上传流程中订阅进度
 *
 * 用法：
 * ```js
 * const { subscribeProgress, connected } = useStompClient()
 *
 * // 上传完成后，开始监听进度
 * const unsub = await subscribeProgress('/topic/progress', (msg) => {
 *   console.log(msg) // { taskId, step, percent, message }
 * })
 *
 * // 完成后取消订阅
 * unsub()
 * ```
 */
export function useStompClient() {
  const connected = ref(false)

  const initConnection = async (options = {}) => {
    try {
      await ensureConnected(options)
      connected.value = true
    } catch (err) {
      console.error('[STOMP] 连接失败:', err)
      connected.value = false
    }
  }

  onBeforeUnmount(() => {
    // 不在此处断开全局连接，因为其他组件可能还在使用
  })

  return {
    connected,
    initConnection,
    subscribeProgress: (topic, callback, options) =>
      subscribe(topic, callback, options),
    /**
     * 断开全局连接（应用退出时使用）
     */
    disconnect: () => {
      if (client) {
        client.deactivate()
        client = null
        connectionPromise = null
        connected.value = false
      }
    },
  }
}

export default useStompClient
