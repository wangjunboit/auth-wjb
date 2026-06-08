import request from './request'

export const rolePageApi = (params) => request.get('/system/role/list', { params })
export const roleAddApi = (data) => request.post('/system/role', data)
export const roleUpdateApi = (data) => request.put('/system/role', data)
export const roleRemoveApi = (id) => request.delete(`/system/role/${id}`)
export const roleMenuIdsApi = (id) => request.get(`/system/role/${id}/menus`)
export const roleAssignMenusApi = (data) => request.post('/system/role/assign-menus', data)
