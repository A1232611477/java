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
        <h2 class="game-title">🕹 社区小游戏</h2>
        <p class="game-sub">{{ current.title }} · {{ current.desc }}</p>
        <button class="game-full" @click="fullscreen">⛶ 全屏</button>
      </div>

      <!-- 游戏切换 -->
      <div class="game-tabs">
        <button
          v-for="g in games"
          :key="g.key"
          class="gtab"
          :class="{ active: currentKey === g.key }"
          @click="switchGame(g.key)"
        >
          <span class="gtab-ico">{{ g.icon }}</span>
          <span>{{ g.name }}</span>
        </button>
      </div>

      <div class="game-frame" :class="{ board: current.layout === 'board' }" ref="frameEl">
        <iframe
          v-if="current.src"
          :key="current.key"
          class="game-iframe"
          :src="current.src"
          :title="current.name"
          :scrolling="current.layout === 'board' ? 'auto' : 'no'"
          frameborder="0"
          allow="fullscreen; autoplay; gamepad"
        ></iframe>
        <div class="game-tip">{{ current.tip }}</div>
      </div>
    </section>

    <!-- 每日运势占卜 -->
    <FortuneCard />

    <footer class="com-footer">
      <p>© 第五人格 · 玩家社区 · 演示页面 · 原创视觉，非官方</p>
      <p class="foot-sub">星辉为引 · 与君同游</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NightSky from '../components/NightSky.vue'
import FortuneCard from '../components/FortuneCard.vue'

const frameEl = ref(null)

const games = [
  {
    key: 'escape',
    icon: '🕹',
    name: '逃脱对决 1v4',
    title: '逃脱对决 · 1v4',
    desc: '1 名监管者 vs 4 名求生者的非对称逃脱游戏 · 支持好友联机',
    src: '/game.html',
    layout: 'wide',
    tip: '点击游戏区域后，用 WASD 移动 · 空格放技能 · E 交互 · 可点「好友联机」组队'
  },
  {
    key: 'gomoku',
    icon: '⚫',
    name: '五子棋',
    title: '五子棋',
    desc: '双人 / 人机对战 · 连成五子者胜',
    src: '/gomoku.html',
    layout: 'board',
    tip: '点击棋盘交叉点落子 · 黑方先行'
  }
]

const currentKey = ref('escape')
const current = computed(() => games.find(g => g.key === currentKey.value))

onMounted(() => {
  // 收到五子棋邀请并接受后，自动切到五子棋 Tab
  try {
    if (localStorage.getItem('idv_gomoku_join')) {
      currentKey.value = 'gomoku'
    }
  } catch (e) { /* 忽略 */ }
})

function switchGame(key) {
  currentKey.value = key
}

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

.game-tabs { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 12px; }
.gtab { display: flex; align-items: center; gap: 6px; padding: 7px 16px; border: 1px solid var(--border); border-radius: 18px; background: transparent; color: var(--muted); font-size: 13px; cursor: pointer; transition: .18s; }
.gtab:hover { color: var(--gold-light); border-color: var(--gold); }
.gtab.active { background: linear-gradient(180deg, var(--gold-light), var(--gold)); color: #1a1208; border-color: var(--gold); font-weight: 700; }
.gtab-ico { font-size: 15px; }

.game-frame { position: relative; aspect-ratio: 16/9; background: #0a0e1c; border: 1px solid var(--border); border-radius: 14px; overflow: hidden; box-shadow: 0 16px 50px rgba(0,0,0,0.5); }
.game-frame.board { aspect-ratio: auto; height: 680px; }
.game-iframe { width: 100%; height: 100%; border: none; display: block; background: #0a0e1c; }
.game-tip { position: absolute; bottom: 12px; left: 50%; transform: translateX(-50%); padding: 6px 16px; background: rgba(6,12,28,0.75); border: 1px solid var(--border); border-radius: 16px; font-size: 12px; color: var(--gold-light); pointer-events: none; white-space: nowrap; max-width: 96%; overflow: hidden; text-overflow: ellipsis; }

/* footer */
.com-footer { text-align: center; padding: 30px 0 40px; color: var(--muted); font-size: 12px; }
.foot-sub { color: var(--gold); margin-top: 6px; letter-spacing: 2px; }
</style>
