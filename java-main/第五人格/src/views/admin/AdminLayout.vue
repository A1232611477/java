<template>
  <div class="admin-shell">
    <aside class="admin-side">
      <div class="admin-brand">
        <span class="admin-emblem">祭</span>
        <span class="admin-brand-name">后台管理</span>
      </div>
      <nav class="admin-navs">
        <RouterLink to="/admin/dashboard" class="admin-nav" :class="{ active: $route.name === 'admin-dashboard' }">仪表盘</RouterLink>
        <RouterLink to="/admin/content" class="admin-nav" :class="{ active: $route.name === 'admin-content' }">内容管理</RouterLink>
        <RouterLink to="/home" class="admin-nav ghost">返回首页</RouterLink>
      </nav>
      <div class="admin-side-foot">第五人格 · 内容中枢</div>
    </aside>

    <div class="admin-main">
      <header class="admin-top">
        <div class="admin-crumb">{{ crumb }}</div>
        <div class="admin-user">
          <span class="admin-avatar">{{ initial }}</span>
          <span class="admin-nick">{{ user?.nickname || '管理员' }}</span>
          <button class="admin-logout" @click="onLogout">退出登录</button>
        </div>
      </header>
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminAuth } from '../../stores/adminAuth'
import { useToast } from '../../composables/useToast'

const route = useRoute()
const router = useRouter()
const { show } = useToast()
const { user, signOut } = useAdminAuth()

const crumb = computed(() => (route.name === 'admin-dashboard' ? '仪表盘' : '内容管理'))
const initial = computed(() => (user.value?.nickname || '管').slice(0, 1))

function onLogout() {
  signOut()
  show('已退出后台登录')
  router.replace({ name: 'admin-login' })
}
</script>
