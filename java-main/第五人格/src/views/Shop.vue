<template>
  <div class="shop">
    <NightSky />

    <!-- 顶部导航（简化版，含返回 + 搜索 + 购物车） -->
    <header class="shop-topbar">
      <div class="tb-inner">
        <div class="tb-brand" @click="$router.replace('/home')">
          <span class="tb-emblem">祭</span>
          <span class="tb-word">第五人格 · 周边商城</span>
        </div>
        <form class="tb-search" @submit.prevent="doSearch">
          <input v-model="kw" type="text" placeholder="搜索手办、抱枕、礼包、通行证…" />
          <button type="submit">搜索</button>
        </form>
        <a class="tb-tmall" :href="TMALL_SHOP" target="_blank" rel="noopener">🏬 天猫旗舰店</a>
        <button class="tb-cart" @click="cartOpen = true">
          🛒 购物车<span v-if="cartCount" class="cart-badge">{{ cartCount }}</span>
        </button>
      </div>
    </header>

    <!-- 分类 tab（淘宝风格） -->
    <div class="shop-tabs">
      <button
        v-for="t in tabs"
        :key="t"
        class="tab"
        :class="{ active: activeTab === t }"
        @click="activeTab = t"
      >{{ t }}</button>
    </div>

    <!-- 商品网格 -->
    <main class="shop-main">
      <div class="goods-grid">
        <article v-for="g in filtered" :key="g.id" class="goods-card">
          <!-- 缩略图（真实商品图，点击去淘宝） -->
          <div class="g-thumb" @click="goTaobao(g)">
            <img class="g-img" :src="g.img" :alt="g.title" loading="lazy" />
            <div v-if="g.badges" class="g-badges">
              <span v-for="b in g.badges" :key="b" class="g-badge" :class="b">{{ badgeText(b) }}</span>
            </div>
          </div>
          <!-- 信息 -->
          <div class="g-body">
            <h3 class="g-title" :title="g.title" @click="goTaobao(g)">{{ g.title }}</h3>
            <p class="g-sub">{{ g.sub }}</p>
            <div class="g-price">
              <span class="g-price-num"><i>¥</i>{{ g.price }}</span>
              <span v-if="g.origin" class="g-origin">¥{{ g.origin }}</span>
              <span class="g-sales">月销 {{ g.sales }}</span>
            </div>
            <div class="g-meta">
              <span class="g-shop">{{ g.shop }}</span>
              <span class="g-rating">★ {{ g.rating }}</span>
            </div>
            <div class="g-actions">
              <button class="g-buy" @click="goTaobao(g)">淘宝购买</button>
              <button class="g-add" @click="addCart(g)">加入购物车</button>
            </div>
          </div>
        </article>
      </div>

      <p v-if="!filtered.length" class="shop-empty">没有找到相关商品，换个关键词试试。</p>
    </main>

    <!-- 购物车抽屉 -->
    <Transition name="slide">
      <div v-if="cartOpen" class="cart-mask" @click.self="cartOpen = false">
        <div class="cart-panel">
          <div class="cart-head">
            <h3>购物车 <span class="cart-head-count">{{ cartCount }} 件</span></h3>
            <button class="cart-close" @click="cartOpen = false">×</button>
          </div>
          <div class="cart-list">
            <div v-for="(item, i) in cart" :key="item.id" class="cart-item">
              <div class="ci-thumb"><img class="ci-img" :src="item.img" :alt="item.title" /></div>
              <div class="ci-info">
                <p class="ci-title">{{ item.title }}</p>
                <p class="ci-price">¥{{ item.price }}</p>
              </div>
              <div class="ci-qty">
                <button @click="dec(i)">−</button>
                <span>{{ item.qty }}</span>
                <button @click="inc(i)">＋</button>
              </div>
              <button class="ci-del" @click="remove(i)">删除</button>
            </div>
            <p v-if="!cart.length" class="cart-empty">购物车还是空的，去逛逛吧～</p>
          </div>
          <div class="cart-foot">
            <div class="cart-total">合计：<span>¥{{ cartTotal }}</span></div>
            <button class="cart-checkout" :disabled="!cart.length" @click="checkout">去结算</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 结算弹窗 -->
    <Transition name="pop">
      <div v-if="checkoutOpen" class="checkout-mask" @click.self="checkoutOpen = false">
        <div class="checkout-panel">
          <div class="co-head">确认订单</div>
          <div class="co-body">
            <div class="co-row"><span>商品数量</span><span>{{ cartCount }} 件</span></div>
            <div class="co-row"><span>商品金额</span><span>¥{{ cartTotal }}</span></div>
            <div class="co-row"><span>运费</span><span class="co-free">包邮</span></div>
            <div class="co-row total"><span>实付款</span><span>¥{{ cartTotal }}</span></div>
          </div>
          <div class="co-actions">
            <button class="co-cancel" @click="checkoutOpen = false">再想想</button>
            <button class="co-pay" @click="pay">提交订单</button>
          </div>
        </div>
      </div>
    </Transition>

    <footer class="shop-footer">
      <p>© 第五人格 · 周边商城 · 演示页面 · 原创视觉，非官方</p>
      <p class="foot-sub">星辉为引 · 好物相伴</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import NightSky from '../components/NightSky.vue'
