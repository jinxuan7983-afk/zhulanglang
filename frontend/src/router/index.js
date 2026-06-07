import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'contests', name: 'ContestList', component: () => import('@/views/ContestList.vue') },
      { path: 'contests/:id', name: 'ContestDetail', component: () => import('@/views/ContestDetail.vue') },
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { role: 'student' },
    children: [
      { path: '', name: 'StudentHome', component: () => import('@/views/student/Profile.vue') },
      { path: 'enrollments', name: 'MyEnrollments', component: () => import('@/views/student/Enrollments.vue') },
      { path: 'teams', name: 'MyTeams', component: () => import('@/views/student/Teams.vue') },
    ]
  },
  {
    path: '/teacher',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { role: 'teacher' },
    children: [
      { path: '', name: 'TeacherHome', component: () => import('@/views/teacher/Dashboard.vue') },
      { path: 'contests', name: 'TeacherContests', component: () => import('@/views/teacher/Contests.vue') },
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { role: 'admin' },
    children: [
      { path: '', name: 'AdminHome', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'contests', name: 'AdminContestManage', component: () => import('@/views/admin/ContestManage.vue') },
      { path: 'enrollments', name: 'AdminEnrollmentManage', component: () => import('@/views/admin/EnrollmentManage.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.role && !token) {
    next('/login')
  } else if (to.meta.role && role !== to.meta.role) {
    next('/')
  } else {
    next()
  }
})

export default router
