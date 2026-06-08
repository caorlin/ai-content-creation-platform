<template>
  <div id="userManagePage">
    <!-- 工具栏：搜索 + 新增 -->
    <div class="toolbar">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="账号">
          <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="用户简介">
          <a-input v-model:value="searchParams.userProfile" placeholder="输入用户简介" allow-clear />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-select
            v-model:value="searchParams.userRole"
            placeholder="用户角色"
            style="width: 120px"
            allow-clear
            :options="userRoleOptions"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
      <a-button type="primary" @click="openCreateModal">
        <PlusOutlined /> 新增用户
      </a-button>
    </div>
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
          <a-avatar :src="record.userAvatar" :size="40" />
        </template>
        <template v-else-if="column.key === 'userRole'">
          <a-tag :color="record.userRole === 'admin' ? 'blue' : 'default'">
            {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openEditModal(record)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm
              title="确定删除该用户吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="doDelete(record.id)"
            >
              <a style="color: var(--color-error)">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增用户弹窗 -->
    <a-modal
      v-model:open="createModalVisible"
      title="新增用户"
      :confirm-loading="createSubmitting"
      @ok="handleCreateSubmit"
      @cancel="handleCreateCancel"
    >
      <a-form
        ref="createFormRef"
        :model="createForm"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
        :rules="createFormRules"
      >
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="createForm.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="用户昵称" name="username">
          <a-input v-model:value="createForm.username" placeholder="请输入用户昵称" />
        </a-form-item>
        <a-form-item label="用户头像" name="userAvatar">
          <a-input v-model:value="createForm.userAvatar" placeholder="请输入头像 URL" />
        </a-form-item>
        <a-form-item label="用户简介" name="userProfile">
          <a-textarea
            v-model:value="createForm.userProfile"
            placeholder="请输入用户简介"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="用户角色" name="userRole">
          <a-select
            v-model:value="createForm.userRole"
            placeholder="请选择用户角色"
            :options="userRoleOptions"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑用户弹窗 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑用户信息"
      :confirm-loading="editSubmitting"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="用户昵称" name="userName">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户昵称" />
        </a-form-item>
        <a-form-item label="用户头像" name="userAvatar">
          <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像 URL" />
        </a-form-item>
        <a-form-item label="用户简介" name="userProfile">
          <a-textarea
            v-model:value="editForm.userProfile"
            placeholder="请输入用户简介"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="用户角色" name="userRole">
          <a-select
            v-model:value="editForm.userRole"
            placeholder="请选择用户角色"
            :options="userRoleOptions"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message, type SelectProps } from 'ant-design-vue'
import { addUser, deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'
import { PlusOutlined } from '@ant-design/icons-vue'

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
    width: 80,
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
    width: 72,
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
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
    width: 140,
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

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

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

/** 删除 */
const doDelete = async (id: string) => {
  if (!id) return
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// ==================== 新增弹窗 ====================
const createModalVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref()

const createForm = reactive<API.UserAddRequest>({
  username: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const createFormRules = {
  userAccount: [{ required: true, message: '请输入账号' }],
  userRole: [{ required: true, message: '请选择角色' }],
}

/** 打开新增弹窗 */
const openCreateModal = () => {
  createForm.username = ''
  createForm.userAccount = ''
  createForm.userAvatar = ''
  createForm.userProfile = ''
  createForm.userRole = 'user'
  createModalVisible.value = true
}

/** 提交新增 */
const handleCreateSubmit = async () => {
  try {
    await createFormRef.value?.validate()
  } catch {
    return
  }

  createSubmitting.value = true
  try {
    const res = await addUser({ userAddRequest: { ...createForm } })
    if (res.data.code === 0) {
      message.success('新增用户成功')
      createModalVisible.value = false
      fetchData()
    } else {
      message.error('新增失败，' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('请求异常，请稍后重试')
    console.error('新增用户失败:', error)
  } finally {
    createSubmitting.value = false
  }
}

/** 关闭新增弹窗 */
const handleCreateCancel = () => {
  createFormRef.value?.resetFields()
}

// ==================== 编辑弹窗 ====================
const editModalVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()

const editForm = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

/** 打开编辑弹窗，回填当前行数据 */
const openEditModal = (record: API.UserVO) => {
  editForm.id = record.id
  editForm.userName = record.username ?? ''
  editForm.userAvatar = record.userAvatar ?? ''
  editForm.userProfile = record.userProfile ?? ''
  editForm.userRole = record.userRole ?? 'user'
  editModalVisible.value = true
}

/** 提交编辑 */
const handleEditSubmit = async () => {
  editSubmitting.value = true
  try {
    const res = await updateUser({
      id: editForm.id,
      userName: editForm.userName,
      userAvatar: editForm.userAvatar,
      userProfile: editForm.userProfile,
      userRole: editForm.userRole,
    })
    if (res.data.code === 0) {
      message.success('更新成功')
      editModalVisible.value = false
      fetchData()
    } else {
      message.error('更新失败，' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('请求异常，请稍后重试')
    console.error('编辑用户失败:', error)
  } finally {
    editSubmitting.value = false
  }
}

/** 关闭编辑弹窗 */
const handleEditCancel = () => {
  editFormRef.value?.resetFields()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 12px;
}

:deep(.ant-table-cell) {
  vertical-align: middle;
}
</style>
