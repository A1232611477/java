<template>
  <main class="shell">
    <section class="brand">
      <div class="brand-inner">
        <DoorPortal />
        <p class="eyebrow">IDENTITY V · 第五人格</p>
        <h1 class="brand-title">缔结<br />星之契约</h1>
        <p class="brand-quote">
          “穿过这道门，便不再是孤身一人。<br />
          以星光为印，许你一段新的预言。”
        </p>
        <div class="brand-divider"></div>
        <p class="brand-foot">注册专属身份，开启你的求生篇章。</p>
      </div>
    </section>

    <section class="form-side">
      <div class="card">
        <header class="card-head">
          <h2>注册</h2>
          <p class="sub">已有身份？<RouterLink to="/login" class="link-router">返回登录 →</RouterLink></p>
        </header>

        <form @submit.prevent="onSubmit" novalidate>
          <div class="field">
            <label for="nickname">昵称</label>
            <div class="input-wrap">
              <span class="icon">✧</span>
              <input id="nickname" v-model="nickname" type="text" placeholder="为自己取一个名字" autocomplete="nickname" />
            </div>
            <small class="err">{{ errors.nickname }}</small>
          </div>

          <div class="field">
            <label for="phone">手机号</label>
            <div class="input-wrap">
              <span class="icon">☏</span>
              <input id="phone" v-model="phone" type="tel" placeholder="请输入手机号" autocomplete="tel" maxlength="11" />
            </div>
            <small class="err">{{ errors.phone }}</small>
          </div>

          <div class="field">
            <label for="code">验证码</label>
            <div class="input-wrap">
              <span class="icon">✶</span>
              <input id="code" v-model="code" type="text" placeholder="6 位验证码" maxlength="6" />
              <button type="button" class="toggle code-btn" :disabled="countdown > 0" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
              </button>
            </div>
            <small class="err">{{ errors.code }}</small>
          </div>

          <div class="field">
            <label for="regPassword">密码</label>
            <div class="input-wrap">
              <span class="icon">⚷</span>
              <input
                id="regPassword"
                v-model="pwd"
                :type="showPwd ? 'text' : 'password'"
                placeholder="8-20 位，含字母与数字"
                autocomplete="new-password"
              />
              <button type="button" class="toggle" @click="showPwd = !showPwd">{{ showPwd ? '隐藏' : '显示' }}</button>
            </div>
            <small class="err">{{ errors.regPassword }}</small>
          </div>

          <div class="field">
            <label for="confirm">确认密码</label>
            <div class="input-wrap">
              <span class="icon">⚷</span>
              <input
                id="confirm"
                v-model="confirm"
                :type="showConfirm ? 'text' : 'password'"
                placeholder="请再次输入密码"
                autocomplete="new-password"
              />
              <button type="button" class="toggle" @click="showConfirm = !showConfirm">{{ showConfirm ? '隐藏' : '显示' }}</button>
            </div>
            <small class="err">{{ errors.confirm }}</small>
          </div>

          <label class="agree">
            <input v-model="agree" type="checkbox" />
            <span>我已阅读并同意 <a href="#" class="link">《用户协议》</a> 与 <a href="#" class="link">《隐私政策》</a></span>
          </label>

          <button type="submit" class="btn-primary">缔 约 注 册</button>
        </form>
      </div>
      <p class="copyright">© 第五人格 · 祭司主题演示页 · 原创视觉，非官方</p>
    </section>
  </main>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import DoorPortal from '../components/DoorPortal.vue'
import { useToast } from '../composables/useToast'

const { show } = useToast()
const nickname = ref('')
const phone = ref('')
const code = ref('')
const pwd = ref('')
const confirm = ref('')
const agree = ref(false)
const showPwd = ref(false)
const showConfirm = ref(false)
const countdown = ref(0)
const errors = ref({})
let timer = null

function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(phone.value.trim())) {
    errors.value.phone = '请输入有效的 11 位手机号'
    return
  }
  errors.value.phone = ''
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(timer)
      countdown.value = 0
    }
  }, 1000)
  show('验证码已发送（演示：任意 6 位数字即可）')
}

function onSubmit() {
  errors.value = {}
  let ok = true
  if (!nickname.value.trim()) { errors.value.nickname = '请填写昵称'; ok = false }
  if (!/^1[3-9]\d{9}$/.test(phone.value.trim())) { errors.value.phone = '请输入有效的 11 位手机号'; ok = false }
  if (!/^\d{6}$/.test(code.value.trim())) { errors.value.code = '请输入 6 位验证码'; ok = false }
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,20}$/.test(pwd.value)) {
    errors.value.regPassword = '密码需 8-20 位且含字母与数字'
    ok = false
  }
  if (pwd.value !== confirm.value) { errors.value.confirm = '两次密码不一致'; ok = false }
  if (!agree.value) { show('请先同意用户协议与隐私政策'); ok = false }
  if (!ok) return
  show('星之契约已成 ✦ 欢迎加入祭司的星空')
}

onUnmounted(() => clearInterval(timer))
</script>
