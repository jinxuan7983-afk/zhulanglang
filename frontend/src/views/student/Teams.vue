<template>
  <div class="teams-page">
    <h2>团队管理</h2>

    <!-- 创建团队 -->
    <el-card v-if="!myTeam" class="section-card">
      <h3>创建团队</h3>
      <el-form :inline="true" :model="createForm">
        <el-form-item label="竞赛ID">
          <el-input v-model.number="createForm.contestId" placeholder="请输入竞赛编号" />
        </el-form-item>
        <el-form-item label="团队名称">
          <el-input v-model="createForm.teamName" placeholder="请输入团队名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCreateTeam">创建</el-button>
        </el-form-item>
      </el-form>
      <p style="font-size:12px;color:#999">提示：在竞赛详情页可查看竞赛编号</p>
    </el-card>

    <!-- 加入团队 -->
    <el-card v-if="!myTeam" class="section-card">
      <h3>加入团队</h3>
      <el-form :inline="true" :model="joinForm">
        <el-form-item label="邀请码">
          <el-input v-model="joinForm.inviteCode" placeholder="输入6位邀请码" maxlength="6" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleJoinTeam">申请加入</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的团队 -->
    <el-card v-if="myTeam" class="section-card">
      <template #header>
        <div class="team-header">
          <span>团队：{{ myTeam.teamName }}</span>
          <el-tag>{{ teamStatusText(myTeam.status) }}</el-tag>
        </div>
      </template>
      <p>邀请码：<el-tag type="warning">{{ myTeam.inviteCode || '仅队长可见' }}</el-tag></p>

      <h4>成员列表</h4>
      <el-table :data="myTeam.members || []" border>
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'approved' ? 'success' : row.status === 'applying' ? 'warning' : 'danger'">
              {{ row.status === 'approved' ? '已通过' : row.status === 'applying' ? '申请中' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isCaptain" label="操作" width="160">
          <template #default="{ row }">
            <template v-if="row.status === 'applying'">
              <el-button size="small" type="success" @click="reviewMember(row.userId, 'approved')">通过</el-button>
              <el-button size="small" type="danger" @click="reviewMember(row.userId, 'rejected')">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="isCaptain" style="margin-top:15px">
        <el-button type="primary" @click="handleSubmitTeam" :disabled="myTeam.status === 'reviewing' || myTeam.status === 'approved'">提交团队报名</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createTeam, joinTeam, getMyEnrollments } from '@/api/student'
import request from '@/utils/request'

const createForm = ref({ contestId: null, teamName: '' })
const joinForm = ref({ inviteCode: '' })
const myTeam = ref(null)

const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || '{}') } catch { return {} }
})
const isCaptain = computed(() => myTeam.value?.captainId === user.value.userId)

onMounted(async () => {
  const er = await getMyEnrollments()
  const teamEnroll = (er.data || []).find(e => e.teamId)
  if (teamEnroll) {
    try {
      const res = await request({ url: `/student/teams/${teamEnroll.teamId}`, method: 'get' })
      myTeam.value = res.data
    } catch {}
  }
})

async function handleCreateTeam() {
  if (!createForm.value.contestId || !createForm.value.teamName) {
    ElMessage.warning('请填写竞赛ID和团队名称')
    return
  }
  try {
    const res = await createTeam(createForm.value)
    ElMessage.success('团队创建成功')
    await refreshTeam()
  } catch {}
}

async function handleJoinTeam() {
  if (!joinForm.value.inviteCode) { ElMessage.warning('请输入邀请码'); return }
  try {
    await joinTeam(joinForm.value)
    ElMessage.success('申请已提交，等待队长审核')
    joinForm.value.inviteCode = ''
  } catch {}
}

async function reviewMember(userId, status) {
  try {
    await request({
      url: `/student/teams/${myTeam.value.teamId}/members/review`,
      method: 'post',
      data: { userId, status }
    })
    ElMessage.success('操作成功')
    await refreshTeam()
  } catch {}
}

async function handleSubmitTeam() {
  try {
    await request({
      url: `/student/teams/${myTeam.value.teamId}/submit`,
      method: 'post',
      data: {}
    })
    ElMessage.success('团队报名已提交')
    await refreshTeam()
  } catch {}
}

async function refreshTeam() {
  const er = await getMyEnrollments()
  const teamEnroll = (er.data || []).find(e => e.teamId)
  if (teamEnroll) {
    const res = await request({ url: `/student/teams/${teamEnroll.teamId}`, method: 'get' })
    myTeam.value = res.data
  }
}

function teamStatusText(s) {
  return s === 'pending' ? '草稿' : s === 'reviewing' ? '审核中' : s === 'approved' ? '已通过' : s === 'rejected' ? '已拒绝' : s
}
</script>

<style scoped>
.section-card { margin-bottom: 20px; }
.team-header { display: flex; align-items: center; gap: 10px; }
</style>
