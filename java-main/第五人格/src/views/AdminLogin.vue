<template>
  <main class="shell">
    <!-- 星空 / 流星叠加在立绘背景之上、文字之下 -->
    <NightSky />
    <section class="brand">
      <div class="brand-inner">
        <DoorPortal />
        <p class="eyebrow">IDENTITY V · 后台管理</p>
        <h1 class="brand-title">管理之门</h1>
        <p class="brand-quote">“仅授权管理员可入此门，<br />打理门户精选，守护星辉归途。”</p>
        <div class="brand-divider"></div>
        <p class="brand-foot">门后有山河，亦有责任。</p>
      </div>
    </section>

    <section class="form-side">
      <div class="card">
        <header class="card-head">
          <h2>管理员登录</h2>
          <p class="sub">登录后台管理系统，维护首页展示的内容。</p>
        </header>

        <form @submit.prevent="onSubmit" novalidate>
          <div class="field">
            <label for="account">管理员账号</label>
            <div class="input-wrap">
              <span class="icon">✦</span>
              <input id="account" v-model="account" type="text" placeholder="请输入管理员账号" autocomplete="username" />
            </div>
            <small class="err">{{ errors.account }}</small>
          </div>

          <div class="field">
            <label for="password">密码</label>
            <div class="input-wrap">
              <span class="icon">⚷</span>
              <input
                id="password"
                v-model="password"
                :type="showPwd ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
              <button type="button" class="toggle" @click="showPwd = !showPwd">{{ showPwd ? '隐藏' : '显示' }}</button>
            </div>
            <small class="err">{{ errors.password }}</small>
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '验 证 中 …' : '进 入 后 台' }}
          </button>
        </form>

        <div class="divider"><span>非管理员请勿尝试</span></div>
        <div class="admin-hint">本系统仅用于内容管理，无注册入口。</div>
      </div>
      <p class="copyright">© 第五人格 · 祭司主题演示页 · 原创视觉，非官方</p>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DoorPortal from '../components/DoorPortal.vue'
import NightSky from '../components/NightSky.vue'
import { useToast } from '../composables/useToast'
import { useAdminAuth } from '../stores/adminAuth'
import { ApiError } from '../api/http'

const router = useRouter()
const route = useRoute()
const { show } = useToast()
const { loading, signIn } = useAdminAuth()

const account = ref('')
const password = ref('')
const showPwd = ref(false)
const errors = ref({})

async function onSubmit() {
  errors.value = {}
  let ok = true
  if (!account.value.trim()) { errors.value.account = '请输入管理员账号'; ok = false }
  if (!password.value) { errors.value.password = '请输入密码'; ok = false }
  else if (password.value.length < 6) { errors.value.password = '密码至少 6 位'; ok = false }
  if (!ok) return

  try {
    await signIn({ nickname: account.value.trim(), password: password.value, rememberMe: true })
    show('门已开启，欢迎回到管理中枢。')
    const back = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin'
    router.replace(back)
  } catch (e) {
    if (!(e instanceof ApiError)) {
      show('登录失败，请稍后重试')
      return
    }
    if (e.code === 401 || e.code === 403) errors.value.password = e.message
    else if (e.code === -1) errors.value.account = e.message
    show(e.message)
  }
}
</script>
