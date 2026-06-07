<template>
  <div class="contest-list-page">
    <el-form :inline="true" class="search-bar">
      <el-form-item>
        <el-input v-model="keyword" placeholder="搜索竞赛名称" clearable @clear="search" />
      </el-form-item>
      <el-form-item>
        <el-select v-model="category" placeholder="竞赛类别" clearable @change="search">
          <el-option label="学科竞赛" value="学科竞赛" />
          <el-option label="创新创业" value="创新创业" />
          <el-option label="文体活动" value="文体活动" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select v-model="progressStatus" placeholder="状态" clearable @change="search">
          <el-option label="即将开始" value="upcoming" />
          <el-option label="报名中" value="registering" />
          <el-option label="已结束" value="ended" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">搜索</el-button>
      </el-form-item>
    </el-form>

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

    <div class="pagination" v-if="total > 0">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" :current-page="pageNum" @current-change="onPageChange" />
    </div>
    <el-empty v-if="!contests.length" description="暂无竞赛" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getContestList } from '@/api/contest'

const contests = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const keyword = ref('')
const category = ref('')
const progressStatus = ref('')

async function search() {
  pageNum.value = 1
  await fetchData()
}

async function onPageChange(p) {
  pageNum.value = p
  await fetchData()
}

async function fetchData() {
  const params = { pageNum: pageNum.value, pageSize: pageSize.value }
  if (keyword.value) params.keyword = keyword.value
  if (category.value) params.category = category.value
  if (progressStatus.value) params.progressStatus = progressStatus.value
  const res = await getContestList(params)
  contests.value = res.data?.records || []
  total.value = res.data?.total || 0
}

onMounted(fetchData)

function statusType(s) { return s === 'registering' ? 'success' : s === 'upcoming' ? 'warning' : 'info' }
function statusText(s) { return s === 'registering' ? '报名中' : s === 'upcoming' ? '即将开始' : '已结束' }
</script>

<style scoped>
.search-bar { margin-bottom: 20px; }
.contest-card { cursor: pointer; margin-bottom: 20px; }
.cover-img { width: 100%; height: 150px; object-fit: cover; }
.card-info { padding: 10px; }
.card-info h4 { margin: 0 0 5px; }
.org { color: #999; font-size: 12px; margin: 0 0 5px; }
.pagination { margin-top: 20px; text-align: center; }
</style>
