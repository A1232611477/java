import { ref } from 'vue'

// 模块级共享状态：同一时刻只有一个页面挂载，跨组件复用同一个 Toast
const message = ref('')
const visible = ref(false)
let timer = null

export function useToast() {
  function show(msg) {
    message.value = msg
    visible.value = true
    clearTimeout(timer)
    timer = setTimeout(() => {
      visible.value = false
    }, 2600)
  }
  return { message, visible, show }
}
