<template>
  <div class="home">
    <!-- 星空 / 流星叠加在立绘背景之上、文字之下 -->
    <NightSky />
    <!-- 导航栏（含搜索栏） -->
    <NavBar @search="onSearch" @navigate="onNavigate" />

    <main class="home-main">
      <!-- 主视觉（全屏立绘背景 + 标题浮层） -->
      <section class="hero">
        <p class="eyebrow">IDENTITY V · 第五人格</p>
        <h1 class="hero-title">命运之门，为你而启</h1>
        <p class="hero-sub">在监管与求生交织的迷局之中，星辉始终是归途的指引。</p>
        <p v-if="user?.nickname" class="hero-welcome">✦ 欢迎归来，{{ user.nickname }}</p>

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
        <div class="section-head">
          <h2 class="section-title">{{ hasVideos ? '赛事视频' : '搜索结果' }}<span class="section-count">{{ total }} 场</span></h2>
          <p v-if="hasVideos" class="section-sub">点击卡片即可在本站观看，来自哔哩哔哩 · 第五人格赛事官方</p>
          <p v-else-if="hasGuides" class="section-sub">点击攻略卡片查看视频演示与详细文字讲解</p>
          <p v-else-if="hasCharacters" class="section-sub">点击角色卡片查看定位、技能、背景故事与玩法要点</p>
        </div>

        <!-- 视频内容（赛事等）：网格卡片布局 -->
        <div v-if="hasVideos" class="video-grid">
          <article v-for="r in videoResults" :key="r.id" class="video-card">
            <div class="video-player">
              <iframe
                :src="toEmbed(r.videoUrl)"
                scrolling="no"
                border="0"
                frameborder="no"
                framespacing="0"
                allowfullscreen="true"
                loading="lazy"
              ></iframe>
            </div>
            <div class="video-info">
              <h3 class="video-title" :title="r.title">{{ r.title }}</h3>
              <p class="video-desc">{{ r.desc }}</p>
              <div class="video-meta">
                <span class="video-cat">{{ r.subCategory || r.category }}</span>
                <span class="video-author">{{ r.author || '官方' }}</span>
                <span class="video-views">▶ {{ r.viewCount || 0 }}</span>
              </div>
            </div>
          </article>
        </div>

        <!-- 攻略内容：可点击卡片（弹详情，含视频+文字讲解） -->
        <div v-else-if="hasGuides" class="guide-grid">
          <article v-for="r in guideResults" :key="r.id" class="guide-card" @click="openGuide(r)">
            <div class="guide-badge">{{ r.subCategory || r.category }}</div>
            <h3 class="guide-title">{{ r.title }}</h3>
            <p class="guide-desc">{{ r.desc }}</p>
            <div class="guide-foot">
              <span class="guide-author">{{ r.author || '官方' }}</span>
              <span class="guide-more">查看详解 ▸</span>
            </div>
          </article>
        </div>

        <!-- 角色图鉴内容：角色资料卡（弹详情） -->
        <div v-else-if="hasCharacters" class="char-grid">
          <article v-for="r in characterResults" :key="r.id" class="char-card" @click="openCharacter(r)">
            <div class="char-avatar">{{ (r.title || '·').charAt(0) }}</div>
            <div class="char-body">
              <h3 class="char-title">{{ r.title }}</h3>
              <p class="char-desc">{{ r.desc }}</p>
              <div class="char-foot">
                <span class="char-cat">{{ r.subCategory || r.category }}</span>
                <span class="char-more">查看资料 ▸</span>
              </div>
            </div>
          </article>
        </div>

        <!-- 纯文字内容：列表布局 -->
        <div v-else-if="results.length" class="card search-card">
          <ul class="result-list">
            <li v-for="r in results" :key="r.id" class="result-item">
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
        <div class="section-head">
          <h2 class="section-title">门户精选</h2>
          <p class="section-sub">从角色图鉴到赛事战报，一站浏览最新内容</p>
        </div>
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
    </main>

    <footer class="home-footer">
      <div class="foot-divider"></div>
      <p>© 第五人格 · 祭司主题演示页 · 原创视觉，非官方</p>
      <p class="foot-sub">命运之门 · 星辉为引 · 愿每一次抉择皆有回响</p>
    </footer>

    <!-- 详情弹窗（攻略视频+文字讲解 / 角色资料卡） -->
    <Transition name="modal-pop">
      <div v-if="detailOpen" class="modal-mask" @click.self="closeGuide">
        <div class="modal guide-modal">
          <div class="modal-head">
            <h3>{{ detailKind === 'character' ? '角色图鉴 · 资料' : '玩法攻略 · 详解' }}</h3>
            <button class="modal-close" type="button" @click="closeGuide">×</button>
          </div>
          <div class="modal-body">
            <template v-if="detailLoading">
              <p class="empty">内容加载中…</p>
            </template>
            <template v-else-if="detail">
              <h2 class="gd-title">{{ detail.title }}</h2>
              <div class="gd-meta">
                <span class="video-cat">{{ detail.subCategory || detail.category }}</span>
                <span class="video-author">{{ detail.author || '官方' }}</span>
                <span class="video-views">▶ {{ detail.viewCount || 0 }}</span>
              </div>

              <!-- 视频（攻略等含视频内容） -->
              <div v-if="detail.videoUrl" class="gd-video">
                <iframe
                  :src="toEmbed(detail.videoUrl)"
                  scrolling="no"
                  border="0"
                  frameborder="no"
                  framespacing="0"
                  allowfullscreen="true"
                ></iframe>
              </div>

              <!-- 正文（角色资料 / 文字讲解） -->
              <div class="gd-content" v-html="renderContent(detail.content)"></div>
            </template>
            <p v-else class="empty">内容不存在或未发布。</p>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NavBar from '../components/NavBar.vue'
