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

        <div v-if="user" class="nav-user" @click="userOpen = !userOpen">
          <span class="nav-avatar">{{ initial }}</span>
          <span class="nav-nick">{{ user.nickname }}</span>
          <div v-if="userOpen" class="nav-user-menu" @click.stop>
            <button type="button" @click="onCenter">个人中心</button>
            <button type="button" @click="onLogout">退出登录</button>
          </div>
        </div>
        <RouterLink v-else to="/login" class="nav-login">登录</RouterLink>
      </div>
    </div>
  </header>

  <div v-if="userOpen" class="nav-backdrop" @click="userOpen = false"></div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../stores/auth'
import { useToast } from '../composables/useToast'

const emit = defineEmits(['search', 'navigate'])

const router = useRouter()
const { show } = useToast()
const { user, signOut } = useAuth()

const menus = ['首页', '角色图鉴', '资讯公告', '玩法攻略', '赛事中心', '玩家社区', '周边商城']
const query = ref('')
const menuOpen = ref(false)
const userOpen = ref(false)

const initial = computed(() => (user.value?.nickname || '旅').slice(0, 1))

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

function onCenter() {
  userOpen.value = false
  emit('navigate', '个人中心')
}

function onLogout() {
  userOpen.value = false
  signOut()
  show('已退出，门已关闭。')
  router.replace({ name: 'login' })
}
</script>
