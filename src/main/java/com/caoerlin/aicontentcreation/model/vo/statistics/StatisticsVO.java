package com.caoerlin.aicontentcreation.model.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统计数据 VO
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据统计模型")
public class StatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 今日创作数量
     */
    @Schema(description = "今日创作数量")
    private Long todayCount;

    /**
     * 本周创作数量
     */
    @Schema(description = "本周创作数量")
    private Long weekCount;

    /**
     * 本月创作数量
     */
    @Schema(description = "本月创作数量")
    private Long monthCount;

    /**
     * 总创作数量
     */
    @Schema(description = "总创作数量")
    private Long totalCount;

    /**
     * 成功率（百分比）
     */
    @Schema(description = "成功率（百分比）")
    private Double successRate;

    /**
     * 平均耗时（毫秒）
     */
    @Schema(description = "平均耗时（毫秒）")
    private Integer avgDurationMs;

    /**
     * 活跃用户数（本周）
     */
    @Schema(description = "活跃用户数（本周）")
    private Long activeUserCount;

    /**
     * 总用户数
     */
    private Long totalUserCount;

    /**
     * VIP 用户数
     */
    @Schema(description = "总用户数")
    private Long vipUserCount;

    /**
     * 配额总使用量
     */
    @Schema(description = "配额总使用量")
    private Long quotaUsed;
}
