<template>
  <div class="cb-wrap">
    <el-card>{{ msg }}</el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { oauthCallbackApi } from '../api/oauth'

const route = useRoute()
const router = useRouter()
const store = useAuthStore()
const msg = ref('正在处理第三方授权...')

onMounted(async () => {
  const code = route.query.code
  const state = route.query.state
  const provider = sessionStorage.getItem('oauth_provider') || 'github'
  if (!code || !state) {
    msg.value = '授权参数缺失'
    setTimeout(() => router.push('/login'), 1500)
    return
  }
  try {
    const res = await oauthCallbackApi(provider, { code, state })
    if (res.data.mode === 'login') {
      store.token = res.data.token
      localStorage.setItem('token', res.data.token)
      await store.loadUserData()
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.success('绑定成功')
      router.push('/system/binding')
    }
  } catch (e) {
    msg.value = '授权失败'
    const mode = sessionStorage.getItem('oauth_mode')
    setTimeout(() => router.push(mode === 'bind' ? '/system/binding' : '/login'), 1500)
  }
})
</script>

<style scoped>
.cb-wrap { height: 100vh; display: flex; align-items: center; justify-content: center; }
</style>
