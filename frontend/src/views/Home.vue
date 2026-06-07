<template>
  <div class="home-page">
    <h2>热门竞赛</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in contests" :key="item.contestId">
        <el-card :body-style="{ padding: '0' }" shadow="hover" class="contest-card" @click="$router.push(`/contests/${item.contestId}`)">
          <img :src="item.coverImage || 'https://via.placeholder.com/300x150'" class="cover-img" />
          <div class="card-info">
            <h4>{{ item.name }}</h4>
            <p class="org">{{ item.organizer }}</p>
            <el-tag :type="statusType(item.progressStatus)" size="small">{{ statusText(item.progressStatus) }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!contests.length" description="暂无竞赛" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getContestList } from '@/api/contest'

const contests = ref([])

onMounted(async () => {
  const res = await getContestList({ pageSize: 8, pageNum: 1 })
  contests.value = res.data?.records || []
})

function statusType(status) {
  return status === 'registering' ? 'success' : status === 'upcoming' ? 'warning' : 'info'
}
function statusText(status) {
  return status === 'registering' ? '报名中' : status === 'upcoming' ? '即将开始' : '已结束'
}
</script>

<style scoped>
.contest-card { cursor: pointer; margin-bottom: 20px; }
.cover-img { width: 100%; height: 150px; object-fit: cover; }
.card-info { padding: 10px; }
.card-info h4 { margin: 0 0 5px; }
.org { color: #999; font-size: 12px; margin: 0 0 5px; }
</style>
