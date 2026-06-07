<template>
  <div class="admin-dashboard">
    <h2>后台控制台</h2>
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="8"><el-card><h3>竞赛数量</h3><p style="font-size:24px;color:#409EFF">{{ stats.contestCount }}</p></el-card></el-col>
      <el-col :span="8"><el-card><h3>报名数量</h3><p style="font-size:24px;color:#67C23A">{{ stats.enrollmentCount }}</p></el-card></el-col>
      <el-col :span="8"><el-card><h3>待审核</h3><p style="font-size:24px;color:#E6A23C">{{ stats.pendingCount }}</p></el-card></el-col>
    </el-row>

    <h3>待审核列表</h3>
    <el-table :data="pendingList" border>
      <el-table-column prop="contestName" label="竞赛" />
      <el-table-column prop="studentName" label="申请人" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="review(row, 'approved')">通过</el-button>
          <el-button size="small" type="danger" @click="review(row, 'rejected')">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminEnrollments, getAdminTeams, reviewAdminEnrollment, reviewAdminTeam, getAdminContests } from '@/api/admin'

const stats = reactive({ contestCount: 0, enrollmentCount: 0, pendingCount: 0 })
const pendingList = ref([])

onMounted(fetchData)

async function fetchData() {
  const [cr, er, tr] = await Promise.all([
    getAdminContests({ pageSize: 1 }),
    getAdminEnrollments({ status: 'pending' }),
    getAdminTeams({ status: 'reviewing' })
  ])
  stats.contestCount = cr.data?.total || 0
  const personal = (er.data || []).filter(e => !e.teamId).map(e => ({ ...e, type: '个人' }))
  const team = (tr.data || []).map(t => ({
    enrollmentId: t.teamId, contestName: t.contestName,
    studentName: t.teamName, status: t.status, type: '团队', isTeam: true
  }))
  pendingList.value = [...personal, ...team]
  stats.pendingCount = pendingList.value.length
}

async function review(row, status) {
  try {
    if (row.isTeam) {
      await reviewAdminTeam(row.enrollmentId, { status, comment: '' })
    } else {
      await reviewAdminEnrollment(row.enrollmentId, { status, comment: '' })
    }
    ElMessage.success('审核成功')
    await fetchData()
  } catch {}
}
</script>
