import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import { hasToken } from '../api/http'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'login', component: Login, meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: Register, meta: { guestOnly: true } },
  { path: '/home', name: 'home', component: Home, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：没有令牌不能进 /home；已登录就不用再看登录 / 注册页
router.beforeEach((to) => {
  const logged = hasToken()
  if (to.meta.requiresAuth && !logged) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && logged) {
    return { name: 'home' }
  }
  return true
})

export default router
