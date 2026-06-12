import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/oauth/callback', component: () => import('../views/OAuthCallback.vue') },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'system/user', component: () => import('../views/system/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', component: () => import('../views/system/RoleManage.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', component: () => import('../views/system/MenuManage.vue'), meta: { title: '菜单管理' } },
      { path: 'system/binding', component: () => import('../views/system/Binding.vue'), meta: { title: '账号绑定' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 守卫:无 token 跳登录;/oauth/callback 放行(回调流程内部处理)
router.beforeEach(async (to) => {
  if (to.path === '/oauth/callback') {
    return true
  }
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    return token ? '/' : true
  }
  if (!token) {
    return '/login'
  }
  const { useAuthStore } = await import('../store/auth')
  const store = useAuthStore()
  if (!store.loaded) {
    try {
      await store.loadUserData()
    } catch (e) {
      store.reset()
      return '/login'
    }
  }
  return true
})

export default router
