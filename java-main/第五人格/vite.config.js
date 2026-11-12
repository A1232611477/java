import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // sockjs-client 在浏览器里引用了 Node 的 global，Vite 下必须映射为 globalThis，
  // 否则 Friends.vue（私信聊天）的导入链会整体报错导致全站白屏
  define: {
    global: 'globalThis'
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: true,
    // 好友联机 WebSocket（/ws）走相对路径，代理到后端 8081，跨设备也能用
    proxy: {
      '/ws': {
        target: 'http://192.168.68.35:8081',
        ws: true,
        changeOrigin: true
      }
    }
  }
})
