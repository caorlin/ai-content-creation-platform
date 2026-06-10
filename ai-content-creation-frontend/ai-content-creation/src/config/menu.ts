import type { MenuProps } from 'ant-design-vue'
import {
  BulbTwoTone,
  HomeTwoTone,
  IdcardTwoTone,
  PieChartTwoTone,
  ProfileTwoTone,
} from '@ant-design/icons-vue'
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
    key: '/article/list',
    icon: () => h(ProfileTwoTone),
    label: '创作历史',
    title: '创作历史',
  },
  {
    key: '/admin/userManage',
    icon: () => h(IdcardTwoTone),
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/statistics',
    icon: () => h(PieChartTwoTone),
    label: '数据统计',
    title: '数据统计',
  },
]