import NightSky from '../components/NightSky.vue'
import { useAuth } from '../stores/auth'
import { useToast } from '../composables/useToast'
import { ApiError } from '../api/http'
import { fetchHome, searchSite, fetchContentDetail } from '../api/search'

const { show } = useToast()
const { user } = useAuth()

// 首页数据（来自后端 /api/home）
const categories = ref([])   // 一级分类（含 children 二级）
const featured = ref([])     // 精选内容

// 搜索状态
const query = ref('')
const results = ref([])
const searched = ref(false)
const total = ref(0)

// 详情弹窗状态
const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const detailKind = ref('guide')   // 'guide' 攻略 | 'character' 角色

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

// 当前结果中是否含视频、视频结果列表（用于切换布局）
const videoResults = computed(() => results.value.filter((r) => r.videoUrl))
const hasVideos = computed(() => videoResults.value.length > 0)

// 攻略内容（属于「玩法攻略」一级分类），用于攻略卡片布局
const guideResults = computed(() => results.value.filter((r) => r.category === '玩法攻略'))
const hasGuides = computed(() => guideResults.value.length > 0)

// 角色图鉴内容（属于「角色图鉴」一级分类），用于角色资料卡布局
const characterResults = computed(() => results.value.filter((r) => r.category === '角色图鉴'))
const hasCharacters = computed(() => characterResults.value.length > 0)

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

// 打开攻略详情弹窗
async function openGuide(r) {
  detailKind.value = 'guide'
  await openDetail(r)
}

// 打开角色详情弹窗
async function openCharacter(r) {
  detailKind.value = 'character'
  await openDetail(r)
}

async function openDetail(r) {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await fetchContentDetail(r.id)
  } catch (e) {
    show(e instanceof ApiError ? e.message : '内容加载失败')
    detailOpen.value = false
  } finally {
    detailLoading.value = false
  }
}

function closeGuide() {
  detailOpen.value = false
  detail.value = null
}

// 把正文的换行渲染为段落（正文内用 \n\n 分段、\n 换行）
function renderContent(content) {
  if (!content) return '<p class="gd-empty">暂无文字讲解。</p>'
  const blocks = String(content)
    .split(/\n\n+/)
    .map((b) => b.trim())
    .filter(Boolean)
  return blocks
    .map((b) => {
      const lines = b
        .split('\n')
        .map((l) => l.trim())
        .filter(Boolean)
      // 以「一、二、三…」或「·」开头的块保留行内换行，其余合并
      if (/^[一二三四五六七八九十]、/.test(lines[0]) || /^[1-9]\./.test(lines[0])) {
        return `<p class="gd-sec">${lines.map(escapeHtml).join('<br/>')}</p>`
      }
      return `<p>${lines.map(escapeHtml).join('<br/>')}</p>`
    })
    .join('')
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

// 把 B 站视频地址规范化为可嵌入 iframe 的 src
// 支持三种输入：1) 纯 BV 号  2) https://www.bilibili.com/video/BVxxx  3) //player.bilibili.com/player.html?bvid=xxx
function toEmbed(url) {
  if (!url) return ''
  const s = String(url).trim()
  // 已是 player 嵌入地址，直接补全协议返回
  if (/player\.bilibili\.com\/player\.html/.test(s)) {
    return s.startsWith('//') ? 'https:' + s : s
  }
  // 提取 BV 号
  const m = s.match(/BV[0-9A-Za-z]+/)
  if (m) {
    return `https://player.bilibili.com/player.html?bvid=${m[0]}&autoplay=0&page=1`
  }
  return s
}

function onNavigate(label) {
  if (label === '首页') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  onSearchCategory(label)
}
</script>
