<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="load">刷新</el-button>
      <el-button type="success" v-permission="'system:menu:add'" @click="openAdd(null)">新增根菜单</el-button>
    </div>

    <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all
              :tree-props="{ children: 'children' }" style="margin-top: 12px">
      <el-table-column prop="menuName" label="菜单名" />
      <el-table-column prop="menuType" label="类型" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ typeLabel(row.menuType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由" />
      <el-table-column prop="perm" label="权限码" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" v-permission="'system:menu:add'" @click="openAdd(row)">新增子</el-button>
          <el-button size="small" v-permission="'system:menu:edit'" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" v-permission="'system:menu:remove'" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select v-model="form.parentId" :data="parentOptions" check-strictly
                          :props="{ label: 'menuName', children: 'children', value: 'id' }"
                          placeholder="不选则为根" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜单名" prop="menuName"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由"><el-input v-model="form.path" placeholder="目录如 /system,菜单如 user" /></el-form-item>
        <el-form-item label="组件"><el-input v-model="form.component" placeholder="如 system/user/index" /></el-form-item>
        <el-form-item label="权限码"><el-input v-model="form.perm" placeholder="如 system:user:list" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { menuTreeApi, menuAddApi, menuUpdateApi, menuRemoveApi } from '../../api/menu'

const loading = ref(false)
const saving = ref(false)
const tree = ref([])
const formRef = ref()
const dialog = reactive({ visible: false, title: '' })
const form = reactive({ id: null, parentId: null, menuName: '', menuType: 'C', path: '', component: '', perm: '', sort: 0 })
const rules = {
  menuName: [{ required: true, message: '请输入菜单名', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const parentOptions = computed(() => tree.value)

const typeLabel = (t) => ({ M: '目录', C: '菜单', F: '按钮' }[t] || t)

const load = async () => {
  loading.value = true
  try {
    tree.value = (await menuTreeApi()).data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.id = null; form.parentId = null; form.menuName = ''; form.menuType = 'C'
  form.path = ''; form.component = ''; form.perm = ''; form.sort = 0
}
const openAdd = (parent) => {
  resetForm()
  form.parentId = parent ? parent.id : null
  dialog.title = '新增菜单'; dialog.visible = true
}
const openEdit = (row) => {
  resetForm()
  form.id = row.id; form.parentId = row.parentId === 0 ? null : row.parentId
  form.menuName = row.menuName; form.menuType = row.menuType; form.path = row.path
  form.component = row.component; form.perm = row.perm; form.sort = row.sort || 0
  dialog.title = '编辑菜单'; dialog.visible = true
}
const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form, parentId: form.parentId || 0 }
    payload.id ? await menuUpdateApi(payload) : await menuAddApi(payload)
    ElMessage.success('保存成功')
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}
const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」?`, '提示', { type: 'warning' })
  await menuRemoveApi(row.id)
  ElMessage.success('删除成功')
  load()
}

load()
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; }
</style>
