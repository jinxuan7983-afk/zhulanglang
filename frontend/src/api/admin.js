import request from '@/utils/request'

export const getAdminContests = (params) => request({ url: '/admin/contests', method: 'get', params })
export const getAdminContestDetail = (id) => request({ url: `/admin/contests/${id}`, method: 'get' })
export const createContest = (data) => request({ url: '/admin/contests', method: 'post', data })
export const updateContest = (id, data) => request({ url: `/admin/contests/${id}`, method: 'put', data })
export const changeContestStatus = (id, status) => request({ url: `/admin/contests/${id}/status`, method: 'put', data: { status } })
export const deleteContest = (id) => request({ url: `/admin/contests/${id}`, method: 'delete' })
export const getAdminEnrollments = (params) => request({ url: '/admin/enrollments', method: 'get', params })
export const reviewAdminEnrollment = (id, data) => request({ url: `/admin/enrollments/${id}/review`, method: 'post', data })
export const getAdminTeams = (params) => request({ url: '/admin/teams', method: 'get', params })
export const reviewAdminTeam = (id, data) => request({ url: `/admin/teams/${id}/review`, method: 'post', data })
