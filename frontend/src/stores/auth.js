import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = () => !!token.value

  const login = async (credentials) => {
    const res = await loginApi(credentials)
    token.value = res.data.token
    user.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('role', res.data.role)
    localStorage.setItem('user', JSON.stringify(res.data))
    return res.data
  }

  const register = async (data) => {
    return await registerApi(data)
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, login, register, logout }
})
