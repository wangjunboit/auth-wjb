import request from './request'

export const userPageApi = (params) => request.get('/system/user/list', { params })
export const userAddApi = (data) => request.post('/system/user', data)
export const userUpdateApi = (data) => request.put('/system/user', data)
export const userRemoveApi = (id) => request.delete(`/system/user/${id}`)
