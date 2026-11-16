<template>
  <div class="forum-page">
    <NightSky />
    <header class="com-topbar">
      <div class="tb-inner">
        <div class="tb-brand" @click="$router.replace('/home')">
          <span class="tb-emblem">祭</span>
          <span class="tb-word">第五人格 · 玩家论坛</span>
        </div>
        <button class="tb-back" @click="$router.replace('/home')">← 返回首页</button>
      </div>
    </header>

    <main class="forum-main">
      <!-- 论坛横幅 -->
      <section class="forum-hero">
        <div class="hero-orn">✦</div>
        <h1 class="hero-title">玩 家 论 坛</h1>
        <p class="hero-sub">分享心得 · 招募队友 · 闲聊庄园</p>
        <div class="hero-stats">
          <span class="stat-chip"><b>{{ total }}</b> 篇帖子</span>
          <span class="stat-chip">第 {{ page }} / {{ totalPages }} 页</span>
          <span class="stat-chip">✦ 欧利蒂丝庄园议会</span>
        </div>
      </section>

      <div class="forum-layout">
        <!-- 左侧主栏 -->
        <div class="forum-col">
          <!-- 发帖区 -->
          <section class="compose panel">
            <h2 class="sec-title"><span class="sec-mark">✦</span> 发 新 帖</h2>
            <div class="field">
              <input v-model="form.title" class="inp full" maxlength="120" placeholder="标题（最多 120 字）" />
              <div class="field-foot">
                <small class="err">{{ errors.title }}</small>
                <small class="counter">{{ form.title.length }} / 120</small>
              </div>
            </div>
            <div class="field">
              <textarea v-model="form.content" class="inp full" rows="4" maxlength="2000" placeholder="正文内容，分享你的心得、招募队友、聊聊游戏…"></textarea>
              <div class="field-foot">
                <small class="counter">{{ form.content.length }} / 2000</small>
              </div>
            </div>
            <div class="compose-foot">
              <span class="hint">发布后所有玩家可见</span>
              <button class="btn-primary sm" :disabled="publishing" @click="onPublish">{{ publishing ? '发 布 中 …' : '发 布 帖 子' }}</button>
            </div>
          </section>

          <!-- 帖子列表 -->
          <section class="posts">
            <div class="posts-head">
              <h2 class="sec-title"><span class="sec-mark">✦</span> 社区热帖</h2>
              <span class="posts-count">共 {{ total }} 篇</span>
            </div>

            <div v-if="loading" class="empty">加载中…</div>
            <div v-else class="post-list">
              <article v-for="(p, i) in posts" :key="p.id" class="post-card" @click="openDetail(p)">
                <div class="pc-rank" :class="{ top: rank(p, i) <= 3 }">{{ pad(rank(p, i)) }}</div>
                <span class="pc-avatar">{{ (p.authorNickname || '友').charAt(0) }}</span>
                <div class="pc-meta">
                  <p class="pc-title">{{ p.title }}</p>
                  <p class="pc-info">
                    <span class="pc-author">{{ p.authorNickname || '匿名玩家' }}</span>
                    <span class="dot">·</span>
                    <span>{{ timeAgo(p.createdAt) }}</span>
                    <span class="dot">·</span>
                    <span>#{{ p.id }}</span>
                  </p>
                  <p v-if="p.content" class="pc-desc">{{ p.content }}</p>
                </div>
                <button
                  v-if="p.authorId === myId"
                  class="post-del"
                  @click.stop="onDelete(p)"
                  title="删除帖子"
                >删除</button>
              </article>
              <div v-if="!posts.length" class="post-empty">
                <div class="empty-orn">✦</div>
                <p>还没有帖子，来抢沙发～</p>
              </div>
            </div>

            <div class="pager" v-if="total > 0">
              <button class="pg-btn" :disabled="page <= 1" @click="changePage(page - 1)">‹</button>
              <template v-for="n in pageList" :key="n">
                <button class="pg-btn" :class="{ on: n === page }" @click="changePage(n)">{{ n }}</button>
              </template>
              <button class="pg-btn" :disabled="page >= totalPages" @click="changePage(page + 1)">›</button>
              <span class="pager-info">第 {{ page }} / {{ totalPages }} 页</span>
            </div>
          </section>
        </div>

        <!-- 右侧边栏 -->
        <aside class="forum-side">
          <section class="side-card">
            <h3 class="side-title"><span class="sec-mark">⚑</span> 论坛公告</h3>
            <ul class="side-list">
              <li>请友善交流，尊重每一位玩家。</li>
              <li>禁止发布广告、外挂、代练信息。</li>
              <li>违规内容将视情节删除或封禁。</li>
            </ul>
          </section>

          <section class="side-card">
            <h3 class="side-title"><span class="sec-mark">✦</span> 最新发帖</h3>
            <ul v-if="sidePosts.length" class="side-posts">
              <li v-for="s in sidePosts" :key="s.id" class="side-post" @click="openDetail(s)">
                <span class="sp-dot"></span>
                <span class="sp-title">{{ s.title }}</span>
              </li>
            </ul>
            <p v-else class="side-empty">暂无帖子</p>
          </section>

          <section class="side-card">
            <h3 class="side-title"><span class="sec-mark">✧</span> 关于庄园</h3>
            <p class="side-text">第五人格 —— 非对称对抗竞技手游。在迷雾笼罩的庄园里，选择你的阵营，书写属于你的故事。</p>
          </section>
        </aside>
      </div>
    </main>

    <!-- 帖子详情弹窗 -->
    <Transition name="modal-pop">
      <div v-if="detailOpen" class="modal-mask" @click.self="detailOpen = false">
        <div class="modal">
          <div class="modal-head">
            <h3>{{ detail?.title }}</h3>
            <button class="modal-close" @click="detailOpen = false">×</button>
          </div>
          <div class="modal-body">
            <p class="detail-meta">
              <span class="dm-avatar">{{ (detail?.authorNickname || '友').charAt(0) }}</span>
              <span>{{ detail?.authorNickname || '匿名玩家' }}</span>
              <span class="dot">·</span>
              <span>{{ fmt(detail?.createdAt) }}</span>
              <span class="dot">·</span>
              <span>#{{ detail?.id }}</span>
            </p>
            <div class="detail-content">{{ detail?.content || '（无正文）' }}</div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import NightSky from '../components/NightSky.vue'
