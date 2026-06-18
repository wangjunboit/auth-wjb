<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">auth-wjb</div>
      <el-menu :default-active="route.path" router unique-opened background-color="#304156"
               text-color="#bfcbd9" active-text-color="#409eff">
        <template v-for="m in store.menus" :key="m.id">
          <el-sub-menu v-if="m.children && m.children.length" :index="'sub-' + m.id">
            <template #title><span>{{ m.menuName }}</span></template>
            <el-menu-item v-for="c in m.children" :key="c.id" :index="fullPath(m, c)">
              {{ c.menuName }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="m.path">{{ m.menuName }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ currentTitle }}</span>
        <el-dropdown @command="onCommand">
          <span class="user">{{ store.userInfo?.nickname || store.userInfo?.username }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const route = useRoute()
const router = useRouter()
const store = useAuthStore()

const currentTitle = computed(() => route.meta.title || '')

// 父菜单 path 形如 "/system",子 path 形如 "user" → "/system/user"
const fullPath = (parent, child) => {
  const p = (parent.path || '').replace(/\/$/, '')
  const c = (child.path || '').replace(/^\//, '')
  return `${p}/${c}`
}

const onCommand = async (cmd) => {
  if (cmd === 'profile') {
    router.push('/profile')
    return
  }
  if (cmd === 'logout') {
    await store.logout()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #304156; }
.logo { color: #fff; height: 56px; line-height: 56px; text-align: center; font-weight: bold; }
.header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; }
.user { cursor: pointer; color: #409eff; }
</style>
