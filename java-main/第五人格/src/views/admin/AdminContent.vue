<template>
  <div class="admin-page">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-card"><div class="stat-num">{{ stats.total }}</div><div class="stat-label">内容总数</div></div>
      <div class="stat-card ok"><div class="stat-num">{{ stats.published }}</div><div class="stat-label">已发布</div></div>
      <div class="stat-card warn"><div class="stat-num">{{ stats.draft }}</div><div class="stat-label">草稿 / 下架</div></div>
      <div class="stat-card"><div class="stat-num">{{ stats.categoryCount }}</div><div class="stat-label">分类总数</div></div>
    </div>

    <!-- 工具栏 + 表格 -->
    <div class="panel">
      <div class="toolbar">
        <div class="filters">
          <input v-model="filters.keyword" class="inp" placeholder="搜索标题 / 摘要 / 标签…" @keyup.enter="onSearch" />
          <select v-model="filters.categoryId" class="inp" @change="onFilterChange">
            <option :value="null">全部分类</option>
            <optgroup v-for="g in groupedCategories" :key="g.topName" :label="g.topName">
              <option v-for="o in g.items" :key="o.id" :value="o.id">{{ o.name }}</option>
            </optgroup>
          </select>
          <select v-model="filters.status" class="inp" @change="onFilterChange">
            <option value="">全部状态</option>
            <option :value="1">已发布</option>
            <option :value="0">草稿 / 下架</option>
          </select>
          <button class="btn-ghost sm" @click="onSearch">查询</button>
          <button class="btn-ghost sm" @click="resetFilters">重置</button>
        </div>
        <button class="btn-primary sm" @click="openCreate">+ 新增内容</button>
      </div>

      <div v-if="loading" class="empty">加载中…</div>
      <table v-else class="admin-table">
        <thead>
          <tr>
            <th class="c-id">ID</th>
            <th>标题</th>
            <th>分类</th>
            <th>状态</th>
            <th>作者</th>
            <th class="c-num">浏览</th>
            <th>创建时间</th>
            <th class="c-act">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in list" :key="row.id">
            <td class="c-id">{{ row.id }}</td>
            <td class="t-title">{{ row.title }}</td>
            <td><span class="cat">{{ catLabel(row) }}</span></td>
            <td>
              <span class="badge" :class="row.status === 1 ? 'on' : 'off'">{{ row.status === 1 ? '已发布' : '草稿' }}</span>
            </td>
            <td>{{ row.author || '—' }}</td>
            <td class="c-num">{{ row.viewCount || 0 }}</td>
            <td>{{ fmt(row.createdAt) }}</td>
            <td class="c-act">
              <button class="link-btn" @click="openEdit(row)">编辑</button>
              <button class="link-btn" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '发布' }}</button>
              <button class="link-btn danger" @click="onDelete(row)">删除</button>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="8" class="empty">暂无内容，点击「新增内容」开始维护首页精选。</td></tr>
        </tbody>
      </table>

      <div class="pager" v-if="total > 0">
        <button class="btn-ghost sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <span class="pager-info">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</span>
        <button class="btn-ghost sm" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
      </div>
    </div>

    <!-- 新增 / 编辑 弹窗 -->
    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal">
        <header class="modal-head">
          <h3>{{ editingId ? '编辑内容' : '新增内容' }}</h3>
          <button class="modal-close" @click="closeForm">×</button>
        </header>
        <div class="modal-body">
          <div class="field">
            <label>标题 *</label>
            <input v-model="form.title" class="inp full" placeholder="请输入标题" />
            <small class="err">{{ errors.title }}</small>
          </div>
          <div class="field">
            <label>摘要</label>
            <textarea v-model="form.summary" class="inp full" rows="2" placeholder="一句话摘要"></textarea>
          </div>
          <div class="grid2">
            <div class="field">
              <label>分类 *</label>
              <select v-model="form.categoryId" class="inp full">
                <option :value="null">请选择分类</option>
                <optgroup v-for="g in groupedCategories" :key="g.topName" :label="g.topName">
                  <option v-for="o in g.items" :key="o.id" :value="o.id">{{ o.name }}</option>
                </optgroup>
              </select>
              <small class="err">{{ errors.categoryId }}</small>
            </div>
            <div class="field">
              <label>状态</label>
              <select v-model="form.status" class="inp full">
                <option :value="1">已发布</option>
                <option :value="0">草稿 / 下架</option>
              </select>
            </div>
          </div>
          <div class="grid2">
            <div class="field">
              <label>作者 / 来源</label>
              <input v-model="form.author" class="inp full" placeholder="如：官方" />
            </div>
            <div class="field">
              <label>封面图 URL</label>
              <input v-model="form.coverUrl" class="inp full" placeholder="https://…" />
            </div>
          </div>
          <div class="field">
            <label>标签（逗号分隔）</label>
            <input v-model="form.tags" class="inp full" placeholder="如：监管者,技巧" />
          </div>
          <div class="field">
            <label>正文</label>
            <textarea v-model="form.content" class="inp full" rows="4" placeholder="正文内容（详情页用）"></textarea>
          </div>
        </div>
        <footer class="modal-foot">
          <button class="btn-ghost" @click="closeForm">取消</button>
          <button class="btn-primary" :disabled="saving" @click="save">{{ saving ? '保 存 中 …' : '保 存' }}</button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { useAdminAuth } from '../../stores/adminAuth'
