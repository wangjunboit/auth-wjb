<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="用户名" clearable style="width: 200px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" v-permission="'system:user:add'" @click="openAdd">新增</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border style="margin-top: 12px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" v-permission="'system:user:edit'" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" v-permission="'system:user:edit'" @click="openRoles(row)">分配角色</el-button>
          <el-button size="small" type="danger" v-permission="'system:user:remove'" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNo"
                   @current-change="onPage" />

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="460px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDlg.visible" title="分配角色" width="420px">
      <el-checkbox-group v-model="roleDlg.checked">
        <el-checkbox v-for="r in roleDlg.options" :key="r.id" :value="r.id" style="display:block">
          {{ r.roleName }}（{{ r.roleKey }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="roleDlg.saving" @click="onRolesSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userPageApi, userAddApi, userUpdateApi, userRemoveApi, userRoleIdsApi, userAssignRolesApi } from '../../api/user'
import { rolePageApi } from '../../api/role'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 10, username: '' })
const formRef = ref()
const dialog = reactive({ visible: false, title: '' })
const form = reactive({ id: null, username: '', password: '', nickname: '', phone: '', email: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const roleDlg = reactive({ visible: false, saving: false, userId: null, options: [], checked: [] })

const openRoles = async (row) => {
  roleDlg.userId = row.id
  if (!roleDlg.options.length) {
    roleDlg.options = (await rolePageApi({ pageNo: 1, pageSize: 100 })).data.records
  }
  roleDlg.checked = (await userRoleIdsApi(row.id)).data || []
  roleDlg.visible = true
}
const onRolesSave = async () => {
  roleDlg.saving = true
  try {
    await userAssignRolesApi({ userId: roleDlg.userId, roleIds: roleDlg.checked })
    ElMessage.success('分配成功(对方重新登录后生效)')
    roleDlg.visible = false
  } finally {
    roleDlg.saving = false
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await userPageApi(query)
    rows.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const onPage = (p) => { query.pageNo = p; load() }

const resetForm = () => {
  form.id = null; form.username = ''; form.password = ''
  form.nickname = ''; form.phone = ''; form.email = ''
}

const openAdd = () => {
  resetForm()
  dialog.title = '新增用户'
  dialog.visible = true
}

const openEdit = (row) => {
  resetForm()
  form.id = row.id; form.username = row.username; form.nickname = row.nickname
  form.phone = row.phone; form.email = row.email
  dialog.title = '编辑用户'
  dialog.visible = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await userUpdateApi(form)
    } else {
      await userAddApi(form)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}

const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」?`, '提示', { type: 'warning' })
  await userRemoveApi(row.id)
  ElMessage.success('删除成功')
  load()
}

load()
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; }
.pager { margin-top: 12px; justify-content: flex-end; display: flex; }
</style>
