<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="学号/邮箱" prop="account">
          <el-input v-model="form.account" placeholder="请输入学号或邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button>
        </el-form-item>
      </el-form>
      <p class="switch-text">还没有账号？<router-link to="/register">注册</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ account: '', password: '' })
const rules = {
  account: [{ required: true, message: '请输入学号或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login({ account: form.account, password: form.password })
    if (res.code === 200) {
      const d = res.data
      localStorage.setItem('token', d.token)
      localStorage.setItem('role', d.role)
      localStorage.setItem('user', JSON.stringify(d))
      ElMessage.success('登录成功')
      if (d.role === 'admin') router.push('/admin')
      else if (d.role === 'teacher') router.push('/teacher')
      else router.push('/')
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f5f5f5; }
.login-card { width: 420px; }
.login-card h2 { text-align: center; margin-bottom: 20px; }
.switch-text { text-align: center; margin-top: 15px; font-size: 14px; }
</style>
