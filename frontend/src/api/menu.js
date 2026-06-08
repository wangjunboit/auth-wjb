import request from './request'

export const menuTreeApi = () => request.get('/system/menu/list')
export const menuAddApi = (data) => request.post('/system/menu', data)
export const menuUpdateApi = (data) => request.put('/system/menu', data)
export const menuRemoveApi = (id) => request.delete(`/system/menu/${id}`)
