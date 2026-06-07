<template>
  <div class="teacher-contests">
    <h2>指导竞赛</h2>
    <el-table :data="contests" border>
      <el-table-column prop="contestName" label="竞赛名称" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag>{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enrollmentCount" label="报名人数" width="100" />
    </el-table>
    <el-empty v-if="!contests.length" description="暂无指导竞赛" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeacherContests } from '@/api/teacher'

const contests = ref([])

onMounted(async () => {
  const res = await getTeacherContests()
  contests.value = res.data || []
})

function statusText(s) {
  return s === 'published' ? '已上架' : s === 'not_started' ? '未上架' : s === 'ended' ? '已结束' : s
}
</script>
