<template>
  <section class="fortune-section">
    <div class="fortune-card">
      <div class="fc-head">
        <span class="fc-orn">✦</span>
        <h2 class="fc-title">每日运势占卜</h2>
        <span class="fc-orn">✦</span>
      </div>
      <p class="fc-sub">庄园的星象，为你揭示今日运势</p>

      <!-- 卡牌 + 结果（单一卡面，靠 flipped 切换正反，保证翻转动画连贯） -->
      <div class="fc-stage">
        <div class="tcard-wrap" :class="{ flipped: drawn, flipping }" @click="draw">
          <div class="tcard-inner">
            <div class="tcard-face back">
              <span class="back-symbol">☽</span>
              <span class="back-label">点击抽卡</span>
              <span class="back-deco"></span>
            </div>
            <div class="tcard-face front" :class="rankClass(result?.rank)">
              <span class="front-rank">{{ result?.rank || '运势' }}</span>
              <span class="front-symbol">{{ result?.symbol || '☽' }}</span>
              <span class="front-name">{{ result?.name || '尚未抽取' }}</span>
            </div>
          </div>
        </div>

        <Transition name="rise">
          <div v-if="drawn && result" class="fc-result">
            <p class="r-fortune">{{ result.desc }}</p>
            <div class="r-grid">
              <div class="r-item">
                <span class="r-k">幸运色</span>
                <span class="r-v"><i class="swatch" :style="{ background: result.colorHex }"></i>{{ result.luckyColor }}</span>
              </div>
              <div class="r-item">
                <span class="r-k">幸运数字</span>
                <span class="r-v num">{{ result.luckyNumber }}</span>
              </div>
              <div class="r-item">
                <span class="r-k">宜</span>
                <span class="r-v good">{{ result.advice }}</span>
              </div>
              <div class="r-item">
                <span class="r-k">忌</span>
                <span class="r-v bad">{{ result.avoid }}</span>
              </div>
            </div>
          </div>
        </Transition>

        <p class="stage-hint">{{ drawn ? '今日已抽取 · 明日 00:00 刷新' : '每日一次 · 点击卡背抽取今日运势' }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const CARDS = [
  { name: '祭司 · 圣心之门', rank: '大吉', symbol: '🕯', luckyColor: '金色', colorHex: '#f6e1a4', luckyNumber: 8, desc: '今日运势极佳，心想事成。如门扉开启，机缘纷至沓来，大胆迈出第一步。', advice: '发起挑战', avoid: '犹豫不决' },
  { name: '欧皇附体', rank: '大吉', symbol: '👑', luckyColor: '彩虹', colorHex: 'linear-gradient(90deg,#ff6b6b,#ffc46b,#7fe0ac,#7fa8e8,#c9a0ff)', luckyNumber: 7, desc: '抽卡掉落运气爆棚，单抽出奇迹。趁着好运，去搏一把大的。', advice: '大胆抽卡', avoid: '上头加注' },
  { name: '开门红', rank: '大吉', symbol: '🚪', luckyColor: '正红', colorHex: '#ff6b6b', luckyNumber: 10, desc: '新一天的好兆头，诸事顺遂。适合开启新计划，抢占先机。', advice: '立即行动', avoid: '宅着不动' },
  { name: '监管者 · 追猎', rank: '吉', symbol: '🔦', luckyColor: '深蓝', colorHex: '#7fa8e8', luckyNumber: 3, desc: '目标明确、行动力强的一天，主动出击方能掌握节奏。', advice: '主动出击', avoid: '拖延' },
  { name: '求生者 · 破译', rank: '吉', symbol: '🔧', luckyColor: '青色', colorHex: '#6bd7c0', luckyNumber: 5, desc: '专注当下，稳步推进，再难的任务也能水到渠成。', advice: '专注手头事', avoid: '分心' },
  { name: '幸运之星', rank: '吉', symbol: '⭐', luckyColor: '银白', colorHex: '#d7e3ff', luckyNumber: 9, desc: '有贵人相助的一天，棘手的事不妨开口求助，往往迎刃而解。', advice: '寻求帮助', avoid: '逞强' },
  { name: '顺风局', rank: '中吉', symbol: '🍃', luckyColor: '浅绿', colorHex: '#7fe0ac', luckyNumber: 2, desc: '整体平顺，节奏在握，适合乘胜追击，把进度再往前推一推。', advice: '乘胜追击', avoid: '冒进' },
  { name: '柳暗花明', rank: '中吉', symbol: '🌅', luckyColor: '暖橙', colorHex: '#ffc46b', luckyNumber: 6, desc: '短暂的迷茫之后豁然开朗，转机就在眼前，稳住别慌。', advice: '耐心等待', avoid: '半途而废' },
  { name: '蓄势待发', rank: '中吉', symbol: '🌙', luckyColor: '淡紫', colorHex: '#c9a0ff', luckyNumber: 4, desc: '厚积薄发的一天，此刻宜沉心积累，为下一次爆发蓄力。', advice: '学习积累', avoid: '急躁' },
  { name: '转危为安', rank: '小吉', symbol: '🛡', luckyColor: '米白', colorHex: '#e8d9a8', luckyNumber: 1, desc: '虽有小小波折，终能化险为夷。多留个心眼，稳字当头。', advice: '谨慎行事', avoid: '鲁莽' },
  { name: '小确幸', rank: '小吉', symbol: '🌸', luckyColor: '粉色', colorHex: '#ffb3c6', luckyNumber: 11, desc: '平凡日子里的微小惊喜，留意身边的美好，心情会更好。', advice: '保持微笑', avoid: '抱怨' },
  { name: '静观其变', rank: '小吉', symbol: '👁', luckyColor: '灰蓝', colorHex: '#9fb2d8', luckyNumber: 0, desc: '今日宜守不宜攻，静待时机，看清局势再出手也不迟。', advice: '静观', avoid: '冲动' }
]

const RANK_WEIGHTS = { '大吉': 12, '吉': 28, '中吉': 35, '小吉': 25 }
const RANK_CLASS = { '大吉': 'rank-da', '吉': 'rank-ji', '中吉': 'rank-zhong', '小吉': 'rank-xiao' }

const KEY = 'idv_fortune'

const drawn = ref(false)
const flipping = ref(false)
const result = ref(null)

function today() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function rankClass(rank) {
  return RANK_CLASS[rank] || 'rank-zhong'
}

function pickRank() {
  const ranks = Object.keys(RANK_WEIGHTS)
  const total = ranks.reduce((a, r) => a + RANK_WEIGHTS[r], 0)
  let r = Math.random() * total
  for (const rank of ranks) {
    r -= RANK_WEIGHTS[rank]
    if (r < 0) return rank
  }
  return '中吉'
}

function pickCard() {
  const rank = pickRank()
  const pool = CARDS.filter(c => c.rank === rank)
  return pool[Math.floor(Math.random() * pool.length)]
}

function draw() {
  if (drawn.value || flipping.value) return
  const card = pickCard()
  result.value = card
  // 先抖一下酝酿运气，再触发翻牌
  flipping.value = true
  setTimeout(() => {
    drawn.value = true
    flipping.value = false
    try {
      localStorage.setItem(KEY, JSON.stringify({ ...card, date: today() }))
    } catch (e) { /* 忽略存储失败 */ }
  }, 550)
}

function loadToday() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return
    const saved = JSON.parse(raw)
    if (saved.date === today() && saved.name) {
      result.value = saved
      drawn.value = true
    }
  } catch (e) { /* 忽略 */ }
}

