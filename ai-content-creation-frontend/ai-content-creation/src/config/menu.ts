import type { MenuProps } from 'ant-design-vue'
import { BulbTwoTone, HomeTwoTone } from '@ant-design/icons-vue'
import { h } from 'vue'

export const headerMenuItems: MenuProps['items'] = [
  {
    key: '/',
    icon: () => h(HomeTwoTone),
    label: '主页',
    title: '主页',
  },
  {
    key: '/article/create',
    icon: () => h(BulbTwoTone),
    label: '文章创作',
    title: '文章创作',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
]
