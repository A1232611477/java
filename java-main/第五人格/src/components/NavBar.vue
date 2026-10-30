<template>
  <header class="navbar">
    <div class="nav-inner">
      <!-- 品牌 -->
      <div class="nav-brand" @click="onNav('首页')">
        <span class="nav-emblem">祭</span>
        <span class="nav-word">第五人格</span>
      </div>

      <!-- 移动端菜单开关 -->
      <button class="nav-toggle" type="button" @click="menuOpen = !menuOpen">☰</button>

      <!-- 导航菜单 -->
      <nav class="nav-menu" :class="{ open: menuOpen }">
        <a
          v-for="item in menus"
          :key="item"
          class="nav-link"
          :class="{ active: item === '首页' }"
          @click="onNav(item)"
        >{{ item }}</a>
      </nav>

      <!-- 右侧：搜索栏 + 用户区 -->
      <div class="nav-right">
        <form class="nav-search" @submit.prevent="doSearch">
          <span class="nav-search-icon">⌕</span>
          <input v-model="query" type="text" placeholder="搜索角色、资讯、攻略…" />
          <button type="submit" class="nav-search-btn">搜索</button>
        </form>

        <!-- 后台管理系统入口：仅登录页，无注册 -->
        <RouterLink to="/admin/login" class="nav-admin">后台管理</RouterLink>

        <div v-if="user" class="nav-user" @click="userOpen = !userOpen">
          <span class="nav-avatar">{{ initial }}</span>
          <span class="nav-nick">{{ user.nickname }}</span>
        </div>
        <RouterLink v-else to="/login" class="nav-login">登录</RouterLink>
      </div>
    </div>
  </header>

  <div v-if="userOpen" class="nav-backdrop" @click="userOpen = false"></div>

  <!-- 个人中心浮层卡片（点击头像弹出） -->
  <Transition name="drop">
    <div v-if="userOpen && user" class="user-panel" @click.stop>
      <div class="up-head">
        <span class="up-avatar">{{ initial }}</span>
        <div class="up-id">
          <p class="up-nick">{{ user.nickname }}</p>
          <p class="up-uid">UID · {{ user.id ?? '—' }}</p>
        </div>
        <span class="up-badge" :class="user.status === 1 ? 'off' : 'on'">{{ user.status === 1 ? '已禁用' : '在线' }}</span>
      </div>

      <div class="up-body">
        <div class="up-row">
          <span class="up-k">账号状态</span>
          <span class="up-v">{{ statusText }}</span>
        </div>
        <div class="up-row">
          <span class="up-k">注册时间</span>
          <span class="up-v">{{ createdAt }}</span>
        </div>
        <div class="up-row token">
          <span class="up-k">登录令牌</span>
          <span class="up-token">{{ shortToken }}</span>
        </div>
      </div>

      <div class="up-foot">
        <button type="button" class="up-btn primary" :disabled="loading" @click="onVerify">
          {{ loading ? '校验中…' : '校验登录态' }}
        </button>
        <button type="button" class="up-btn ghost" @click="onLogout">退出登录</button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../stores/auth'
import { useToast } from '../composables/useToast'
import { ApiError } from '../api/http'

const emit = defineEmits(['search', 'navigate'])

const router = useRouter()
const { show } = useToast()
const { user, token, loading, refresh, signOut } = useAuth()

const menus = ['首页', '角色图鉴', '资讯公告', '玩法攻略', '赛事中心', '玩家社区', '周边商城']
const query = ref('')
const menuOpen = ref(false)
const userOpen = ref(false)

const initial = computed(() => (user.value?.nickname || '旅').slice(0, 1))
const statusText = computed(() => (user.value?.status === 1 ? '已禁用' : '正常'))
const createdAt = computed(() =>
  user.value?.createdAt ? String(user.value.createdAt).replace('T', ' ').slice(0, 19) : '—'
)
const shortToken = computed(() =>
  token.value ? `${token.value.slice(0, 20)}……${token.value.slice(-8)}` : '（无令牌）'
)

async function onVerify() {
  try {
    await refresh()
    show('登录态有效 ✦ 服务端已确认你的身份')
  } catch (e) {
    show(e instanceof ApiError ? e.message : '校验失败')
    if (e instanceof ApiError && e.code === 401) {
      userOpen.value = false
      router.replace({ name: 'login' })
    }
  }
}

function doSearch() {
  const q = query.value.trim()
  if (!q) {
    show('请输入要搜索的内容')
    return
  }
  menuOpen.value = false
  emit('search', q)
}

function onNav(item) {
  menuOpen.value = false
  emit('navigate', item)
}

function onLogout() {
  userOpen.value = false
  signOut()
  show('已退出，门已关闭。')
  router.replace({ name: 'login' })
}
</script>
