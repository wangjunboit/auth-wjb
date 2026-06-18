import request from './request'

export const loginApi = (data) => request.post('/auth/login', data)
export const smsLoginApi = (data) => request.post('/auth/login/sms', data)
export const emailLoginApi = (data) => request.post('/auth/login/email', data)
export const logoutApi = () => request.post('/auth/logout')
export const userInfoApi = () => request.get('/auth/userinfo')
export const userMenusApi = () => request.get('/auth/menus')
export const captchaApi = () => request.get('/auth/captcha')
export const smsCodeApi = (data) => request.post('/auth/sms-code', data)
export const emailCodeApi = (data) => request.post('/auth/email-code', data)

export const profileApi = () => request.get('/auth/profile')
export const updateProfileApi = (data) => request.put('/auth/profile', data)
export const changePasswordApi = (data) => request.put('/auth/password', data)
