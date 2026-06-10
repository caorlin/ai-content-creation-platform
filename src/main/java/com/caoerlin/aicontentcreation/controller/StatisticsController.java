package com.caoerlin.aicontentcreation.controller;

import com.caoerlin.aicontentcreation.common.annotation.AuthCheck;
import com.caoerlin.aicontentcreation.common.response.BaseResponse;
import com.caoerlin.aicontentcreation.common.response.ResultUtils;
import com.caoerlin.aicontentcreation.constant.UserConstant;
import com.caoerlin.aicontentcreation.model.vo.statistics.StatisticsVO;
import com.caoerlin.aicontentcreation.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("statistics")
@Tag(name = "StatisticsController", description = "数据统计接口")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("overview")
    @Operation(summary = "数据统计接口")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<StatisticsVO> getStatistics() {
        return ResultUtils.success(statisticsService.getStatistics());
    }
}
