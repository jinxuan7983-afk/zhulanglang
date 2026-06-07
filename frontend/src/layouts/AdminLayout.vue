<template>
  <div class="admin-layout">
    <el-header class="admin-header">
      <h1 class="logo" @click="$router.push('/')">后台管理</h1>
      <el-button type="danger" @click="logout">退出</el-button>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="activeMenu" router class="side-menu">
          <el-menu-item index="/admin">控制台</el-menu-item>
          <el-menu-item index="/admin/contests">竞赛管理</el-menu-item>
          <el-menu-item index="/admin/enrollments">报名管理</el-menu-item>
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
.admin-layout { min-height: 100vh; }
.admin-header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; }
.logo { cursor: pointer; color: #409EFF; margin: 0; font-size: 18px; }
.side-menu { height: 100%; }
</style>
