<template>
  <div class="enrollment-manage">
    <h2>报名管理</h2>
    <el-table :data="list" border>
      <el-table-column prop="enrollmentId" label="编号" width="80" />
      <el-table-column prop="contestName" label="竞赛" />
      <el-table-column prop="studentName" label="申请人" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ row.teamId ? '团队' : '个人' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status === 'pending'" size="small" type="success" @click="review(row, 'approved')">通过</el-button>
          <el-button v-if="row.status === 'pending'" size="small" type="danger" @click="review(row, 'rejected')">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!list.length" description="暂无报名记录" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminEnrollments, reviewAdminEnrollment } from '@/api/admin'

const list = ref([])

onMounted(fetchData)

async function fetchData() {
  const res = await getAdminEnrollments({ pageSize: 200 })
  list.value = res.data || []
}

async function review(row, status) {
  try {
    await reviewAdminEnrollment(row.enrollmentId, { status, comment: '' })
    ElMessage.success('审核成功')
    await fetchData()
  } catch {}
}

function statusType(s) {
  return s === 'approved' ? 'success' : s === 'rejected' ? 'danger' : s === 'pending' ? 'warning' : 'info'
}
function statusText(s) {
  return s === 'approved' ? '已通过' : s === 'rejected' ? '已拒绝' : s === 'pending' ? '待审核' : s === 'cancelled' ? '已取消' : s
}
</script>
