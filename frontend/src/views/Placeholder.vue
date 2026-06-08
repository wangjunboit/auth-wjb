<template>
  <el-card>
    <el-empty :description="(route.meta.title || '该模块') + ' 建设中(M5)'" />
  </el-card>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
