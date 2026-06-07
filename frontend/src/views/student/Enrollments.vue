<template>
  <div class="enrollments-page">
    <h2>我的报名记录</h2>
    <el-table :data="list" border>
      <el-table-column prop="contestName" label="竞赛名称" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ row.teamId ? '团队' : '个人' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见" />
      <el-table-column label="报名时间" width="170">
        <template #default="{ row }">{{ row.registrationTime?.substring(0, 16) }}</template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!list.length" description="暂无报名记录" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyEnrollments } from '@/api/student'

const list = ref([])

onMounted(async () => {
  const res = await getMyEnrollments()
  list.value = res.data || []
})

function statusType(s) {
  return s === 'approved' ? 'success' : s === 'rejected' ? 'danger' : s === 'pending' ? 'warning' : 'info'
}
function statusText(s) {
  return s === 'approved' ? '已通过' : s === 'rejected' ? '已拒绝' : s === 'pending' ? '待审核' : s === 'cancelled' ? '已取消' : s
}
</script>
