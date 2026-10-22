<template>
  <main class="shell">
    <section class="brand">
      <div class="brand-inner">
        <DoorPortal />
        <p class="eyebrow">IDENTITY V · 第五人格</p>
        <h1 class="brand-title">推开<br />命运之门</h1>
        <p class="brand-quote">
          “门，是连接两个世界的约定。<br />
          在深渊的尽头，仍有星光为我引路。”
        </p>
        <div class="brand-divider"></div>
        <p class="brand-foot">星之预言已落，只待你的归来。</p>
      </div>
    </section>

    <section class="form-side">
      <div class="card">
        <header class="card-head">
          <h2>登录</h2>
          <p class="sub">尚未缔结契约？<RouterLink to="/register" class="link-router">注册新身份 →</RouterLink></p>
        </header>

        <form @submit.prevent="onSubmit" novalidate>
          <div class="field">
            <label for="account">账号</label>
            <div class="input-wrap">
              <span class="icon">✦</span>
              <input id="account" v-model="account" type="text" placeholder="请输入账号" autocomplete="username" />
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

          <div class="row">
            <label class="remember" title="勾选后 7 天内免登录；不勾则关闭浏览器后需要重新登录">
              <input v-model="remember" type="checkbox" /> <span>记住我</span>
            </label>
            <a href="#" class="link">忘记密码？</a>
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '穿 越 中 …' : '穿 越 之 门' }}
          </button>
        </form>

        <div class="divider"><span>或以星辉相连</span></div>
        <div class="socials">
          <button type="button" class="social" @click="onSocial('微信')">微信</button>
          <button type="button" class="social" @click="onSocial('QQ')">QQ</button>
          <button type="button" class="social" @click="onSocial('网易')">网易</button>
        </div>
      </div>
      <p class="copyright">© 第五人格 · 祭司主题演示页 · 原创视觉，非官方</p>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DoorPortal from '../components/DoorPortal.vue'
import { useToast } from '../composables/useToast'
import { useAuth } from '../stores/auth'
import { ApiError } from '../api/http'

const router = useRouter()
const route = useRoute()
const { show } = useToast()
const { loading, signIn } = useAuth()

const account = ref(
  // 注册成功后跳过来会带 ?nickname=，直接帮用户把账号填好
  typeof route.query.nickname === 'string' ? route.query.nickname : ''
)
const password = ref('')
const remember = ref(true)
const showPwd = ref(false)
const errors = ref({})

// 刚注册完跳过来：给一句提示
if (typeof route.query.nickname === 'string') {
  show('注册成功，请输入密码登录 ✦')
}

async function onSubmit() {
  errors.value = {}
  let ok = true
  if (!account.value.trim()) { errors.value.account = '请输入账号'; ok = false }
  if (!password.value) { errors.value.password = '请输入密码'; ok = false }
  else if (password.value.length < 6) { errors.value.password = '密码至少 6 位'; ok = false }
  if (!ok) return

  try {
    // 记住我：勾选 → 令牌进 localStorage（后端发 7 天长效令牌）；不勾 → sessionStorage（关浏览器即失效）
    await signIn({ nickname: account.value.trim(), password: password.value, rememberMe: remember.value })
    show('门已开启，星辉为你引路…欢迎归来。')
    // 登录前想去哪，登录后就回哪
    const back = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    router.replace(back)
  } catch (e) {
    if (!(e instanceof ApiError)) {
      show('登录失败，请稍后重试')
      return
    }
    // 401 = 账号或密码错误，挂在密码框下；403 = 账号被禁用
    if (e.code === 401 || e.code === 403) errors.value.password = e.message
    else if (e.code === -1) errors.value.account = e.message
    show(e.message)
  }
}

function onSocial(name) {
  show(`已唤起 ${name} 授权（演示）`)
}
</script>
