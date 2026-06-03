<template>
  <div id="articleListPage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="文章标题">
        <a-input
          v-model:value="searchParams.mainTitle"
          placeholder="输入文章标题"
          allow-clear
          style="width: 200px"
        />
      </a-form-item>
      <a-form-item label="创建时间">
        <a-range-picker
          v-model:value="searchParams.createTimeRange"
          :placeholder="['开始日期', '结束日期']"
          :format="'YYYY-MM-DD'"
          :valueFormat="'YYYY-MM-DD'"
          style="width: 260px"
        />
      </a-form-item>
      <a-form-item label="文章状态">
        <a-select
          v-model:value="searchParams.status"
          placeholder="选择文章状态"
          allow-clear
          style="width: 150px"
          :options="statusOptions"
        />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
          <a-button @click="resetSearch">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      row-key="id"
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      :loading="loading"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'topic'">
          <span>{{ record.topic || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'mainTitle'">
          <a-tooltip :title="record.mainTitle">
            <span class="title-ellipsis">{{ record.mainTitle || '-' }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)" bordered>
            {{ getStatusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'createTime'">
          <span>{{ record.createTime || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="doView(record)">
              <template #icon><EyeOutlined /></template>
              查看
            </a-button>
            <a-button type="link" size="small" @click="doExport(record)">
              <template #icon><DownloadOutlined /></template>
              导出
            </a-button>
            <a-popconfirm
              title="确定删除该文章？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="doDelete(record.id!)"
            >
              <a-button type="link" danger size="small">
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  DownloadOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { listArticle, deleteArticle } from '@/api/articleController.ts'

const router = useRouter()
const data = ref<API.ArticleVO[]>([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive<
  API.ArticleQueryRequest & { mainTitle?: string; createTimeRange?: [string, string] }
>({
  pageNum: 1,
  pageSize: 10,
})

const columns = [
  {
    title: '选题',
    dataIndex: 'topic',
    key: 'topic',
    width: 180,
  },
  {
    title: '标题',
    dataIndex: 'mainTitle',
    key: 'mainTitle',
    ellipsis: true,
  },
  {
    title: '状态',

    key: 'status',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
  },
]

const statusOptions = [
  { value: 'pending', label: '待处理' },
  { value: 'generating', label: '生成中' },
  { value: 'completed', label: '已完成' },
  { value: 'failed', label: '失败' },
]

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING: { label: '待处理', color: 'default' },
  GENERATING: { label: '生成中', color: 'blue' },
  COMPLETED: { label: '已完成', color: 'green' },
  FAILED: { label: '失败', color: 'red' },
}

const getStatusLabel = (status?: string) => statusMap[status ?? '']?.label ?? (status || '-')
const getStatusColor = (status?: string) => statusMap[status ?? '']?.color ?? 'default'

const pagination = computed(() => ({
  current: searchParams.pageNum,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

const fetchData = async () => {
  loading.value = true
  try {
    const { mainTitle, createTimeRange, ...rest } = searchParams
    const params: Record<string, unknown> = { ...rest }
    if (mainTitle) {
      params.mainTitle = mainTitle
    }
    if (createTimeRange && createTimeRange.length === 2) {
      params.createTimeStart = createTimeRange[0]
      params.createTimeEnd = createTimeRange[1]
    }

    const res = await listArticle(params as API.ArticleQueryRequest)
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch {
    message.error('请求失败，请检查网络')
  } finally {
    loading.value = false
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

const resetSearch = () => {
  searchParams.mainTitle = undefined
  searchParams.createTimeRange = undefined
  searchParams.status = undefined
  searchParams.pageNum = 1
  fetchData()
}

const doView = (record: API.ArticleVO) => {
  if (record.taskId) {
    router.push({ name: '文章详情', params: { taskId: record.taskId } })
  } else {
    message.warning('缺少文章标识，无法查看详情')
  }
}

// 导出（客户端 CSV 导出）
const doExport = (record: API.ArticleVO) => {
  const headers = ['选题', '主标题', '副标题', '状态', '当前阶段', '创建时间', '完成时间']
  const row = [
    record.topic ?? '',
    record.mainTitle ?? '',
    record.subTitle ?? '',
    getStatusLabel(record.status),
    record.phase ?? '',
    record.createTime ?? '',
    record.completedTime ?? '',
  ]
  const csvContent =
    '\uFEFF' +
    headers.join(',') +
    '\n' +
    row.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(',')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `文章_${record.mainTitle || record.topic || record.id}.csv`
  link.click()
  URL.revokeObjectURL(url)
  message.success('导出成功')
}

// 删除
const doDelete = async (id: number) => {
  const res = await deleteArticle({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败，' + res.data.message)
  }
}


onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#articleListPage {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}

.title-ellipsis {
  display: inline-block;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

</style>
