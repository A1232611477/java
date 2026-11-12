<template>
  <div class="admin-page">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-card"><div class="stat-num">{{ stats.total }}</div><div class="stat-label">账号总数</div></div>
      <div class="stat-card ok"><div class="stat-num">{{ stats.adminCount }}</div><div class="stat-label">管理员</div></div>
      <div class="stat-card warn"><div class="stat-num">{{ stats.disabled }}</div><div class="stat-label">已禁用</div></div>
      <div class="stat-card"><div class="stat-num">{{ stats.total - stats.disabled }}</div><div class="stat-label">正常账号</div></div>
    </div>

    <!-- 工具栏 + 表格 -->
    <div class="panel">
      <div class="toolbar">
        <div class="filters">
          <input v-model="filters.keyword" class="inp" placeholder="搜索昵称…" @keyup.enter="onSearch" />
          <select v-model="filters.role" class="inp" @change="onFilterChange">
            <option value="">全部角色</option>
            <option value="USER">普通用户</option>
            <option value="ADMIN">管理员</option>
          </select>
          <select v-model="filters.status" class="inp" @change="onFilterChange">
            <option value="">全部状态</option>
            <option value="0">正常</option>
            <option value="1">已禁用</option>
          </select>
          <button class="btn-ghost sm" @click="onSearch">查询</button>
          <button class="btn-ghost sm" @click="resetFilters">重置</button>
        </div>
        <button class="btn-primary sm" @click="openCreate">+ 新建账号</button>
      </div>

      <div v-if="loading" class="empty">加载中…</div>
      <table v-else class="admin-table">
        <thead>
          <tr>
            <th class="c-id">ID</th>
            <th>昵称</th>
            <th>角色</th>
            <th>状态</th>
            <th>创建时间</th>
            <th class="c-act">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in list" :key="row.id">
            <td class="c-id">{{ row.id }}</td>
            <td class="t-title">
              {{ row.nickname }}
              <span v-if="row.nickname === 'admin'" class="root-tag" title="内置超级管理员">超级</span>
            </td>
            <td>
              <span class="badge" :class="row.role === 'ADMIN' ? 'on' : 'off'">{{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
            </td>
            <td>
              <span class="badge" :class="row.status === 1 ? 'off' : 'on'">{{ row.status === 1 ? '已禁用' : '正常' }}</span>
            </td>
            <td>{{ fmt(row.createdAt) }}</td>
            <td class="c-act">
              <button class="link-btn" @click="openEdit(row)">编辑</button>
              <button class="link-btn" @click="openResetPwd(row)">重置密码</button>
              <button v-if="row.nickname !== 'admin'" class="link-btn" @click="toggleStatus(row)">{{ row.status === 1 ? '启用' : '禁用' }}</button>
              <button v-if="row.nickname !== 'admin'" class="link-btn danger" @click="onDelete(row)">删除</button>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="6" class="empty">暂无账号。</td></tr>
        </tbody>
      </table>

      <div class="pager" v-if="total > 0">
        <button class="btn-ghost sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <span class="pager-info">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</span>
        <button class="btn-ghost sm" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
      </div>
    </div>

    <!-- 新建 / 编辑 弹窗 -->
    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal">
        <header class="modal-head">
          <h3>{{ editingId ? '编辑账号' : '新建账号' }}</h3>
          <button class="modal-close" @click="closeForm">×</button>
        </header>
        <div class="modal-body">
          <div class="field">
            <label>昵称 *</label>
            <input v-model="form.nickname" class="inp full" placeholder="请输入昵称" />
            <small class="err">{{ errors.nickname }}</small>
          </div>
          <div v-if="!editingId" class="field">
            <label>密码 *</label>
            <input v-model="form.password" type="password" class="inp full" placeholder="8-20 位，可含字母、数字及 !@#$%^&*." />
            <small class="err">{{ errors.password }}</small>
          </div>
          <div class="grid2">
            <div class="field">
              <label>角色</label>
              <select v-model="form.role" class="inp full" :disabled="editingId && isSelfOrRoot">
                <option value="USER">普通用户</option>
                <option value="ADMIN">管理员</option>
              </select>
            </div>
            <div class="field">
              <label>状态</label>
              <select v-model="form.status" class="inp full" :disabled="editingId && isSelfOrRoot">
                <option :value="0">正常</option>
                <option :value="1">禁用</option>
              </select>
            </div>
          </div>
          <p v-if="editingId && isSelfOrRoot" class="hint-line">内置超级管理员账号不可修改角色与状态。</p>
        </div>
        <footer class="modal-foot">
          <button class="btn-ghost" @click="closeForm">取消</button>
          <button class="btn-primary" :disabled="saving" @click="save">{{ saving ? '保 存 中 …' : '保 存' }}</button>
        </footer>
      </div>
    </div>

    <!-- 重置密码弹窗 -->
    <div v-if="showPwd" class="modal-mask" @click.self="closePwd">
      <div class="modal">
        <header class="modal-head">
          <h3>重置密码</h3>
          <button class="modal-close" @click="closePwd">×</button>
        </header>
        <div class="modal-body">
          <p class="pwd-tip">为账号「{{ pwdTarget?.nickname }}」设置新密码。</p>
          <div class="field">
            <label>新密码 *</label>
            <input v-model="pwdForm.password" type="password" class="inp full" placeholder="8-20 位" />
            <small class="err">{{ pwdErrors.password }}</small>
          </div>
        </div>
        <footer class="modal-foot">
          <button class="btn-ghost" @click="closePwd">取消</button>
          <button class="btn-primary" :disabled="saving" @click="doResetPwd">{{ saving ? '提 交 中 …' : '确 定' }}</button>
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
  fetchUsers, fetchUserStats,
  createUser, updateUser, setUserStatus, resetUserPassword, deleteUser
} from '../../api/admin'

const router = useRouter()
const { show } = useToast()
const { user: currentAdmin, signOut } = useAdminAuth()

const stats = reactive({ total: 0, adminCount: 0, disabled: 0 })
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const filters = reactive({ keyword: '', role: '', status: '' })

const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = reactive({ nickname: '', password: '', role: 'USER', status: 0 })
const errors = reactive({ nickname: '', password: '' })

const showPwd = ref(false)
const pwdTarget = ref(null)
const pwdForm = reactive({ password: '' })
const pwdErrors = reactive({ password: '' })

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

// 正在编辑的是否为「当前登录管理员自己」或「内置超级管理员」
const isSelfOrRoot = computed(() => {
  if (!editingId.value) return false
  const row = list.value.find((u) => u.id === editingId.value)
  if (!row) return false
  return row.nickname === 'admin' || row.id === (currentAdmin.value?.id)
})

function fmt(s) {
  if (!s) return '—'
  return String(s).replace('T', ' ').slice(0, 16)
}

async function loadStats() {
  try {
    const s = await fetchUserStats()
    Object.assign(stats, s)
  } catch (e) { /* 统计失败不阻断主流程 */ }
}

async function loadList() {
  loading.value = true
  try {
    const data = await fetchUsers({
      keyword: filters.keyword,
      role: filters.role,
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
  filters.role = ''
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
  Object.assign(form, { nickname: '', password: '', role: 'USER', status: 0 })
  errors.nickname = ''
  errors.password = ''
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    nickname: row.nickname,
    password: '',
    role: row.role === 'ADMIN' ? 'ADMIN' : 'USER',
    status: row.status === 1 ? 1 : 0
  })
  errors.nickname = ''
  errors.password = ''
  showForm.value = true
}

function closeForm() { showForm.value = false }

async function save() {
  errors.nickname = ''
  errors.password = ''
  let ok = true
  if (!form.nickname.trim()) { errors.nickname = '请输入昵称'; ok = false }
  if (!editingId.value) {
    if (!form.password) { errors.password = '请输入密码'; ok = false }
    else if (form.password.length < 8 || form.password.length > 20) { errors.password = '密码需 8-20 位'; ok = false }
  }
  if (!ok) return

  saving.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, {
        nickname: form.nickname.trim(),
        role: form.role,
        status: Number(form.status)
      })
      show('已保存修改')
    } else {
      await createUser({
        nickname: form.nickname.trim(),
        password: form.password,
        role: form.role,
        status: Number(form.status)
      })
      show('已新建账号')
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

function openResetPwd(row) {
  pwdTarget.value = row
  pwdForm.password = ''
  pwdErrors.password = ''
  showPwd.value = true
}

function closePwd() { showPwd.value = false }

async function doResetPwd() {
  pwdErrors.password = ''
  if (!pwdForm.password) { pwdErrors.password = '请输入新密码'; return }
  if (pwdForm.password.length < 8 || pwdForm.password.length > 20) { pwdErrors.password = '密码需 8-20 位'; return }
  saving.value = true
  try {
    await resetUserPassword(pwdTarget.value.id, pwdForm.password)
    show(`已重置「${pwdTarget.value.nickname}」的密码`)
    closePwd()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '重置失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await setUserStatus(row.id, next)
    row.status = next
    show(next === 1 ? '已禁用' : '已启用')
    loadStats()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '操作失败')
  }
}

async function onDelete(row) {
  if (!confirm(`确定删除账号「${row.nickname}」？此操作不可恢复。`)) return
  try {
    await deleteUser(row.id)
    show('已删除')
    loadList()
    loadStats()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '删除失败')
  }
}

onMounted(async () => {
  await loadStats()
  await loadList()
})
</script>
