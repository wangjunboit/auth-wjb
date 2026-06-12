<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 class="title">auth-wjb 权限管理</h2>
      <el-tabs v-model="tab">
        <!-- 账号密码 -->
        <el-tab-pane label="账号密码" name="pwd">
          <el-form :model="pwd" ref="pwdRef" @keyup.enter="onPwdLogin">
            <el-form-item>
              <el-input v-model="pwd.username" placeholder="用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="pwd.password" type="password" show-password placeholder="密码" :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <div class="cap-row">
                <el-input v-model="pwd.captchaCode" placeholder="图形验证码" style="flex:1" />
                <img v-if="captcha.img" :src="captcha.img" class="cap-img" @click="loadCaptcha" title="点击刷新" />
              </div>
            </el-form-item>
            <el-button type="primary" class="btn" :loading="loading" @click="onPwdLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <!-- 手机验证码 -->
        <el-tab-pane label="手机验证码" name="sms">
          <el-form @keyup.enter="onSmsLogin">
            <el-form-item>
              <el-input v-model="sms.phone" placeholder="手机号" :prefix-icon="Iphone" />
            </el-form-item>
            <el-form-item>
              <div class="cap-row">
                <el-input v-model="sms.captchaCode" placeholder="图形验证码" style="flex:1" />
                <img v-if="captcha.img" :src="captcha.img" class="cap-img" @click="loadCaptcha" title="点击刷新" />
              </div>
            </el-form-item>
            <el-form-item>
              <div class="cap-row">
                <el-input v-model="sms.code" placeholder="短信验证码" style="flex:1" />
                <el-button :disabled="sms.counting > 0" @click="sendSms">
                  {{ sms.counting > 0 ? sms.counting + 's' : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" class="btn" :loading="loading" @click="onSmsLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <!-- 邮箱验证码 -->
        <el-tab-pane label="邮箱验证码" name="email">
          <el-form @keyup.enter="onEmailLogin">
            <el-form-item>
              <el-input v-model="email.email" placeholder="邮箱" :prefix-icon="Message" />
            </el-form-item>
            <el-form-item>
              <div class="cap-row">
                <el-input v-model="email.captchaCode" placeholder="图形验证码" style="flex:1" />
                <img v-if="captcha.img" :src="captcha.img" class="cap-img" @click="loadCaptcha" title="点击刷新" />
              </div>
            </el-form-item>
            <el-form-item>
              <div class="cap-row">
                <el-input v-model="email.code" placeholder="邮箱验证码" style="flex:1" />
                <el-button :disabled="email.counting > 0" @click="sendEmail">
                  {{ email.counting > 0 ? email.counting + 's' : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" class="btn" :loading="loading" @click="onEmailLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Message } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'
import { captchaApi, smsCodeApi, emailCodeApi } from '../api/auth'

const router = useRouter()
const store = useAuthStore()
const loading = ref(false)
const tab = ref('pwd')

const captcha = reactive({ key: '', img: '' })
const pwd = reactive({ username: 'admin', password: '123456', captchaKey: '', captchaCode: '' })
const sms = reactive({ phone: '13800000000', captchaCode: '', code: '', counting: 0 })
const email = reactive({ email: 'admin@test.com', captchaCode: '', code: '', counting: 0 })

const loadCaptcha = async () => {
  const res = await captchaApi()
  captcha.key = res.data.captchaKey
  captcha.img = res.data.imageBase64
}
onMounted(loadCaptcha)

const afterLogin = async () => {
  await store.loadUserData()
  ElMessage.success('登录成功')
  router.push('/')
}

const onPwdLogin = async () => {
  loading.value = true
  try {
    await store.login({ username: pwd.username, password: pwd.password, captchaKey: captcha.key, captchaCode: pwd.captchaCode })
    await afterLogin()
  } catch (e) { loadCaptcha() } finally { loading.value = false }
}

const startCount = (obj) => {
  obj.counting = 60
  const t = setInterval(() => { if (--obj.counting <= 0) clearInterval(t) }, 1000)
}

const sendSms = async () => {
  try {
    const res = await smsCodeApi({ phone: sms.phone, captchaKey: captcha.key, captchaCode: sms.captchaCode })
    if (res.data) ElMessage.success('验证码(dev):' + res.data)
    else ElMessage.success('验证码已发送')
    startCount(sms)
  } catch (e) { loadCaptcha() }
}
const onSmsLogin = async () => {
  loading.value = true
  try {
    await store.loginBySms({ phone: sms.phone, code: sms.code })
    await afterLogin()
  } catch (e) { /* 提示已弹 */ } finally { loading.value = false }
}

const sendEmail = async () => {
  try {
    const res = await emailCodeApi({ email: email.email, captchaKey: captcha.key, captchaCode: email.captchaCode })
    if (res.data) ElMessage.success('验证码(dev):' + res.data)
    else ElMessage.success('验证码已发送')
    startCount(email)
  } catch (e) { loadCaptcha() }
}
const onEmailLogin = async () => {
  loading.value = true
  try {
    await store.loginByEmail({ email: email.email, code: email.code })
    await afterLogin()
  } catch (e) { /* 提示已弹 */ } finally { loading.value = false }
}
</script>

<style scoped>
.login-wrap { height: 100vh; display: flex; align-items: center; justify-content: center; background: #f0f2f5; }
.login-card { width: 380px; }
.title { text-align: center; margin: 0 0 12px; }
.btn { width: 100%; }
.cap-row { display: flex; gap: 8px; width: 100%; align-items: center; }
.cap-img { height: 40px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px; }
</style>
