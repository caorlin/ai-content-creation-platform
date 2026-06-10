// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 数据统计接口 GET /statistics/overview */
export async function getStatistics(options?: { [key: string]: any }) {
  return request<API.BaseResponseStatisticsVO>('/statistics/overview', {
    method: 'GET',
    ...(options || {}),
  })
}
