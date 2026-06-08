declare namespace API {
  type addUserParams = {
    userAddRequest: UserAddRequest
  }

  type ArticleAiModifyOutlineRequest = {
    /** 任务id */
    taskId?: string
    /** 用户的修改建议 */
    modifySuggestion?: string
  }

  type ArticleConfirmOutlineRequest = {
    /** 任务id */
    taskId?: string
    /** 用户修改后的大纲列表 */
    selectOutlineList?: OutlineSection[]
  }

  type ArticleConfirmTitleRequest = {
    /** 任务id */
    taskId?: string
    /** 用户挑选的主标题 */
    selectedMainTitle?: string
    /** 用户挑选的副标题 */
    selectedSubTitle?: string
    /** 用户追加的标题描述（可选） */
    userDescription?: string
  }

  type ArticleCreateRequest = {
    /** 文章选题 */
    topic?: string
    /** 文章风格 */
    style?: string
    /** 文章配图生成方式 */
    enabledImageMethods?: string[]
  }

  type ArticleQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userId?: number
    status?: string
  }

  type ArticleVO = {
    id?: number
    taskId?: string
    userId?: number
    topic?: string
    userDescription?: string
    mainTitle?: string
    subTitle?: string
    outline?: string
    content?: string
    fullContent?: string
    coverImage?: string
    images?: string
    status?: string
    phase?: string
    errorMessage?: string
    createTime?: string
    completedTime?: string
  }

  type BaseResponseArticleVO = {
    code?: number
    data?: ArticleVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseListOutlineSection = {
    code?: number
    data?: OutlineSection[]
    message?: string
  }

  type BaseResponseListPaymentRecordVO = {
    code?: number
    data?: PaymentRecordVO[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageArticleVO = {
    code?: number
    data?: PageArticleVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type BaseResponseVoid = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type getArticleParams = {
    taskId: string
  }

  type getProgressParams = {
    taskId: string
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type LoginUserVO = {
    /** 用户id */
    id?: number
    /** 账号名称 */
    userAccount?: string
    /** 用户名 */
    username?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色：user/admin */
    userRole?: string
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type OutlineSection = {
    section?: number
    title?: string
    points?: string[]
  }

  type PageArticleVO = {
    records?: ArticleVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageArticleVO
    searchCount?: PageArticleVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageUserVO = {
    records?: UserVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageUserVO
    searchCount?: PageUserVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PaymentRecordVO = {
    /** 主键 */
    id?: number
    /** 主键 */
    userId?: number
    /** Stripe Checkout Session ID */
    stripeSessionId?: string
    /** Stripe 支付意向ID */
    stripePaymentIntentId?: string
    /** 金额（美元） */
    amount?: number
    /** 货币 */
    currency?: string
    /** 状态 */
    status?: string
    /** 产品类型 */
    productType?: string
    /** 描述 */
    description?: string
    /** 退款时间 */
    refundTime?: string
    /** 退款原因 */
    refundReason?: string
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
  }

  type refundParams = {
    /** 退款原因 */
    reason?: string
  }

  type SseEmitter = {
    timeout?: number
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    username?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
    vipTime?: string
  }

  type UserAddRequest = {
    /** 用户昵称 */
    username?: string
    /** 账号 */
    userAccount?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色: user, admin */
    userRole?: string
  }

  type UserLoginRequest = {
    /** 用户名 */
    userAccount?: string
    /** 密码 */
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** id */
    id?: number
    /** 用户昵称 */
    userName?: string
    /** 账号 */
    userAccount?: string
    /** 简介 */
    userProfile?: string
    /** 用户角色：user/admin/ban */
    userRole?: string
  }

  type UserRegisterRequest = {
    /** 用户名 */
    userAccount?: string
    /** 密码 */
    userPassword?: string
    /** 确认密码 */
    checkPassword?: string
  }

  type UserUpdateRequest = {
    /** id */
    id?: number
    /** 用户昵称 */
    userName?: string
    /** 用户头像 */
    userAvatar?: string
    /** 简介 */
    userProfile?: string
    /** 用户角色：user/admin */
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    username?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
