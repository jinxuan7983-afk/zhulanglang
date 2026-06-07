import request from '@/utils/request'

export const getContestList = (params) => request({ url: '/contests', method: 'get', params })
export const getContestDetail = (id) => request({ url: `/contests/${id}`, method: 'get' })