onMounted(loadToday)
</script>

<style scoped>
.fortune-section { max-width: 1180px; margin: 30px auto 0; padding: 0 20px; }
.fortune-card {
  position: relative; text-align: center; padding: 2rem 1.5rem 2.2rem;
  border-radius: 18px; overflow: hidden;
  background: linear-gradient(160deg, rgba(216,177,90,0.10), rgba(9,20,46,0.55) 50%, rgba(43,91,181,0.12));
  border: 1px solid var(--border);
  box-shadow: 0 16px 44px rgba(2,6,18,0.5), inset 0 1px 0 rgba(255,255,255,0.05);
}
.fortune-card::before, .fortune-card::after {
  content: ""; position: absolute; width: 24px; height: 24px; pointer-events: none;
  border: 1.5px solid var(--gold); opacity: 0.8;
}
.fortune-card::before { top: 12px; left: 12px; border-right: none; border-bottom: none; border-top-left-radius: 8px; }
.fortune-card::after { bottom: 12px; right: 12px; border-left: none; border-top: none; border-bottom-right-radius: 8px; }

.fc-head { display: flex; align-items: center; justify-content: center; gap: 0.6rem; }
.fc-orn { color: var(--gold); text-shadow: 0 0 12px rgba(216,177,90,0.7); }
.fc-title {
  font-family: var(--font); font-weight: 700; font-size: 1.5rem; letter-spacing: 0.12em;
  background: linear-gradient(110deg, #fff 10%, var(--blue-light) 45%, var(--gold-light) 80%);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.fc-sub { color: var(--muted); font-size: 0.85rem; margin-top: 6px; letter-spacing: 0.04em; }

.fc-stage { margin-top: 1.4rem; }
.stage-hint { color: var(--muted); font-size: 0.75rem; margin-top: 1.2rem; opacity: 0.85; }

/* ---------- 卡牌 3D 翻转 ---------- */
.tcard-wrap { width: 168px; height: 240px; margin: 0 auto; perspective: 900px; cursor: pointer; }
.tcard-inner { position: relative; width: 100%; height: 100%; transform-style: preserve-3d; transition: transform 0.9s cubic-bezier(.2,.7,.3,1); }
.tcard-wrap.flipped .tcard-inner { transform: rotateY(180deg); }
.tcard-wrap.flipped { cursor: default; }
.tcard-wrap.flipping { animation: shake 0.55s ease; }
@keyframes shake {
  0%, 100% { transform: translateY(0); }
  25% { transform: translateY(-8px) rotate(-2deg); }
  50% { transform: translateY(0) rotate(2deg); }
  75% { transform: translateY(-4px); }
}

.tcard-face {
  position: absolute; inset: 0; border-radius: 14px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  backface-visibility: hidden; -webkit-backface-visibility: hidden;
  box-shadow: 0 12px 34px rgba(2,6,18,0.6);
}
.tcard-face.back {
  background: linear-gradient(160deg, #1c2a55, #0a1230 60%, #101c42);
  border: 1px solid var(--border);
}
.tcard-face.back::before {
  content: ""; position: absolute; inset: 9px; border-radius: 9px;
  border: 1px solid rgba(216,177,90,0.45);
}
.back-symbol { font-size: 2.6rem; color: var(--gold-light); text-shadow: 0 0 18px rgba(216,177,90,0.6); }
.back-label { font-family: var(--font); font-size: 0.9rem; color: var(--gold-light); letter-spacing: 0.2em; margin-top: 0.6rem; }
.back-deco { position: absolute; width: 60px; height: 60px; border-radius: 50%; border: 1px solid rgba(216,177,90,0.3); }
.back-deco::after { content: ""; position: absolute; inset: 8px; border-radius: 50%; border: 1px solid rgba(216,177,90,0.25); }

.tcard-face.front { transform: rotateY(180deg); color: #eaf1ff; }
.rank-da { background: radial-gradient(circle at 50% 22%, rgba(246,225,164,0.35), transparent 60%), linear-gradient(165deg, #4a3a12, #1c1508); border: 1px solid #f6e1a4; }
.rank-ji { background: radial-gradient(circle at 50% 22%, rgba(127,168,232,0.35), transparent 60%), linear-gradient(165deg, #17315f, #0a1428); border: 1px solid #7fa8e8; }
.rank-zhong { background: radial-gradient(circle at 50% 22%, rgba(107,215,192,0.3), transparent 60%), linear-gradient(165deg, #123f38, #0a1c18); border: 1px solid #6bd7c0; }
.rank-xiao { background: radial-gradient(circle at 50% 22%, rgba(201,160,255,0.3), transparent 60%), linear-gradient(165deg, #2b1a4a, #140c24); border: 1px solid #c9a0ff; }

.front-rank { font-family: var(--font); font-size: 1.7rem; font-weight: 700; letter-spacing: 0.3em; text-shadow: 0 0 16px currentColor; }
.rank-da .front-rank { color: #f6e1a4; }
.rank-ji .front-rank { color: #7fa8e8; }
.rank-zhong .front-rank { color: #6bd7c0; }
.rank-xiao .front-rank { color: #c9a0ff; }
.front-symbol { font-size: 3rem; margin: 0.5rem 0 0.2rem; filter: drop-shadow(0 0 14px rgba(255,255,255,0.3)); }
.front-name { font-family: var(--font); font-size: 1.05rem; letter-spacing: 0.06em; opacity: 0.95; }

/* ---------- 结果详情 ---------- */
.fc-result { margin-top: 1.5rem; }
.r-fortune { max-width: 520px; margin: 0 auto; color: #d7e3ff; font-size: 0.92rem; line-height: 1.9; }
.r-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; max-width: 460px; margin: 1.2rem auto 0; }
.r-item {
  display: flex; align-items: center; justify-content: space-between; gap: 0.6rem;
  padding: 0.6rem 0.9rem; border-radius: 10px;
  background: rgba(255,255,255,0.04); border: 1px solid rgba(159,178,216,0.22);
}
.r-k { color: var(--muted); font-size: 0.8rem; flex: none; }
.r-v { color: var(--text); font-size: 0.9rem; display: flex; align-items: center; gap: 6px; }
.r-v.num { font-family: var(--font); font-size: 1.1rem; font-weight: 700; color: var(--gold-light); }
.r-v.good { color: #7fe0ac; }
.r-v.bad { color: #ff9a9a; }
.swatch { display: inline-block; width: 14px; height: 14px; border-radius: 50%; box-shadow: 0 0 0 1px rgba(255,255,255,0.2); }

.rise-enter-active { transition: opacity 0.5s, transform 0.5s; }
.rise-enter-from { opacity: 0; transform: translateY(14px); }

@media (max-width: 640px) {
  .r-grid { grid-template-columns: 1fr; }
  .tcard-wrap { width: 150px; height: 214px; }
}
</style>
