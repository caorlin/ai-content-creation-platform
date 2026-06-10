package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.vo.statistics.StatisticsVO;

public interface StatisticsService {
    /**
     * 获取系统统计数据
     *
     * @return 统计数据
     */
    StatisticsVO getStatistics();
}
