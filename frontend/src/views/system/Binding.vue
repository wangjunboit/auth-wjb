<template>
  <el-card>
    <h3>账号绑定</h3>
    <el-table :data="rows" border style="margin-top: 12px">
      <el-table-column prop="label" label="平台" width="160" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.bound ? 'success' : 'info'">{{ row.bound ? '已绑定' : '未绑定' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="!row.bound" size="small" type="primary" @click="bind(row.provider)">绑定</el-button>
          <el-button v-else size="small" type="danger" @click="unbind(row.provider)">解绑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { oauthBindingsApi, oauthBindUrlApi, oauthUnbindApi } from '../../api/oauth'

const providers = [
  { provider: 'github', label: 'GitHub' },
  { provider: 'wechat', label: '微信(mock)' }
]
const rows = ref(providers.map(p => ({ ...p, bound: false })))

const load = async () => {
  const res = await oauthBindingsApi()
  const bound = new Set((res.data || []).map(b => b.provider))
  rows.value = providers.map(p => ({ ...p, bound: bound.has(p.provider) }))
}

const bind = async (provider) => {
  const res = await oauthBindUrlApi(provider)
  sessionStorage.setItem('oauth_provider', provider)
  sessionStorage.setItem('oauth_mode', 'bind')
  window.location.href = res.data
}

const unbind = async (provider) => {
  await ElMessageBox.confirm('确认解绑?', '提示', { type: 'warning' })
  await oauthUnbindApi(provider)
  ElMessage.success('已解绑')
  load()
}

onMounted(load)
</script>
