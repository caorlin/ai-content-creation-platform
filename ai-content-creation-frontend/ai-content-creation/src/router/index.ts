import { createRouter, createWebHistory } from "vue-router"
import HomePage from "@/pages/HomePage.vue"
import UserLoginPage from "@/pages/user/UserLoginPage.vue"
import UserRegisterPage from "@/pages/user/UserRegisterPage.vue"
import UserManagePage from "@/pages/admin/UserManagePage.vue"
import ArticleCreatePage from "@/pages/article/ArticleCreatePage.vue"
import ArticleListPage from "@/pages/article/ArticleListPage.vue"
import ArticleDetailPage from "@/pages/article/ArticleDetailPage.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "主页",
      component: HomePage,
    },
    {
      path: "/user/login",
      name: "用户登录",
      component: UserLoginPage,
    },
    {
      path: "/user/register",
      name: "用户注册",
      component: UserRegisterPage,
    },
    {
      path: "/admin/userManage",
      name: "用户管理",
      component: UserManagePage,
    },
    {
      path: "/article/create",
      name: "文章创作",
      component: ArticleCreatePage,
    },
    {
      path: "/article/list",
      name: "创作历史",
      component: ArticleListPage,
    },
    {
      path: "/article/detail/:taskId",
      name: "文章详情",
      component: ArticleDetailPage,
    },
  ],
})

export default router
