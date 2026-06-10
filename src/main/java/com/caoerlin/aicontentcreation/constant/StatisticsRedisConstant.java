package com.caoerlin.aicontentcreation.constant;

public interface StatisticsRedisConstant {
    /**
     * 系统统计数据redis key
     */
    String STATISTICS_CACHE_KEY = "Statistics:overview";

    /**
     * 系统统计数据TTL
     */
    long STATISTICS_CACHE_KEY_TTL = 60 * 60 * 1000L;
}
