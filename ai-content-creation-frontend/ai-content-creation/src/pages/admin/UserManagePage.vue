<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item label="用户简介">
        <a-input v-model:value="searchParams.userProfile" placeholder="输入用户简介" />
      </a-form-item>
      <a-form-item label="用户角色">
        <a-space>
          <a-select
            ref="select"
            v-model:value="searchParams.userRole"
            placeholder="用户角色"
            style="width: 120px"
          >
            <a-select-option
              style="width: 120px"
              v-model:value="searchParams.userRole"
              :options="userRoleOptions"
            />
          </a-select>
        </a-space>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      row-key="id"
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'userAvatar'">
          <a-avatar :src="record.userAvatar" />
        </template>
        <template v-else-if="column.key === 'userName'">
          <a>{{ record.userName || '-' }}</a>
        </template>
        <template v-else-if="column.key === 'userProfile'">
          {{ record.userProfile || '-' }}
        </template>
        <template v-else-if="column.key === 'userRole'">
          <a-tag :color="record.userRole === 'admin' ? 'green' : 'blue'">
            {{ record.userRole || '-' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <span>
            <a>编辑</a>
            <a-divider type="vertical" />
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message, type SelectProps } from 'ant-design-vue'
import { deleteUser, listUserVoByPage } from '@/api/userController.ts'

const data = ref<API.UserVO[]>([])
const total = ref(0)

const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    key: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

const pagination = computed(() => ({
  current: searchParams.pageNum,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`,
}))

const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })

  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

const handleTableChange = (page: { current?: number; pageSize?: number }) => {
  searchParams.pageNum = page.current ?? 1
  searchParams.pageSize = page.pageSize ?? 10
  fetchData()
}

// 获取数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}
//角色
const userRoleOptions = ref<SelectProps['options']>([
  {
    value: 'admin',
    label: '管理员',
  },
  {
    value: 'user',
    label: '普通用户',
  },
])

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>
