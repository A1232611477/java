import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import AdminLogin from '../views/AdminLogin.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminContent from '../views/admin/AdminContent.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import { hasToken, getAdminToken } from '../api/http'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'login', component: Login, meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: Register, meta: { guestOnly: true } },
  { path: '/home', name: 'home', component: Home, meta: { requiresAuth: true } },

  // ============ 后台管理系统 ============
  // 仅登录页（无注册入口）；其余接口需管理员令牌
  { path: '/admin/login', name: 'admin-login', component: AdminLogin, meta: { guestOnlyAdmin: true } },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: { name: 'admin-content' } },
      { path: 'dashboard', name: 'admin-dashboard', component: AdminDashboard },
      { path: 'content', name: 'admin-content', component: AdminContent }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：
// - 普通用户页：未登录跳登录
// - 后台页：无管理员令牌跳后台登录；已登录管理员访问登录页则直接进后台
router.beforeEach((to) => {
  const userLogged = hasToken()
  const adminLogged = getAdminToken()

  if (to.meta.requiresAuth && !userLogged) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && userLogged) {
    return { name: 'home' }
  }
  if (to.meta.requiresAdmin && !adminLogged) {
    return { name: 'admin-login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnlyAdmin && adminLogged) {
    return { name: 'admin-content' }
  }
  return true
})

export default router
