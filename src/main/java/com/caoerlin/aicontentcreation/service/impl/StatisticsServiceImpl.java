package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caoerlin.aicontentcreation.constant.StatisticsRedisConstant;
import com.caoerlin.aicontentcreation.mapper.ArticleMapper;
import com.caoerlin.aicontentcreation.mapper.UserMapper;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.model.enums.UserRoleEnum;
import com.caoerlin.aicontentcreation.model.vo.statistics.StatisticsVO;
import com.caoerlin.aicontentcreation.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.caoerlin.aicontentcreation.constant.StatisticsRedisConstant.STATISTICS_CACHE_KEY_TTL;
import static com.caoerlin.aicontentcreation.constant.UserConstant.DEFAULT_QUOTA;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public StatisticsVO getStatistics() {
        //先从缓存中获取
        String statisticsStr = redisTemplate.opsForValue().get(StatisticsRedisConstant.STATISTICS_CACHE_KEY);
        if (StrUtil.isNotBlank(statisticsStr)) {
            log.info("从缓存中获取到数据,data={}", statisticsStr);
            return JSONUtil.toBean(statisticsStr, StatisticsVO.class);
        }

        //缓存未命中,重新计算数据
        //今日创作数量
        Long todayCount = countArticleByDateRange(getTodayStart(), LocalDateTime.now());

        //本周创作数量
        Long weekCount = countArticleByDateRange(getWeekStart(), LocalDateTime.now());

        //本月创作数量
        Long monthCount = countArticleByDateRange(getMonthStart(), LocalDateTime.now());

        //总创作数量
        Long totalCount = countArticleTotalRange();

        //成功率（百分比）
        Double successRate = calculateSuccessRate();

        //平均耗时（毫秒）
        Integer avgDurationMs = calculateAvgDuration();

        //活跃用户数（本周）
        Long activeUserCount = countActiveUserRange(getWeekStart());

        //总用户
        Long totalUserCount = countTotalUserRange();

        //vip用户
        Long vipUserCount = countVipUserRange();

        //配额总使用量
        Long quotaUsed = countQuotaUsedRange();

        StatisticsVO vo = StatisticsVO.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .successRate(successRate)
                .avgDurationMs(avgDurationMs)
                .activeUserCount(activeUserCount)
                .totalUserCount(totalUserCount)
                .vipUserCount(vipUserCount)
                .quotaUsed(quotaUsed)
                .build();

        //存入缓存
        redisTemplate.opsForValue().set(
                StatisticsRedisConstant.STATISTICS_CACHE_KEY,
                JSONUtil.toJsonStr(vo),
                STATISTICS_CACHE_KEY_TTL
        );

        log.info("统计数据已缓存,{}ms后过期",STATISTICS_CACHE_KEY_TTL);
        return vo;
    }

    /**
     * 统计配额总使用量
     */
    private Long countQuotaUsedRange() {
        //配额用量 = (初始用量 * 普通用户数) - 当前剩余用量总和
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserRole, UserRoleEnum.USER.getValue());

        try {
            List<User> userList = userMapper.selectList(wrapper);
            //普通用户总量
            Long regularUserCount = (long) userList.size();
            //剩余用量数
            int remainderQuotaCount = userList.stream()
                    .mapToInt(user -> ObjectUtil.isNotNull(user.getQuota()) ? user.getQuota() : 0)
                    .sum();
            return (regularUserCount * DEFAULT_QUOTA) - remainderQuotaCount;
        } catch (Exception e) {
            log.error("配额总使用量统计失败", e);
            return 0L;
        }
    }

    /**
     * 统计VIP用户
     */
    private Long countVipUserRange() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserRole, UserRoleEnum.VIP.getValue())
                .isNotNull(User::getVipTime);
        return userMapper.selectCount(wrapper);
    }

    private Long countTotalUserRange() {
        return userMapper.selectCount(new LambdaQueryWrapper<>());
    }

    /**
     * 统计活跃用户（本周）
     */
    private Long countActiveUserRange(LocalDateTime weekStart) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Article::getCreateTime, weekStart);

        try {
            List<Article> articleList = articleMapper.selectList(wrapper);
            return articleList.stream().map(Article::getUserId).distinct().count();
        } catch (Exception e) {
            log.error("统计活跃用户失败", e);
            return 0L;
        }
    }

    /**
     * 计算平均耗时（从创建到完成的平均时间）
     */
    private Integer calculateAvgDuration() {
        // 查询所有已完成的文章，计算 createTime 到 completedTime 的平均耗时
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatusEnum.COMPLETED.getStatus())
                .isNotNull(Article::getCompletedTime);

        try {
            List<Article> completedArticles = articleMapper.selectList(wrapper);
            if (completedArticles == null || completedArticles.isEmpty()) {
                return 0;
            }

            // 计算每篇文章的耗时（毫秒）
            double avgDuration = completedArticles.stream()
                    .filter(article -> article.getCreateTime() != null && article.getCompletedTime() != null)
                    .mapToLong(article -> {
                        long createMillis = java.sql.Timestamp.valueOf(article.getCreateTime()).getTime();
                        long completedMillis = java.sql.Timestamp.valueOf(article.getCompletedTime()).getTime();
                        return completedMillis - createMillis;
                    })
                    .average()
                    .orElse(0.0);

            return (int) avgDuration;
        } catch (Exception e) {
            log.warn("计算平均耗时失败", e);
        }

        return 0;
    }


    private Double calculateSuccessRate() {
        Long totalRange = countArticleTotalRange();
        if (ObjectUtil.isNull(totalRange) || totalRange == 0) {
            return 0.0;
        }
        //成功数量
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatusEnum.COMPLETED.getStatus());
        Long successRange = articleMapper.selectCount(wrapper);

        return (successRange.doubleValue() / totalRange.doubleValue()) * 100;
    }

    /**
     * 总创作数量
     */
    private Long countArticleTotalRange() {
        return articleMapper.selectCount(new LambdaQueryWrapper<>());
    }


    /**
     * 获取文章在指定时间范围的数量
     */
    private Long countArticleByDateRange(LocalDateTime startTime, LocalDateTime currentTime) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Article::getCreateTime, startTime, currentTime);
        return articleMapper.selectCount(wrapper);
    }

    /**
     * 获取当天开始时间
     */
    private LocalDateTime getTodayStart() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * 获取本周起始时间
     */
    private LocalDateTime getWeekStart() {
        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
    }

    private LocalDateTime getMonthStart() {
        return LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();
    }
}
