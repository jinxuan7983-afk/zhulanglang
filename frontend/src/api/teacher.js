import request from '@/utils/request'

export const getTeacherContests = () => request({ url: '/teacher/contests', method: 'get' })
export const getTeacherEnrollments = (params) => request({ url: '/teacher/enrollments', method: 'get', params })
export const reviewTeacherEnrollment = (id, data) => request({ url: `/teacher/enrollments/${id}/review`, method: 'post', data })
export const getTeacherTeams = (params) => request({ url: '/teacher/teams', method: 'get', params })
export const reviewTeacherTeam = (id, data) => request({ url: `/teacher/teams/${id}/review`, method: 'post', data })
