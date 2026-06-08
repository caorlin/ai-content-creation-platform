// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 创建支付记录接口 POST /payment/create/vip */
export async function createVipPaymentRecord(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/payment/create/vip', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 获取支付信息列表接口 GET /payment/list */
export async function getPaymentRecordList(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPaymentRecordVO>('/payment/list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 用户退款接口 POST /payment/refund */
export async function refund(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.refundParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/payment/refund', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
