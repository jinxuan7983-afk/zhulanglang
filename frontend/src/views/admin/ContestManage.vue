<template>
  <div class="contest-manage">
    <div class="toolbar">
      <h2>竞赛管理</h2>
      <el-button type="primary" @click="showCreateDialog">发布竞赛</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="registrationType" label="报名类型" width="100">
        <template #default="{ row }">{{ typeText(row.registrationType) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'published' ? 'success' : 'info'">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status !== 'published'" size="small" type="success" @click="publish(row.contestId)">上架</el-button>
          <el-button v-if="row.status === 'published'" size="small" type="warning" @click="unpublish(row.contestId)">下架</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.contestId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑竞赛' : '发布竞赛'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="竞赛名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类别"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="级别"><el-input v-model="form.level" /></el-form-item>
        <el-form-item label="报名开始">
          <el-date-picker v-model="form.registrationStart" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="报名结束">
          <el-date-picker v-model="form.registrationEnd" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="竞赛时间">
          <el-date-picker v-model="form.contestTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="主办方"><el-input v-model="form.organizer" /></el-form-item>
        <el-form-item label="报名类型">
          <el-select v-model="form.registrationType">
            <el-option label="个人赛" value="individual" />
            <el-option label="团队赛" value="team" />
            <el-option label="两者均可" value="both" />
          </el-select>
        </el-form-item>
        <el-form-item label="团队人数范围">
          <el-input-number v-model="form.minTeamSize" :min="1" :max="50" /> -
          <el-input-number v-model="form.maxTeamSize" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="需要审核">
          <el-switch v-model="form.needReview" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminContests, createContest, updateContest, changeContestStatus, deleteContest } from '@/api/admin'

const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '', category: '', level: '', registrationStart: '', registrationEnd: '',
  contestTime: '', location: '', organizer: '', registrationType: 'both',
  minTeamSize: 2, maxTeamSize: 10, needReview: true
})

onMounted(fetchList)

async function fetchList() {
  const res = await getAdminContests({ pageSize: 100 })
  list.value = res.data?.records || []
}

function showCreateDialog() {
  editingId.value = null
  Object.assign(form, {
    name: '', category: '', level: '', registrationStart: '', registrationEnd: '',
    contestTime: '', location: '', organizer: '', registrationType: 'both',
    minTeamSize: 2, maxTeamSize: 10, needReview: true
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  editingId.value = row.contestId
  Object.assign(form, {
    name: row.name, category: row.category || '', level: row.level || '',
    registrationStart: row.registrationStart, registrationEnd: row.registrationEnd,
    contestTime: row.contestTime, location: row.location || '', organizer: row.organizer || '',
    registrationType: row.registrationType, minTeamSize: row.minTeamSize || 2,
    maxTeamSize: row.maxTeamSize || 10, needReview: row.needReview !== false
  })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (editingId.value) {
      await updateContest(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createContest(form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    await fetchList()
  } catch {}
}

async function publish(id) {
  await changeContestStatus(id, 'published')
  ElMessage.success('已上架')
  await fetchList()
}

async function unpublish(id) {
  await changeContestStatus(id, 'not_started')
  ElMessage.success('已下架')
  await fetchList()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该竞赛？', '提示', { type: 'warning' })
  try {
    await deleteContest(id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {}
}

function typeText(t) { return t === 'individual' ? '个人赛' : t === 'team' ? '团队赛' : '两者均可' }
function statusText(s) { return s === 'published' ? '已上架' : s === 'not_started' ? '未上架' : '已结束' }
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
</style>