import { useToast } from '../composables/useToast'
import { useAuth } from '../stores/auth'
import { ApiError } from '../api/http'
import { fetchPosts, createPost, deletePost } from '../api/forum'

const { show } = useToast()
const { user } = useAuth()

const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const form = reactive({ title: '', content: '' })
const errors = reactive({ title: '' })
const publishing = ref(false)

const detailOpen = ref(false)
const detail = ref(null)

const myId = computed(() => user.value?.id)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const sidePosts = computed(() => posts.value.slice(0, 6))
const pageList = computed(() => {
  const t = totalPages.value
  const cur = page.value
  const start = Math.max(1, cur - 2)
  const end = Math.min(t, cur + 2)
  const arr = []
  for (let i = start; i <= end; i++) arr.push(i)
  return arr
})

function fmt(s) {
  if (!s) return '—'
  return String(s).replace('T', ' ').slice(0, 16)
}

function timeAgo(s) {
  if (!s) return '—'
  const d = new Date(String(s).replace(' ', 'T'))
  if (isNaN(d.getTime())) return fmt(s)
  const diff = Date.now() - d.getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  const hr = Math.floor(min / 60)
  if (hr < 24) return `${hr} 小时前`
  const day = Math.floor(hr / 24)
  if (day < 7) return `${day} 天前`
  return fmt(s)
}

function rank(p, i) {
  return (page.value - 1) * size.value + i + 1
}

function pad(n) {
  return n < 10 ? `0${n}` : `${n}`
}

async function loadList() {
  loading.value = true
  try {
    const data = await fetchPosts(page.value, size.value)
    posts.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    show(e instanceof ApiError ? e.message : '帖子加载失败')
  } finally {
    loading.value = false
  }
}

function changePage(p) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadList()
}

