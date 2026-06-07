<template>
  <div class="detail-page" v-if="contest">
    <el-row :gutter="20">
      <el-col :span="16">
        <img :src="contest.coverImage || 'https://via.placeholder.com/600x300'" class="cover-img" />
        <h2>{{ contest.name }}</h2>
        <el-descriptions :column="2" border style="margin-top:20px">
          <el-descriptions-item label="类别">{{ contest.category }}</el-descriptions-item>
          <el-descriptions-item label="级别">{{ contest.level }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ formatDate(contest.registrationStart) }} ~ {{ formatDate(contest.registrationEnd) }}</el-descriptions-item>
          <el-descriptions-item label="竞赛时间">{{ formatDate(contest.contestTime) }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ contest.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主办方">{{ contest.organizer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报名类型">{{ typeText(contest.registrationType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(contest.progressStatus)">{{ statusText(contest.progressStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="contest.registrationType !== 'individual'" label="团队人数"> {{ contest.minTeamSize }}-{{ contest.maxTeamSize }}人</el-descriptions-item>
        </el-descriptions>
      </el-col>
      <el-col :span="8">
        <el-card>
          <h3>报名操作</h3>
          <el-button v-if="!isLoggedIn" type="primary" style="width:100%" @click="$router.push('/login')">请登录后报名</el-button>
          <el-button v-else-if="!isStudent" type="info" disabled style="width:100%">仅学生可报名</el-button>
          <template v-else>
            <el-button v-if="contest.registrationType !== 'team'" type="primary" style="width:100%;margin-bottom:10px" :disabled="!canEnroll" @click="personalEnroll">{{ enrollText }}</el-button>
            <el-button v-if="contest.registrationType !== 'individual'" style="width:100%;margin-bottom:10px" :disabled="!canEnroll" @click="$router.push('/student/teams')">创建/加入团队</el-button>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContestDetail } from '@/api/contest'
import { personalEnroll as doPersonalEnroll, getMyEnrollments } from '@/api/student'

const route = useRoute()
const router = useRouter()
const contest = ref(null)
const alreadyEnrolled = ref(false)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const role = computed(() => localStorage.getItem('role'))
const isStudent = computed(() => role.value === 'student')
const canEnroll = computed(() => contest.value?.progressStatus === 'registering' && !alreadyEnrolled.value)
const enrollText = computed(() => alreadyEnrolled.value ? '已报名' : '个人报名')

onMounted(async () => {
  const id = route.params.id
  const res = await getContestDetail(id)
  contest.value = res.data
  if (isLoggedIn.value && isStudent.value) {
    const er = await getMyEnrollments()
    alreadyEnrolled.value = (er.data || []).some(e => e.contestId === Number(id) && e.status !== 'cancelled')
  }
})

async function personalEnroll() {
  try {
    await doPersonalEnroll({ contestId: Number(route.params.id) })
    ElMessage.success('报名成功')
    alreadyEnrolled.value = true
  } catch (e) { /* handled */ }
}

function formatDate(d) { return d ? d.substring(0, 10) : '-' }
function typeText(t) { return t === 'individual' ? '个人赛' : t === 'team' ? '团队赛' : '个人/团队均可' }
function statusType(s) { return s === 'registering' ? 'success' : s === 'upcoming' ? 'warning' : 'info' }
function statusText(s) { return s === 'registering' ? '报名中' : s === 'upcoming' ? '即将开始' : '已结束' }
</script>

<style scoped>
.cover-img { width: 100%; height: 300px; object-fit: cover; border-radius: 8px; margin-bottom: 10px; }
</style>