import { ApiError } from '../../api/http'
import {
  fetchContent, fetchStats, fetchCategoryOptions,
  createContent, updateContent, deleteContent, setContentStatus
} from '../../api/admin'

const router = useRouter()
const { show } = useToast()
const { signOut } = useAdminAuth()

const stats = reactive({ total: 0, published: 0, draft: 0, categoryCount: 0 })
const categories = ref([])
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const filters = reactive({ keyword: '', categoryId: null, status: '' })

const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = reactive({ title: '', summary: '', categoryId: null, status: 1, author: '', coverUrl: '', tags: '', content: '' })
const errors = reactive({ title: '', categoryId: '' })

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const groupedCategories = computed(() => {
  const map = {}
  for (const o of categories.value) {
    if (!map[o.topName]) map[o.topName] = []
    map[o.topName].push(o)
  }
  return Object.entries(map).map(([topName, items]) => ({ topName, items }))
})

function catLabel(row) {
  const o = categories.value.find((c) => c.id === row.categoryId)
  if (!o) return '—'
  return o.topName ? `${o.topName} / ${o.name}` : o.name
}

function fmt(s) {
  if (!s) return '—'
  return String(s).replace('T', ' ').slice(0, 16)
}

async function loadStats() {
  try {
    const s = await fetchStats()
    Object.assign(stats, s)
  } catch (e) { /* 统计失败不阻断主流程 */ }
}

async function loadList() {
  loading.value = true
  try {
    const data = await fetchContent({
      keyword: filters.keyword,
      categoryId: filters.categoryId,
      status: filters.status,
      page: page.value,
      size: size.value
    })
    list.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    if (e instanceof ApiError && e.code === 401) {
      signOut()
      router.replace({ name: 'admin-login' })
    } else {
      show(e instanceof ApiError ? e.message : '加载失败')
    }
  } finally {
    loading.value = false
  }
}

function onSearch() { page.value = 1; loadList() }
function onFilterChange() { page.value = 1; loadList() }
function resetFilters() {
  filters.keyword = ''
  filters.categoryId = null
  filters.status = ''
  page.value = 1
  loadList()
}
function changePage(p) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadList()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { title: '', summary: '', categoryId: null, status: 1, author: '', coverUrl: '', tags: '', content: '' })
  errors.title = ''
  errors.categoryId = ''
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    summary: row.summary || '',
    categoryId: row.categoryId,
    status: row.status === 1 ? 1 : 0,
    author: row.author || '',
    coverUrl: row.coverUrl || '',
    tags: row.tags || '',
    content: row.content || ''
  })
  errors.title = ''
  errors.categoryId = ''
  showForm.value = true
}

function closeForm() { showForm.value = false }

async function save() {
  errors.title = ''
  errors.categoryId = ''
  let ok = true
  if (!form.title.trim()) { errors.title = '请输入标题'; ok = false }
  if (!form.categoryId) { errors.categoryId = '请选择分类'; ok = false }
  if (!ok) return

  saving.value = true
  try {
    const payload = { ...form, title: form.title.trim(), status: Number(form.status) }
    if (editingId.value) {
      await updateContent(editingId.value, payload)
      show('已保存修改')
    } else {
      await createContent(payload)
      show('已新增内容')
    }
    closeForm()
    loadList()
    loadStats()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await setContentStatus(row.id, next)
    row.status = next
    show(next === 1 ? '已发布' : '已下架')
    loadStats()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '操作失败')
  }
}

async function onDelete(row) {
  if (!confirm(`确定删除「${row.title}」？此操作不可恢复。`)) return
  try {
    await deleteContent(row.id)
    show('已删除')
    loadList()
    loadStats()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '删除失败')
  }
}

onMounted(async () => {
  await loadStats()
  try {
    categories.value = await fetchCategoryOptions()
  } catch (e) { /* 分类加载失败不阻断 */ }
  await loadList()
})
</script>