async function onPublish() {
  errors.title = ''
  if (!form.title.trim()) { errors.title = '请输入标题'; return }
  publishing.value = true
  try {
    await createPost(form.title.trim(), form.content.trim())
    show('帖子发布成功')
    form.title = ''
    form.content = ''
    page.value = 1
    await loadList()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '发布失败')
  } finally {
    publishing.value = false
  }
}

function openDetail(p) {
  detail.value = p
  detailOpen.value = true
}

async function onDelete(p) {
  if (!confirm(`确定删除帖子「${p.title}」？`)) return
  try {
    await deletePost(p.id)
    show('已删除')
    loadList()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.forum-page { position: relative; min-height: 100vh; color: var(--text); z-index: 1; }

/* ---------- 顶栏 ---------- */
.com-topbar { position: sticky; top: 0; z-index: 40; background: rgba(6, 12, 28, 0.92); backdrop-filter: blur(8px); border-bottom: 1px solid var(--border); }
.com-topbar::before { content: ""; position: absolute; top: 0; left: 0; right: 0; height: 1px; background: linear-gradient(90deg, transparent, var(--gold), transparent); }
.tb-inner { max-width: 1180px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; justify-content: space-between; }
.tb-brand { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.tb-emblem { width: 34px; height: 34px; border-radius: 50%; background: radial-gradient(circle at 32% 28%, var(--gold-light), var(--gold) 70%); color: #1a1208; display: grid; place-items: center; font-weight: 800; font-size: 16px; box-shadow: 0 0 0 2px rgba(216,177,90,0.5), 0 0 16px rgba(216,177,90,0.4); }
.tb-word { font-family: var(--font); color: var(--gold-light); font-size: 16px; letter-spacing: 1px; }
.tb-back { height: 36px; padding: 0 16px; border: 1px solid var(--border); border-radius: 18px; background: transparent; color: var(--text); font-size: 13px; cursor: pointer; transition: border-color .2s, color .2s; }
.tb-back:hover { border-color: var(--gold); color: var(--gold-light); }

/* ---------- 布局 ---------- */
.forum-main { max-width: 1180px; margin: 24px auto 60px; padding: 0 20px; }

.forum-hero {
  position: relative; text-align: center; padding: 2.5rem 1.5rem 2.1rem; margin-bottom: 1.6rem;
  border-radius: 18px; overflow: hidden;
  background: linear-gradient(160deg, rgba(43,91,181,0.14), rgba(9,20,46,0.55) 55%, rgba(216,177,90,0.08));
  border: 1px solid var(--border);
  box-shadow: 0 16px 44px rgba(2,6,18,0.5), inset 0 1px 0 rgba(255,255,255,0.05);
}
.forum-hero::before, .forum-hero::after {
  content: ""; position: absolute; width: 26px; height: 26px; pointer-events: none;
  border: 1.5px solid var(--gold); opacity: 0.85;
}
.forum-hero::before { top: 12px; left: 12px; border-right: none; border-bottom: none; border-top-left-radius: 10px; }
.forum-hero::after { bottom: 12px; right: 12px; border-left: none; border-top: none; border-bottom-right-radius: 10px; }
.hero-orn { font-size: 1.5rem; color: var(--gold); text-shadow: 0 0 14px rgba(216,177,90,0.7); animation: floaty 4s ease-in-out infinite; }
.hero-title {
  font-family: var(--font); font-weight: 700; font-size: 2.4rem; letter-spacing: 0.14em; margin: 0.4rem 0 0.5rem;
  background: linear-gradient(110deg, #fff 10%, var(--blue-light) 45%, var(--gold-light) 80%);
  background-size: 220% auto; -webkit-background-clip: text; background-clip: text; color: transparent;
  filter: drop-shadow(0 0 22px rgba(127,168,232,0.3)); animation: shimmer 6s linear infinite;
}
@keyframes shimmer { to { background-position: 220% center; } }
@keyframes floaty { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }
.hero-sub { color: var(--muted); font-size: 0.95rem; letter-spacing: 0.06em; }
.hero-stats { display: flex; flex-wrap: wrap; justify-content: center; gap: 0.7rem; margin-top: 1.3rem; }
.stat-chip {
  padding: 0.34rem 0.95rem; border-radius: 999px; font-size: 0.82rem; color: #cdd9f2;
  background: rgba(255,255,255,0.04); border: 1px solid rgba(159,178,216,0.25);
}
.stat-chip b { color: var(--gold-light); font-family: var(--font); font-weight: 700; }

.forum-layout { display: grid; grid-template-columns: minmax(0, 1fr) 290px; gap: 1.5rem; align-items: start; }

.forum-col { display: flex; flex-direction: column; gap: 1.4rem; min-width: 0; }

.sec-title { font-family: var(--font); font-size: 17px; color: var(--gold-light); letter-spacing: 2px; margin-bottom: 14px; display: flex; align-items: center; gap: 0.4rem; }
.sec-mark { color: var(--gold); text-shadow: 0 0 10px rgba(216,177,90,0.6); font-size: 15px; }

/* ---------- 发帖区 ---------- */
.compose { position: relative; padding: 22px 22px 18px; }
.compose::before, .compose::after {
  content: ""; position: absolute; width: 20px; height: 20px; pointer-events: none;
  border: 1.5px solid var(--gold); opacity: 0.7;
}
.compose::before { top: 12px; left: 12px; border-right: none; border-bottom: none; border-top-left-radius: 8px; }
.compose::after { bottom: 12px; right: 12px; border-left: none; border-top: none; border-bottom-right-radius: 8px; }
.compose .field { margin-bottom: 12px; }
.compose .inp { background: rgba(255,255,255,0.05); }
textarea.inp { resize: vertical; min-height: 96px; line-height: 1.6; }
.field-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 5px; }
.compose .err { color: #ff9a9a; font-size: 0.76rem; }
.counter { color: var(--muted); font-size: 0.72rem; opacity: 0.75; }
.compose-foot { display: flex; align-items: center; justify-content: space-between; }
.hint { color: var(--muted); font-size: 12px; }

/* ---------- 帖子列表 ---------- */
.posts-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 14px; }
.posts-count { color: var(--muted); font-size: 13px; }
.post-list { display: flex; flex-direction: column; gap: 12px; }

.post-card {
  position: relative; display: flex; align-items: flex-start; gap: 13px;
  background: var(--card); border: 1px solid var(--border); border-radius: 14px;
  padding: 16px 18px 16px 12px; backdrop-filter: blur(6px); cursor: pointer;
  transition: transform .18s, border-color .2s, box-shadow .2s, background .2s;
}
.post-card::before {
  content: ""; position: absolute; top: 0; bottom: 0; left: 0; width: 3px;
  border-radius: 14px 0 0 14px;
  background: linear-gradient(180deg, var(--gold), transparent);
  opacity: 0; transition: opacity .2s;
}
.post-card:hover {
  border-color: var(--gold); transform: translateY(-2px);
  box-shadow: 0 14px 34px rgba(2,6,18,0.55), 0 0 24px rgba(216,177,90,0.16);
}
.post-card:hover::before { opacity: 1; }

.pc-rank {
  flex: none; width: 30px; padding-top: 9px; text-align: center;
  font-family: var(--font); font-size: 0.85rem; color: var(--muted); opacity: 0.7;
}
.pc-rank.top { color: var(--gold-light); opacity: 1; text-shadow: 0 0 10px rgba(216,177,90,0.5); }

.pc-avatar {
  flex: none; width: 42px; height: 42px; border-radius: 50%;
  background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light);
  display: grid; place-items: center; font-weight: 700; font-size: 16px;
  box-shadow: 0 0 0 2px rgba(127,168,232,0.3), 0 6px 16px rgba(43,91,181,0.3);
}
.pc-meta { flex: 1; min-width: 0; }
.pc-title { font-size: 15.5px; font-weight: 700; color: var(--text); line-height: 1.4; }
.post-card:hover .pc-title { color: var(--gold-light); }
.pc-info { font-size: 12px; color: var(--muted); margin-top: 4px; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.pc-author { color: var(--blue-light); }
.dot { opacity: 0.5; }
.pc-desc {
  font-size: 13px; color: var(--muted); margin-top: 10px; line-height: 1.65;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
  word-break: break-word;
}
.post-del {
  flex: none; background: transparent; border: 1px solid rgba(255,154,154,0.4); color: #ff9a9a;
  border-radius: 8px; font-size: 12px; padding: 3px 10px; cursor: pointer; margin-top: 4px;
  transition: background .2s;
}
.post-del:hover { background: rgba(255,154,154,0.12); }

.post-empty { text-align: center; color: var(--muted); padding: 46px 0; }
.empty-orn { font-size: 1.6rem; color: var(--gold); margin-bottom: 0.6rem; text-shadow: 0 0 14px rgba(216,177,90,0.6); }

/* ---------- 分页 ---------- */
.pager { display: flex; align-items: center; justify-content: flex-end; gap: 0.4rem; margin-top: 1.1rem; flex-wrap: wrap; }
.pg-btn {
  min-width: 34px; height: 34px; padding: 0 8px; border-radius: 9px; cursor: pointer;
  background: transparent; border: 1px solid rgba(159,178,216,0.3); color: #cdd9f2; font-size: 0.86rem;
  transition: border-color .2s, color .2s, background .2s;
}
.pg-btn:hover { border-color: var(--gold); color: var(--gold-light); background: rgba(216,177,90,0.1); }
.pg-btn.on { background: linear-gradient(120deg, var(--gold-light), var(--gold) 60%, #b8902f); color: #07101f; border-color: transparent; font-weight: 700; }
.pg-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.pager-info { color: var(--muted); font-size: 0.82rem; margin-left: 0.5rem; }

/* ---------- 侧边栏 ---------- */
.forum-side { display: flex; flex-direction: column; gap: 1.2rem; }
.side-card {
  background: var(--card); border: 1px solid var(--border); border-radius: 14px;
  padding: 16px 18px; backdrop-filter: blur(6px);
}
.side-title { font-family: var(--font); font-size: 15px; color: var(--gold-light); letter-spacing: 1.5px; margin-bottom: 12px; display: flex; align-items: center; gap: 0.4rem; }
.side-list { list-style: none; display: flex; flex-direction: column; gap: 8px; }
.side-list li { position: relative; padding-left: 14px; color: var(--muted); font-size: 0.82rem; line-height: 1.6; }
.side-list li::before { content: ""; position: absolute; left: 0; top: 8px; width: 5px; height: 5px; border-radius: 50%; background: var(--gold); }
.side-posts { list-style: none; display: flex; flex-direction: column; gap: 4px; }
.side-post { display: flex; align-items: center; gap: 8px; padding: 7px 4px; cursor: pointer; border-radius: 8px; transition: background .2s; }
.side-post:hover { background: rgba(216,177,90,0.08); }
.sp-dot { flex: none; width: 5px; height: 5px; border-radius: 50%; background: var(--blue-light); }
.sp-title { flex: 1; min-width: 0; font-size: 0.84rem; color: var(--text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.side-post:hover .sp-title { color: var(--gold-light); }
.side-empty { color: var(--muted); font-size: 0.82rem; }
.side-text { color: var(--muted); font-size: 0.82rem; line-height: 1.7; }

/* ---------- 弹窗 ---------- */
.detail-meta { display: flex; align-items: center; gap: 7px; flex-wrap: wrap; color: var(--muted); font-size: 13px; margin-bottom: 14px; }
.dm-avatar {
  width: 24px; height: 24px; border-radius: 50%; display: inline-grid; place-items: center;
  font-size: 12px; font-weight: 700; color: var(--ink-1);
  background: radial-gradient(circle at 32% 28%, var(--gold-light), var(--gold) 70%);
}
.detail-content { color: var(--text); font-size: 14px; line-height: 1.75; white-space: pre-wrap; word-break: break-word; }

/* ---------- 响应式 ---------- */
@media (max-width: 920px) {
  .forum-layout { grid-template-columns: 1fr; }
  .forum-side { order: 2; }
  .hero-title { font-size: 1.9rem; }
}
</style>
