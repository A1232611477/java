<template>
  <div class="admin-page">
    <div class="dash-head">
      <h2 class="section-title">门户数据概览</h2>
      <p class="dash-sub">首页「门户精选」与全站内容的状态一览。</p>
    </div>

    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-num">{{ stats.total }}</div>
        <div class="stat-label">内容总数</div>
      </div>
      <div class="stat-card ok">
        <div class="stat-num">{{ stats.published }}</div>
        <div class="stat-label">已发布</div>
      </div>
      <div class="stat-card warn">
        <div class="stat-num">{{ stats.draft }}</div>
        <div class="stat-label">草稿 / 下架</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.categoryCount }}</div>
        <div class="stat-label">分类总数</div>
      </div>
    </div>

    <div class="panel dash-tip">
      <h3>管理入口</h3>
      <p>进入 <RouterLink to="/admin/content" class="link-router">内容管理</RouterLink> 可对首页「门户精选」展示的内容进行新增、编辑、发布 / 下架与删除。</p>
      <p class="dash-note">提示：仅「已发布（status = 1）」的内容会出现在首页门户精选与搜索结果中。</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchStats } from '../../api/admin'
import { useToast } from '../../composables/useToast'
import { ApiError } from '../../api/http'
import { useAdminAuth } from '../../stores/adminAuth'

const router = useRouter()
const { show } = useToast()
const { signOut } = useAdminAuth()
const stats = reactive({ total: 0, published: 0, draft: 0, categoryCount: 0 })

onMounted(async () => {
  try {
    const s = await fetchStats()
    Object.assign(stats, s)
  } catch (e) {
    if (e instanceof ApiError && e.code === 401) {
      signOut()
      router.replace({ name: 'admin-login' })
    } else {
      show(e instanceof ApiError ? e.message : '加载失败')
    }
  }
})
</script>
