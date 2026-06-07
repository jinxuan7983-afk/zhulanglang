import request from '@/utils/request'

export const personalEnroll = (data) => request({ url: '/student/enrollments/personal', method: 'post', data })
export const getMyEnrollments = () => request({ url: '/student/enrollments', method: 'get' })
export const createTeam = (data) => request({ url: '/student/teams', method: 'post', data })
export const joinTeam = (data) => request({ url: '/student/teams/join', method: 'post', data })
export const getTeamDetail = (teamId) => request({ url: `/student/teams/${teamId}`, method: 'get' })
export const reviewMember = (teamId, data) => request({ url: `/student/teams/${teamId}/members/review`, method: 'post', data })
export const submitTeam = (teamId, data) => request({ url: `/student/teams/${teamId}/submit`, method: 'post', data })
