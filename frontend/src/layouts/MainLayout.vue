<template>
  <div class="main-layout">
    <el-header class="main-header">
      <div class="header-left">
        <h1 class="logo" @click="$router.push('/')">高校竞赛报名</h1>
        <el-menu :default-active="activeMenu" mode="horizontal" router class="header-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/contests">竞赛列表</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <template v-if="isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-info">{{ userInfo?.name || '用户' }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="enrollments">我的报名</el-dropdown-item>
                <el-dropdown-item v-if="role === 'admin'" command="admin">后台管理</el-dropdown-item>
                <el-dropdown-item v-if="role === 'teacher'" command="teacher">教师后台</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const role = computed(() => localStorage.getItem('role'))
const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || 'null') } catch { return null }
})
const activeMenu = computed(() => route.path === '/contests' ? '/contests' : '/')

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.clear()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/student')
  } else if (cmd === 'enrollments') {
    router.push('/student/enrollments')
  } else if (cmd === 'admin') {
    router.push('/admin')
  } else if (cmd === 'teacher') {
    router.push('/teacher')
  }
}
</script>

<style scoped>
.main-layout { min-height: 100vh; }
.main-header { display: flex; align-items: center; justify-content: space-between; padding: 0 20px; border-bottom: 1px solid #eee; }
.header-left { display: flex; align-items: center; gap: 20px; }
.logo { cursor: pointer; color: #409EFF; margin: 0; font-size: 18px; white-space: nowrap; }
.header-menu { border-bottom: none !important; }
.header-right { display: flex; align-items: center; gap: 10px; }
.user-info { cursor: pointer; color: #409EFF; }
</style>
