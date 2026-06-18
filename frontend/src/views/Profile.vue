<template>
  <div>
    <el-card>
      <template #header>个人资料</template>
      <el-form :model="form" label-width="90px" style="max-width: 480px">
        <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="头像URL"><el-input v-model="form.avatar" placeholder="http://..." /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>修改密码</template>
      <el-form :model="pwd" :rules="pwdRules" ref="pwdRef" label-width="90px" style="max-width: 480px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwd.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirm">
          <el-input v-model="pwd.confirm" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="changing" @click="doChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { profileApi, updateProfileApi, changePasswordApi } from '../api/auth'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const store = useAuthStore()
const saving = ref(false)
const changing = ref(false)
const form = reactive({ username: '', nickname: '', phone: '', email: '', avatar: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const pwdRef = ref()
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirm: [{
    validator: (rule, value, cb) => value === pwd.newPassword ? cb() : cb(new Error('两次密码不一致')),
    trigger: 'blur'
  }]
}

const load = async () => {
  const res = await profileApi()
  Object.assign(form, res.data)
}
onMounted(load)

const saveProfile = async () => {
  saving.value = true
  try {
    await updateProfileApi({ nickname: form.nickname, phone: form.phone, email: form.email, avatar: form.avatar })
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}

const doChangePassword = async () => {
  await pwdRef.value.validate()
  changing.value = true
  try {
    await changePasswordApi({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已修改,请重新登录')
    store.reset()
    router.push('/login')
  } finally {
    changing.value = false
  }
}
</script>
