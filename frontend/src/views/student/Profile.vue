<template>
  <div class="profile-page">
    <h2>个人资料</h2>
    <el-card>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ user.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ user.name }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleText(user.role) }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ user.college || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ user.phone || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || '{}') } catch { return {} }
})

function roleText(r) { return r === 'student' ? '学生' : r === 'teacher' ? '教师' : r === 'admin' ? '管理员' : r }
</script>