import { useToast } from '../composables/useToast'

const { show } = useToast()

// 淘宝风格分类
const tabs = ['全部', '手办模型', '抱枕玩偶', '徽章立牌', '数字礼包', '赛季通行证', '服饰周边']
const activeTab = ref('全部')
const kw = ref('')

// 商品数据（演示用，参考真实第五人格周边，taobaoQ 为淘宝搜索关键词）
const goods = [
  { id: 1, cat: '手办模型', title: '祭司·菲欧娜 1/7 比例手办 星辉之钥', sub: '官方正品 · 高精度雕像', img: '/shop/priestess-figure.png', price: 699, origin: 899, sales: 1280, shop: '第五人格官方旗舰店', rating: 4.9, badges: ['hot', 'free', 'official'], taobaoQ: '第五人格 祭司 手办' },
  { id: 2, cat: '手办模型', title: '红蝶·美智子 Q版手办 蝶影翩跹', sub: '可爱 Q 版 · 全彩涂装', img: '/shop/butterfly-figure.png', price: 199, origin: 259, sales: 3560, shop: '官方旗舰店', rating: 4.8, badges: ['hot', 'free'], taobaoQ: '第五人格 红蝶 手办' },
  { id: 3, cat: '手办模型', title: '监管者·杰克 雕像 绅士之雾', sub: '限量编号 · 收藏级', img: '/shop/hunter-statue.png', price: 1299, origin: 1599, sales: 320, shop: '官方旗舰店', rating: 5.0, badges: ['limited', 'official'], taobaoQ: '第五人格 杰克 手办 雕像' },
  { id: 4, cat: '抱枕玩偶', title: '祭司 等身抱枕 星辉入梦', sub: '双面印刷 · 亲肤面料', img: '/shop/pillow.png', price: 129, origin: 169, sales: 2100, shop: '官方旗舰店', rating: 4.7, badges: ['free'], taobaoQ: '第五人格 等身抱枕' },
  { id: 5, cat: '抱枕玩偶', title: '佣兵·奈布 Q版靠垫', sub: '回弹记忆棉', img: '/shop/cushion.png', price: 89, sales: 890, shop: '周边专营店', rating: 4.6, badges: ['free'], taobaoQ: '第五人格 靠垫 抱枕' },
  { id: 6, cat: '徽章立牌', title: '求生者全套 金属徽章套装', sub: '8 枚装 · 烤漆工艺', img: '/shop/badges.png', price: 168, origin: 208, sales: 1560, shop: '官方旗舰店', rating: 4.8, badges: ['hot', 'free'], taobaoQ: '第五人格 金属徽章' },
  { id: 7, cat: '徽章立牌', title: '监管者 亚克力立牌 3 件套', sub: '高透亚克力 · 送底座', img: '/shop/standee.png', price: 99, sales: 720, shop: '周边专营店', rating: 4.7, badges: ['free'], taobaoQ: '第五人格 亚克力立牌' },
  { id: 8, cat: '数字礼包', title: '钻石礼包 · 128 元档', sub: '充值即到账 · 附赠线索', img: '/shop/diamond-pack.png', price: 128, sales: 8900, shop: '官方直充', rating: 4.9, badges: ['hot', 'official'], taobaoQ: '第五人格 钻石 代充' },
  { id: 9, cat: '数字礼包', title: '线索礼包 · 超值补给', sub: '开箱必备 · 限时加赠', img: '/shop/clue-pack.png', price: 68, origin: 88, sales: 5600, shop: '官方直充', rating: 4.8, badges: ['hot', 'free'], taobaoQ: '第五人格 线索 代充' },
  { id: 10, cat: '赛季通行证', title: '第三十九赛季 通行证', sub: '解锁专属时装与奖励', img: '/shop/pass.png', price: 45, sales: 12000, shop: '官方直充', rating: 4.9, badges: ['hot', 'official', 'free'], taobaoQ: '第五人格 赛季通行证' },
  { id: 11, cat: '赛季通行证', title: '赛季通行证 · 豪华版', sub: '含 20 级直升 + 限定头像框', img: '/shop/pass-deluxe.png', price: 128, origin: 168, sales: 3400, shop: '官方直充', rating: 4.9, badges: ['hot'], taobaoQ: '第五人格 通行证 代充' },
  { id: 12, cat: '服饰周边', title: '第五人格 主题印花 T恤', sub: '纯棉 · 多角色可选', img: '/shop/tshirt.png', price: 79, origin: 99, sales: 4500, shop: '官方旗舰店', rating: 4.6, badges: ['free'], taobaoQ: '第五人格 T恤' },
  { id: 13, cat: '服饰周边', title: '祭司 主题连帽卫衣', sub: '加绒加厚 · 冬季限定', img: '/shop/hoodie.png', price: 199, origin: 249, sales: 980, shop: '官方旗舰店', rating: 4.7, badges: ['free'], taobaoQ: '第五人格 卫衣' },
  { id: 14, cat: '服饰周边', title: '角色 钥匙扣 挂件盲盒', sub: '随机角色 · 抽隐藏款', img: '/shop/keychain.png', price: 39, sales: 7600, shop: '周边专营店', rating: 4.5, badges: ['hot', 'free'], taobaoQ: '第五人格 钥匙扣 挂件' },
  { id: 15, cat: '数字礼包', title: '月卡 · 每日钻石补给', sub: '30 天 · 性价比之王', img: '/shop/month-card.png', price: 30, sales: 15000, shop: '官方直充', rating: 4.9, badges: ['hot', 'official'], taobaoQ: '第五人格 月卡 代充' },
  { id: 16, cat: '手办模型', title: '深渊的呼唤 纪念雕像 套装', sub: 'COA 限定 · 典藏礼盒', img: '/shop/statue-gift.png', price: 899, origin: 1099, sales: 210, shop: '官方旗舰店', rating: 5.0, badges: ['limited', 'official'], taobaoQ: '第五人格 深渊的呼唤 手办' },
]

