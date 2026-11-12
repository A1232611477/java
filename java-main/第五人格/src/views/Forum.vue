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
      <!-- 发帖区 -->
      <section class="compose panel">
        <h2 class="sec-title">✦ 发新帖</h2>
        <div class="field">
          <input v-model="form.title" class="inp full" maxlength="120" placeholder="标题（最多 120 字）" />
          <small class="err">{{ errors.title }}</small>
        </div>
        <div class="field">
          <textarea v-model="form.content" class="inp full" rows="4" placeholder="正文内容，分享你的心得、招募队友、聊聊游戏…"></textarea>
        </div>
        <div class="compose-foot">
          <span class="hint">发布后所有玩家可见</span>
          <button class="btn-primary sm" :disabled="publishing" @click="onPublish">{{ publishing ? '发 布 中 …' : '发 布 帖 子' }}</button>
        </div>
      </section>

      <!-- 帖子列表 -->
      <section class="posts">
        <div class="posts-head">
          <h2 class="sec-title">社区热帖</h2>
          <span class="posts-count">共 {{ total }} 篇</span>
        </div>

        <div v-if="loading" class="empty">加载中…</div>
        <div v-else class="post-list">
          <article v-for="p in posts" :key="p.id" class="post-card" @click="openDetail(p)">
            <div class="pc-head">
              <span class="pc-avatar">{{ (p.authorNickname || '友').charAt(0) }}</span>
              <div class="pc-meta">
                <p class="pc-title">{{ p.title }}</p>
                <p class="pc-info">{{ p.authorNickname || '匿名玩家' }} · {{ fmt(p.createdAt) }}</p>
              </div>
              <button
                v-if="p.authorId === myId"
                class="post-del"
                @click.stop="onDelete(p)"
                title="删除帖子"
              >删除</button>
            </div>
            <p v-if="p.content" class="pc-desc">{{ p.content }}</p>
          </article>
          <p v-if="!posts.length" class="post-empty">还没有帖子，来抢沙发～</p>
        </div>

        <div class="pager" v-if="total > 0">
          <button class="btn-ghost sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
          <span class="pager-info">第 {{ page }} / {{ totalPages }} 页</span>
          <button class="btn-ghost sm" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
        </div>
      </section>
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
            <p class="detail-meta">{{ detail?.authorNickname }} · {{ fmt(detail?.createdAt) }}</p>
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

function fmt(s) {
  if (!s) return '—'
  return String(s).replace('T', ' ').slice(0, 16)
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
.com-topbar { position: sticky; top: 0; z-index: 40; background: rgba(6, 12, 28, 0.92); backdrop-filter: blur(8px); border-bottom: 1px solid var(--border); }
.tb-inner { max-width: 1080px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; justify-content: space-between; }
.tb-brand { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.tb-emblem { width: 34px; height: 34px; border-radius: 50%; background: var(--gold); color: #1a1208; display: grid; place-items: center; font-weight: 800; font-size: 16px; }
.tb-word { font-family: var(--font); color: var(--gold-light); font-size: 16px; letter-spacing: 1px; }
.tb-back { height: 36px; padding: 0 16px; border: 1px solid var(--border); border-radius: 18px; background: transparent; color: var(--text); font-size: 13px; cursor: pointer; }
.tb-back:hover { border-color: var(--gold); color: var(--gold-light); }

.forum-main { max-width: 1080px; margin: 22px auto 60px; padding: 0 20px; }
.sec-title { font-family: var(--font); font-size: 18px; color: var(--gold-light); letter-spacing: 1px; margin-bottom: 14px; }

.compose { padding: 20px; margin-bottom: 28px; }
.compose .field { margin-bottom: 12px; }
.compose .err { display: block; color: #ff9a9a; font-size: 0.76rem; margin-top: 6px; }
.compose-foot { display: flex; align-items: center; justify-content: space-between; }
.hint { color: var(--muted); font-size: 12px; }

.posts-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 14px; }
.posts-count { color: var(--muted); font-size: 13px; }
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 16px 18px; backdrop-filter: blur(6px); cursor: pointer; transition: border-color .18s; }
.post-card:hover { border-color: var(--gold); }
.pc-head { display: flex; align-items: center; gap: 12px; }
.pc-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light); display: grid; place-items: center; font-weight: 700; flex-shrink: 0; }
.pc-meta { flex: 1; min-width: 0; }
.pc-title { font-size: 15px; font-weight: 700; color: var(--text); }
.pc-info { font-size: 12px; color: var(--muted); margin-top: 3px; }
.pc-desc { font-size: 13px; color: var(--muted); margin-top: 10px; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
.post-del { background: transparent; border: 1px solid rgba(255,154,154,0.4); color: #ff9a9a; border-radius: 8px; font-size: 12px; padding: 3px 10px; cursor: pointer; }
.post-del:hover { background: rgba(255,154,154,0.12); }
.post-empty { text-align: center; color: var(--muted); padding: 40px 0; }
.pager { display: flex; align-items: center; justify-content: flex-end; gap: 0.9rem; margin-top: 1.1rem; }
.pager-info { color: var(--muted); font-size: 0.84rem; }

.detail-meta { color: var(--muted); font-size: 13px; margin-bottom: 14px; }
.detail-content { color: var(--text); font-size: 14px; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
</style>
