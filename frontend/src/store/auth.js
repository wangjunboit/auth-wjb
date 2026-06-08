import { defineStore } from 'pinia'
import { loginApi, logoutApi, userInfoApi, userMenusApi } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    perms: [],
    menus: [],
    loaded: false
  }),
  actions: {
    async login(form) {
      const res = await loginApi(form)
      this.token = res.data.token
      localStorage.setItem('token', this.token)
    },
    async loadUserData() {
      const info = await userInfoApi()
      this.userInfo = info.data
      this.perms = info.data.perms || []
      const menus = await userMenusApi()
      this.menus = menus.data || []
      this.loaded = true
    },
    hasPerm(code) {
      return this.perms.includes(code)
    },
    async logout() {
      try { await logoutApi() } catch (e) { /* 忽略 */ }
      this.reset()
    },
    reset() {
      this.token = ''
      this.userInfo = null
      this.perms = []
      this.menus = []
      this.loaded = false
      localStorage.removeItem('token')
    }
  }
})