// 天猫第五人格官方旗舰店（真实店铺）
const TMALL_SHOP = 'https://shop382752633.m.taobao.com'

function taobaoUrl(g) {
  return `https://s.taobao.com/search?q=${encodeURIComponent(g.taobaoQ || g.title)}`
}
function goTaobao(g) {
  window.open(taobaoUrl(g), '_blank')
}

const filtered = computed(() => {
  let list = goods
  if (activeTab.value !== '全部') list = list.filter(g => g.cat === activeTab.value)
  const q = kw.value.trim()
  if (q) list = list.filter(g => g.title.includes(q) || g.sub.includes(q) || g.cat.includes(q))
  return list
})

function badgeText(b) {
  return { hot: '热卖', free: '包邮', official: '官方正品', limited: '限量' }[b] || b
}

// 购物车
const cart = ref([])
const cartOpen = ref(false)
const checkoutOpen = ref(false)

const cartCount = computed(() => cart.value.reduce((s, i) => s + i.qty, 0))
const cartTotal = computed(() => cart.value.reduce((s, i) => s + i.qty * i.price, 0))

function addCart(g) {
  const item = cart.value.find(i => i.id === g.id)
  if (item) item.qty++
  else cart.value.push({ ...g, qty: 1 })
  show(`已加入购物车：${g.title}`)
}
function inc(i) { cart.value[i].qty++ }
function dec(i) {
  const it = cart.value[i]
  it.qty--
  if (it.qty <= 0) cart.value.splice(i, 1)
}
function remove(i) { cart.value.splice(i, 1) }

function checkout() {
  if (!cart.value.length) return
  cartOpen.value = false
  checkoutOpen.value = true
}
function pay() {
  checkoutOpen.value = false
  cart.value = []
  show('演示下单完成，请前往淘宝完成真实购买 ✦')
}
function doSearch() { /* 关键词由 filtered 计算属性响应 */ }
</script>

<style scoped>
.shop { position: relative; min-height: 100vh; color: var(--text); z-index: 1; }

