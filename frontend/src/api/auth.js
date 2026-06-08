import request from './request'

export const loginApi = (data) => request.post('/auth/login', data)
export const logoutApi = () => request.post('/auth/logout')
export const userInfoApi = () => request.get('/auth/userinfo')
export const userMenusApi = () => request.get('/auth/menus')
