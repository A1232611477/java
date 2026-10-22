<template>
  <div class="home">
    <!-- 导航栏（含搜索栏） -->
    <NavBar @search="onSearch" @navigate="onNavigate" />

    <main class="home-main">
      <!-- 主视觉 -->
      <section class="hero">
        <p class="eyebrow">IDENTITY V · 第五人格</p>
        <h1 class="hero-title">命运之门，为你而启</h1>
        <p class="hero-sub">在监管与求生交织的迷局之中，星辉始终是归途的指引。</p>

        <div class="hero-chips">
          <button
            v-for="c in CHIPS"
            :key="c"
            type="button"
            class="chip"
            @click="onChip(c)"
          >{{ c }}</button>
        </div>
      </section>

      <!-- 搜索结果（输入后展示） -->
      <section v-if="searched" class="section">
        <h2 class="section-title">搜索结果：{{ query }}（共 {{ total }} 条）</h2>
        <div v-if="results.length" class="card search-card">
          <ul class="result-list">
            <li v-for="r in results" :key="r.id" class="result-item" @click="onChip(r.category)">
              <span class="result-cat">{{ r.subCategory || r.category }}</span>
              <div class="result-body">
                <p class="result-title">{{ r.title }}</p>
                <p class="result-desc">{{ r.desc }}</p>
              </div>
            </li>
          </ul>
        </div>
        <p v-else class="empty">未找到与「{{ query }}」相关的内容，换个关键词试试。</p>
      </section>

      <!-- 精选内容（来自后端首页接口） -->
      <section class="section">
        <h2 class="section-title">门户精选</h2>
        <div class="grid">
          <div
            v-for="f in featured"
            :key="f.id"
            class="card feature"
            @click="onSearchCategory(f.category)"
          >
            <div class="f-icon">{{ (f.title || '·').charAt(0) }}</div>
            <h3 class="f-title">{{ f.title }}</h3>
            <p class="f-desc">{{ f.desc }}</p>
            <span class="f-go">{{ f.subCategory || f.category }} →</span>
          </div>
          <p v-if="!featured.length" class="empty">精选内容加载中…</p>
        </div>
      </section>

      <!-- 账户面板（保留原登录态校验功能） -->
      <section class="section">
        <h2 class="section-title">账户 · 星之契约</h2>
        <div class="card account-card">
          <div class="info-list">
            <div class="info-row">
              <span class="k">用户 ID</span>
              <span class="v">{{ user?.id ?? '—' }}</span>
            </div>
            <div class="info-row">
              <span class="k">昵称</span>
              <span class="v">{{ user?.nickname || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="k">账号状态</span>
              <span class="v">{{ statusText }}</span>
            </div>
            <div class="info-row">
              <span class="k">注册时间</span>
              <span class="v">{{ createdAt }}</span>
            </div>
          </div>

          <div class="field">
            <label>登录令牌（请求自动携带）</label>
            <div class="token-box">{{ shortToken }}</div>
          </div>

          <div class="btn-row">
            <button type="button" class="btn-primary" :disabled="loading" @click="onRefresh">
              {{ loading ? '校 验 中 …' : '校 验 登 录 态' }}
            </button>
            <button type="button" class="btn-ghost" @click="onLogout">退出登录</button>
          </div>

          <p class="hint">
            校验登录态会携带令牌请求后端受保护接口 <code>GET /api/user/me</code>，确认身份仍然有效。
          </p>
        </div>
      </section>
    </main>

    <footer class="home-footer">© 第五人格 · 祭司主题演示页 · 原创视觉，非官方</footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import { useAuth } from '../stores/auth'
import { useToast } from '../composables/useToast'
import { ApiError } from '../api/http'
import { fetchHome, searchSite } from '../api/search'

const router = useRouter()
const { show } = useToast()
const { user, token, loading, refresh, signOut } = useAuth()

// 首页数据（来自后端 /api/home）
const categories = ref([])   // 一级分类（含 children 二级）
const featured = ref([])     // 精选内容

// 搜索状态
const query = ref('')
const results = ref([])
const searched = ref(false)
const total = ref(0)

onMounted(async () => {
  try {
    const home = await fetchHome()
    categories.value = home.categories || []
    featured.value = home.featured || []
  } catch (e) {
    show(e instanceof ApiError ? e.message : '首页数据加载失败')
  }
})

// 一级分类名作为快捷标签
const CHIPS = computed(() => categories.value.map((c) => c.name))

async function onSearch(q) {
  const kw = (q || '').trim()
  if (!kw) {
    show('请输入要搜索的内容')
    return
  }
  query.value = kw
  searched.value = true
  try {
    const res = await searchSite({ q: kw })
    results.value = res.list || []
    total.value = res.total || 0
    if (!results.value.length) show(`没有找到与“${kw}”相关的内容`)
  } catch (e) {
    show(e instanceof ApiError ? e.message : '搜索失败')
  }
}

function categoryIdByName(name) {
  const node = categories.value.find((c) => c.name === name)
  return node ? node.id : null
}

async function onSearchCategory(name) {
  const id = categoryIdByName(name)
  query.value = name
  searched.value = true
  try {
    const res = await searchSite({ categoryId: id })
    results.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    show(e instanceof ApiError ? e.message : '加载失败')
  }
}

function onChip(c) {
  onSearchCategory(c)
}

function onNavigate(label) {
  if (label === '首页') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  onSearchCategory(label)
}

// 账户面板
const statusText = computed(() => (user.value?.status === 1 ? '已禁用' : '正常'))
const createdAt = computed(() =>
  user.value?.createdAt ? String(user.value.createdAt).replace('T', ' ').slice(0, 19) : '—'
)
const shortToken = computed(() =>
  token.value ? `${token.value.slice(0, 28)} …… ${token.value.slice(-12)}` : '（无令牌）'
)

async function onRefresh() {
  try {
    await refresh()
    show('登录态有效 ✦ 服务端已确认你的身份')
  } catch (e) {
    show(e instanceof ApiError ? e.message : '校验失败')
    if (e instanceof ApiError && e.code === 401) {
      router.replace({ name: 'login' })
    }
  }
}

function onLogout() {
  signOut()
  show('已退出，门已关闭。')
  router.replace({ name: 'login' })
}
</script>