/* 顶栏 */
.shop-topbar { position: sticky; top: 0; z-index: 40; background: rgba(6, 12, 28, 0.92); backdrop-filter: blur(8px); border-bottom: 1px solid var(--border); }
.tb-inner { max-width: 1180px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; gap: 18px; }
.tb-brand { display: flex; align-items: center; gap: 10px; cursor: pointer; flex-shrink: 0; }
.tb-emblem { width: 34px; height: 34px; border-radius: 50%; background: var(--gold); color: #1a1208; display: grid; place-items: center; font-weight: 800; font-size: 16px; }
.tb-word { font-family: var(--font); color: var(--gold-light); font-size: 16px; letter-spacing: 1px; }
.tb-search { flex: 1; display: flex; max-width: 520px; margin: 0 auto; }
.tb-search input { flex: 1; height: 38px; padding: 0 14px; border: 2px solid var(--border); border-right: none; border-radius: 20px 0 0 20px; background: rgba(255,255,255,0.06); color: var(--text); font-size: 14px; outline: none; }
.tb-search input:focus { border-color: var(--gold); }
.tb-search button { height: 38px; padding: 0 22px; border: none; border-radius: 0 20px 20px 0; background: linear-gradient(180deg, var(--gold-light), var(--gold)); color: #1a1208; font-weight: 700; cursor: pointer; }
.tb-cart { position: relative; height: 38px; padding: 0 16px; border: 1px solid var(--border); border-radius: 20px; background: transparent; color: var(--text); font-size: 14px; cursor: pointer; flex-shrink: 0; }
.tb-cart:hover { border-color: var(--gold); }
.tb-tmall { display: inline-flex; align-items: center; height: 38px; padding: 0 16px; border: 1px solid #ff5000; border-radius: 20px; background: rgba(255,80,0,0.12); color: #ff8a4c; font-size: 13px; text-decoration: none; flex-shrink: 0; transition: .18s; }
.tb-tmall:hover { background: #ff5000; color: #fff; }
.cart-badge { position: absolute; top: -8px; right: -6px; min-width: 18px; height: 18px; padding: 0 5px; border-radius: 9px; background: #ff4d4f; color: #fff; font-size: 11px; line-height: 18px; }

/* 分类 tab */
.shop-tabs { max-width: 1180px; margin: 16px auto 0; padding: 0 20px; display: flex; gap: 8px; flex-wrap: wrap; }
.tab { padding: 8px 18px; border: 1px solid var(--border); border-radius: 20px; background: transparent; color: var(--muted); font-size: 13px; cursor: pointer; transition: .18s; }
.tab:hover { color: var(--gold-light); border-color: var(--gold); }
.tab.active { background: linear-gradient(180deg, var(--gold-light), var(--gold)); color: #1a1208; border-color: var(--gold); font-weight: 700; }

/* 商品网格 */
.shop-main { max-width: 1180px; margin: 18px auto; padding: 0 20px; }
.goods-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px; }
.goods-card { background: #fff; color: #333; border-radius: 12px; overflow: hidden; cursor: pointer; transition: .2s; box-shadow: 0 4px 16px rgba(0,0,0,0.35); }
.goods-card:hover { transform: translateY(-6px); box-shadow: 0 12px 32px rgba(0,0,0,0.5); }
.g-thumb { position: relative; height: 190px; overflow: hidden; background: #f5f5f5; cursor: pointer; }
.g-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.g-badges { position: absolute; top: 8px; left: 8px; display: flex; gap: 4px; }
.g-badge { padding: 2px 8px; border-radius: 4px; font-size: 11px; color: #fff; }
.g-badge.hot { background: #ff4d4f; }
.g-badge.free { background: #fa8c16; }
.g-badge.official { background: #1890ff; }
.g-badge.limited { background: #722ed1; }
.g-body { padding: 12px 14px 14px; }
.g-title { font-size: 14px; font-weight: 700; color: #1a1a1a; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; cursor: pointer; }
.g-title:hover { color: #ff5000; }
.g-sub { font-size: 12px; color: #999; margin: 4px 0 8px; }
.g-price { display: flex; align-items: baseline; gap: 8px; }
.g-price-num { color: #ff4d4f; font-size: 20px; font-weight: 800; }
.g-price-num i { font-style: normal; font-size: 13px; }
.g-origin { color: #bbb; font-size: 12px; text-decoration: line-through; }
.g-sales { color: #999; font-size: 12px; margin-left: auto; }
.g-meta { display: flex; align-items: center; justify-content: space-between; margin: 8px 0 10px; font-size: 12px; color: #999; }
.g-rating { color: #fa8c16; }
.g-actions { display: flex; gap: 8px; }
.g-buy, .g-add { flex: 1; height: 32px; border: none; border-radius: 6px; font-size: 13px; cursor: pointer; font-weight: 600; }
.g-buy { background: #ff4d4f; color: #fff; }
.g-buy:hover { background: #ff7875; }
.g-add { background: #fff1f0; color: #ff4d4f; border: 1px solid #ffccc7; }
.g-add:hover { background: #ffe7e6; }

.shop-empty { text-align: center; color: var(--muted); padding: 60px 0; }

/* 购物车抽屉 */
.cart-mask { position: fixed; inset: 0; z-index: 60; background: rgba(0,0,0,0.5); }
.cart-panel { position: absolute; top: 0; right: 0; bottom: 0; width: 400px; max-width: 92vw; background: #0c1220; display: flex; flex-direction: column; box-shadow: -8px 0 30px rgba(0,0,0,0.5); }
.cart-head { display: flex; align-items: center; justify-content: space-between; padding: 18px 20px; border-bottom: 1px solid var(--border); }
.cart-head h3 { color: var(--gold-light); font-size: 17px; }
.cart-head-count { font-size: 13px; color: var(--muted); margin-left: 6px; }
.cart-close { border: none; background: none; color: var(--muted); font-size: 26px; cursor: pointer; }
.cart-list { flex: 1; overflow-y: auto; padding: 8px 20px; }
.cart-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.06); }
.ci-thumb { width: 52px; height: 52px; border-radius: 8px; overflow: hidden; flex-shrink: 0; }
.ci-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.ci-info { flex: 1; min-width: 0; }
.ci-title { font-size: 13px; color: var(--text); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ci-price { font-size: 14px; color: #ff4d4f; font-weight: 700; margin-top: 4px; }
.ci-qty { display: flex; align-items: center; gap: 8px; }
.ci-qty button { width: 24px; height: 24px; border: 1px solid var(--border); border-radius: 4px; background: transparent; color: var(--text); cursor: pointer; }
.ci-qty span { min-width: 18px; text-align: center; color: var(--text); }
.ci-del { border: none; background: none; color: var(--muted); font-size: 12px; cursor: pointer; }
.cart-empty { text-align: center; color: var(--muted); padding: 50px 0; }
.cart-foot { padding: 16px 20px; border-top: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; }
.cart-total { color: var(--text); font-size: 14px; }
.cart-total span { color: #ff4d4f; font-size: 22px; font-weight: 800; }
.cart-checkout { padding: 10px 32px; border: none; border-radius: 20px; background: linear-gradient(180deg, #ff7875, #ff4d4f); color: #fff; font-size: 15px; font-weight: 700; cursor: pointer; }
.cart-checkout:disabled { opacity: 0.4; cursor: not-allowed; }

/* 结算弹窗 */
.checkout-mask { position: fixed; inset: 0; z-index: 70; background: rgba(0,0,0,0.6); display: grid; place-items: center; }
.checkout-panel { width: 360px; max-width: 90vw; background: #fff; color: #333; border-radius: 12px; overflow: hidden; }
.co-head { padding: 16px 20px; font-weight: 700; font-size: 16px; border-bottom: 1px solid #eee; }
.co-body { padding: 16px 20px; }
.co-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #666; }
.co-row.total { font-size: 16px; color: #1a1a1a; font-weight: 700; border-top: 1px solid #eee; margin-top: 8px; padding-top: 14px; }
.co-row.total span:last-child { color: #ff4d4f; font-size: 22px; }
.co-free { color: #fa8c16; }
.co-actions { display: flex; gap: 0; border-top: 1px solid #eee; }
.co-actions button { flex: 1; height: 48px; border: none; font-size: 15px; cursor: pointer; }
.co-cancel { background: #f5f5f5; color: #666; }
.co-pay { background: #ff4d4f; color: #fff; font-weight: 700; }

/* 底部 */
.shop-footer { text-align: center; padding: 30px 0 40px; color: var(--muted); font-size: 12px; }
.foot-sub { color: var(--gold); margin-top: 6px; letter-spacing: 2px; }

/* 过渡动画 */
.slide-enter-active, .slide-leave-active { transition: opacity .25s; }
.slide-enter-active .cart-panel, .slide-leave-active .cart-panel { transition: transform .25s; }
.slide-enter-from, .slide-leave-to { opacity: 0; }
.slide-enter-from .cart-panel { transform: translateX(100%); }
.pop-enter-active, .pop-leave-active { transition: opacity .2s; }
.pop-enter-from, .pop-leave-to { opacity: 0; }
</style>
