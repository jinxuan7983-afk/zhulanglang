<template>
  <div class="student-layout">
    <el-header class="student-header">
      <h1 class="logo" @click="$router.push('/')">高校竞赛报名</h1>
      <div class="header-right">
        <el-button @click="$router.push('/')">返回首页</el-button>
        <el-button type="danger" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="activeMenu" router class="side-menu">
          <el-menu-item index="/student">个人资料</el-menu-item>
          <el-menu-item index="/student/enrollments">报名记录</el-menu-item>
          <el-menu-item index="/student/teams">团队管理</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.student-layout { min-height: 100vh; }
.student-header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; }
.logo { cursor: pointer; color: #409EFF; margin: 0; font-size: 18px; }
.header-right { display: flex; gap: 10px; }
.side-menu { height: 100%; }
</style>
