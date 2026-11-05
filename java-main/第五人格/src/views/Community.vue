<template>
  <div class="community">
    <NightSky />

    <!-- 顶栏 -->
    <header class="com-topbar">
      <div class="tb-inner">
        <div class="tb-brand" @click="$router.replace('/home')">
          <span class="tb-emblem">祭</span>
          <span class="tb-word">第五人格 · 玩家社区</span>
        </div>
        <button class="tb-back" @click="$router.replace('/home')">← 返回首页</button>
      </div>
    </header>

    <!-- 小游戏板块 -->
    <section class="game-section">
      <div class="game-head">
        <h2 class="game-title">🕹 逃脱对决 · 1v4</h2>
        <p class="game-sub">1 名监管者 vs 4 名求生者的非对称逃脱游戏 · 支持好友联机 · 技能参考角色图鉴</p>
        <button class="game-full" @click="fullscreen">⛶ 全屏</button>
      </div>
      <div class="game-frame" ref="frameEl">
        <iframe
          class="game-iframe"
          src="/game.html"
          title="逃脱对决小游戏"
          scrolling="no"
          frameborder="0"
          allow="fullscreen; autoplay; gamepad"
        ></iframe>
        <div class="game-tip">点击游戏区域后，用 WASD 移动 · 空格放技能 · E 交互 · 可点「好友联机」组队</div>
      </div>
    </section>

    <!-- 社区帖子 -->
    <section class="post-section">
      <div class="post-head">
        <h2 class="post-title">社区热帖</h2>
        <div class="post-tabs">
          <button
            v-for="t in postTabs"
            :key="t"
            class="ptab"
            :class="{ active: postTab === t }"
            @click="postTab = t"
          >{{ t }}</button>
        </div>
      </div>

      <div class="post-list">
        <article v-for="p in filteredPosts" :key="p.id" class="post-card">
          <div class="pc-head">
            <span class="pc-avatar">{{ (p.author || '友').charAt(0) }}</span>
            <div class="pc-meta">
              <p class="pc-title">{{ p.title }}</p>
              <p class="pc-info">{{ p.subCategory || '社区' }} · {{ p.author || '匿名玩家' }} · {{ p.viewCount || 0 }} 浏览</p>
            </div>
          </div>
          <p class="pc-desc">{{ p.desc }}</p>
        </article>
        <p v-if="!filteredPosts.length" class="post-empty">该板块还没有帖子，来抢沙发～</p>
      </div>
    </section>

    <footer class="com-footer">
      <p>© 第五人格 · 玩家社区 · 演示页面 · 原创视觉，非官方</p>
      <p class="foot-sub">星辉为引 · 与君同游</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NightSky from '../components/NightSky.vue'
import { useToast } from '../composables/useToast'
import { ApiError } from '../api/http'
import { searchSite } from '../api/search'

const { show } = useToast()

const posts = ref([])
const postTabs = ['全部', '同人二创', '组队招募', '攻略分享']
const postTab = ref('全部')

const filteredPosts = computed(() => {
  if (postTab.value === '全部') return posts.value
  return posts.value.filter(p => p.subCategory === postTab.value)
})

const frameEl = ref(null)

onMounted(async () => {
  try {
    const res = await searchSite({ categoryId: 5, size: 200 })
    posts.value = res.list || []
  } catch (e) {
    // 社区帖子加载失败不阻塞游戏，静默处理
    show(e instanceof ApiError ? e.message : '社区帖子加载失败')
  }
})

function fullscreen() {
  const el = frameEl.value
  if (!el) return
  if (el.requestFullscreen) el.requestFullscreen()
  else if (el.webkitRequestFullscreen) el.webkitRequestFullscreen()
}
</script>

<style scoped>
.community { position: relative; min-height: 100vh; color: var(--text); z-index: 1; }

/* 顶栏 */
.com-topbar { position: sticky; top: 0; z-index: 40; background: rgba(6, 12, 28, 0.92); backdrop-filter: blur(8px); border-bottom: 1px solid var(--border); }
.tb-inner { max-width: 1180px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; justify-content: space-between; }
.tb-brand { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.tb-emblem { width: 34px; height: 34px; border-radius: 50%; background: var(--gold); color: #1a1208; display: grid; place-items: center; font-weight: 800; font-size: 16px; }
.tb-word { font-family: var(--font); color: var(--gold-light); font-size: 16px; letter-spacing: 1px; }
.tb-back { height: 36px; padding: 0 16px; border: 1px solid var(--border); border-radius: 18px; background: transparent; color: var(--text); font-size: 13px; cursor: pointer; }
.tb-back:hover { border-color: var(--gold); color: var(--gold-light); }

/* 游戏板块 */
.game-section { max-width: 1180px; margin: 22px auto 0; padding: 0 20px; }
.game-head { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; margin-bottom: 12px; }
.game-title { font-size: 20px; color: var(--gold-light); letter-spacing: 1px; }
.game-sub { font-size: 13px; color: var(--muted); flex: 1; min-width: 200px; }
.game-full { height: 34px; padding: 0 18px; border: 1px solid var(--border); border-radius: 17px; background: linear-gradient(180deg, var(--gold-light), var(--gold)); color: #1a1208; font-weight: 700; font-size: 13px; cursor: pointer; }
.game-full:hover { box-shadow: 0 4px 14px rgba(216, 177, 90, 0.4); }
.game-frame { position: relative; aspect-ratio: 16/9; background: #0a0e1c; border: 1px solid var(--border); border-radius: 14px; overflow: hidden; box-shadow: 0 16px 50px rgba(0,0,0,0.5); }
.game-iframe { width: 100%; height: 100%; border: none; display: block; background: #0a0e1c; }
.game-tip { position: absolute; bottom: 12px; left: 50%; transform: translateX(-50%); padding: 6px 16px; background: rgba(6,12,28,0.75); border: 1px solid var(--border); border-radius: 16px; font-size: 12px; color: var(--gold-light); pointer-events: none; white-space: nowrap; }

/* 帖子板块 */
.post-section { max-width: 1180px; margin: 30px auto; padding: 0 20px; }
.post-head { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; margin-bottom: 14px; }
.post-title { font-size: 18px; color: var(--gold-light); }
.post-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.ptab { padding: 6px 16px; border: 1px solid var(--border); border-radius: 16px; background: transparent; color: var(--muted); font-size: 12px; cursor: pointer; transition: .18s; }
.ptab:hover { color: var(--gold-light); border-color: var(--gold); }
.ptab.active { background: linear-gradient(180deg, var(--gold-light), var(--gold)); color: #1a1208; border-color: var(--gold); font-weight: 700; }
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 16px 18px; backdrop-filter: blur(6px); }
.pc-head { display: flex; align-items: center; gap: 12px; }
.pc-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light); display: grid; place-items: center; font-weight: 700; flex-shrink: 0; }
.pc-meta { flex: 1; min-width: 0; }
.pc-title { font-size: 15px; font-weight: 700; color: var(--text); }
.pc-info { font-size: 12px; color: var(--muted); margin-top: 3px; }
.pc-desc { font-size: 13px; color: var(--muted); margin-top: 10px; line-height: 1.6; }
.post-empty { text-align: center; color: var(--muted); padding: 40px 0; }

/* footer */
.com-footer { text-align: center; padding: 30px 0 40px; color: var(--muted); font-size: 12px; }
.foot-sub { color: var(--gold); margin-top: 6px; letter-spacing: 2px; }
</style>
