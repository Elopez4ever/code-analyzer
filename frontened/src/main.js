import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import './style.css'
import App from './App.vue'
import HomeView from './views/HomeView.vue'
import UploadView from './views/UploadView.vue'
import ProjectListView from './views/ProjectListView.vue'
import ChatView from './views/ChatView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/upload', name: 'upload', component: UploadView },
    { path: '/projects', name: 'projects', component: ProjectListView },
    { path: '/chat/:projectId', name: 'chat', component: ChatView, props: true },
  ],
})

createApp(App).use(router).mount('#app')
