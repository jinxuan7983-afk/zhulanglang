<template>
  <div class="teacher-dashboard">
    <h2>待审核报名</h2>
    <el-table :data="enrollments" border>
      <el-table-column prop="contestName" label="竞赛" />
      <el-table-column prop="studentName" label="学生" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag type="warning">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="review(row, 'approved')">通过</el-button>
          <el-button size="small" type="danger" @click="review(row, 'rejected')">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!enrollments.length" description="暂无待审核记录" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeacherEnrollments, getTeacherTeams, reviewTeacherEnrollment, reviewTeacherTeam } from '@/api/teacher'

const enrollments = ref([])

onMounted(fetchData)

async function fetchData() {
  const eRes = await getTeacherEnrollments({ status: 'pending' })
  const tRes = await getTeacherTeams({ status: 'reviewing' })
  const personal = (eRes.data || []).filter(e => !e.teamId).map(e => ({ ...e, type: '个人' }))
  const team = (tRes.data || []).map(t => ({
    enrollmentId: t.teamId, contestName: t.contestName,
    studentName: t.teamName, status: t.status, type: '团队', isTeam: true
  }))
  enrollments.value = [...personal, ...team]
}

async function review(row, status) {
  try {
    if (row.isTeam) {
      await reviewTeacherTeam(row.enrollmentId, { status, comment: '' })
    } else {
      await reviewTeacherEnrollment(row.enrollmentId, { status, comment: '' })
    }
    ElMessage.success('审核成功')
    await fetchData()
  } catch {}
}

function statusText(s) { return s === 'pending' ? '待审核' : s === 'reviewing' ? '审核中' : s }
</script>
