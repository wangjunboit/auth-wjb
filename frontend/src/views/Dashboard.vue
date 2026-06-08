<template>
  <el-card>
    <h3>欢迎,{{ store.userInfo?.nickname || store.userInfo?.username }}</h3>
    <p>角色:{{ (store.userInfo?.roles || []).join(', ') }}</p>
    <p>权限码数量:{{ store.perms.length }}</p>
  </el-card>
</template>

<script setup>
import { useAuthStore } from '../store/auth'
const store = useAuthStore()
</script>
