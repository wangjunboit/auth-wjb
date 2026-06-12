import request from './request'

export const oauthUrlApi = (provider) => request.get(`/auth/oauth/${provider}/url`)
export const oauthCallbackApi = (provider, data) => request.post(`/auth/oauth/${provider}/callback`, data)
export const oauthBindUrlApi = (provider) => request.get(`/system/oauth/${provider}/bind-url`)
export const oauthBindingsApi = () => request.get('/system/oauth/bindings')
export const oauthUnbindApi = (provider) => request.delete(`/system/oauth/${provider}`)
