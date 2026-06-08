<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.roleName" placeholder="角色名" clearable style="width: 200px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" v-permission="'system:role:add'" @click="openAdd">新增</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border style="margin-top: 12px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名" />
      <el-table-column prop="roleKey" label="角色标识" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" v-permission="'system:role:edit'" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" v-permission="'system:role:assign'" @click="openAssign(row)">分配菜单</el-button>
          <el-button size="small" type="danger" v-permission="'system:role:remove'" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNo"
                   @current-change="onPage" />

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="460px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="角色名" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="角色标识" prop="roleKey"><el-input v-model="form.roleKey" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单 -->
    <el-dialog v-model="assign.visible" title="分配菜单" width="420px">
      <el-tree ref="treeRef" :data="menuTree" show-checkbox node-key="id" default-expand-all
               :props="{ label: 'menuName', children: 'children' }" />
      <template #footer>
        <el-button @click="assign.visible = false">取消</el-button>
        <el-button type="primary" :loading="assign.saving" @click="onAssignSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rolePageApi, roleAddApi, roleUpdateApi, roleRemoveApi, roleMenuIdsApi, roleAssignMenusApi } from '../../api/role'
import { menuTreeApi } from '../../api/menu'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 10, roleName: '' })
const formRef = ref()
const dialog = reactive({ visible: false, title: '' })
const form = reactive({ id: null, roleName: '', roleKey: '', remark: '' })
const rules = {
  roleName: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}
const menuTree = ref([])
const treeRef = ref()
const assign = reactive({ visible: false, saving: false, roleId: null })

const load = async () => {
  loading.value = true
  try {
    const res = await rolePageApi(query)
    rows.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
const onPage = (p) => { query.pageNo = p; load() }

const resetForm = () => { form.id = null; form.roleName = ''; form.roleKey = ''; form.remark = '' }
const openAdd = () => { resetForm(); dialog.title = '新增角色'; dialog.visible = true }
const openEdit = (row) => {
  resetForm()
  form.id = row.id; form.roleName = row.roleName; form.roleKey = row.roleKey; form.remark = row.remark
  dialog.title = '编辑角色'; dialog.visible = true
}
const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    form.id ? await roleUpdateApi(form) : await roleAddApi(form)
    ElMessage.success('保存成功')
    dialog.visible = false
    load()
  } finally {
    saving.value = false
  }
}
const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」?`, '提示', { type: 'warning' })
  await roleRemoveApi(row.id)
  ElMessage.success('删除成功')
  load()
}

// 收集树中所有叶子节点 id
const collectLeafIds = (nodes, acc = new Set()) => {
  for (const n of nodes) {
    if (n.children && n.children.length) {
      collectLeafIds(n.children, acc)
    } else {
      acc.add(n.id)
    }
  }
  return acc
}

const openAssign = async (row) => {
  assign.roleId = row.id
  if (!menuTree.value.length) {
    menuTree.value = (await menuTreeApi()).data
  }
  assign.visible = true
  const ids = (await roleMenuIdsApi(row.id)).data || []
  // 回显只勾叶子,父节点由 el-tree 自动半选,避免级联误选;等弹窗渲染后设置
  const leafSet = collectLeafIds(menuTree.value)
  const leafChecked = ids.filter((id) => leafSet.has(id))
  await nextTick()
  treeRef.value.setCheckedKeys(leafChecked)
}
const onAssignSave = async () => {
  assign.saving = true
  try {
    const checked = treeRef.value.getCheckedKeys()
    const half = treeRef.value.getHalfCheckedKeys()
    const menuIds = [...checked, ...half]
    await roleAssignMenusApi({ roleId: assign.roleId, menuIds })
    ElMessage.success('分配成功(对方重新登录后生效)')
    assign.visible = false
  } finally {
    assign.saving = false
  }
}

load()
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; }
.pager { margin-top: 12px; justify-content: flex-end; display: flex; }
</style>
