package com.caoerlin.aicontentcreation.controller;

import com.caoerlin.aicontentcreation.common.annotation.AuthCheck;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.common.response.BaseResponse;
import com.caoerlin.aicontentcreation.common.response.ResultUtils;
import com.caoerlin.aicontentcreation.constant.UserConstant;
import com.caoerlin.aicontentcreation.model.entity.PaymentRecord;
import com.caoerlin.aicontentcreation.model.vo.payment.PaymentRecordVO;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;
import com.caoerlin.aicontentcreation.service.PaymentRecordService;
import com.caoerlin.aicontentcreation.service.UserService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("payment")
@Tag(name = "PaymentController", description = "支付接口")
public class PaymentController {
    private final PaymentRecordService paymentRecordService;
    private final UserService userService;

    @PostMapping("/create/vip")
    @Operation(summary = "创建支付记录接口")
    public BaseResponse<String> createVipPaymentRecord(HttpServletRequest request) {
        LoginUserVO loginUser = userService.getLoginUser(request);
        try {
            String sessionUrl = paymentRecordService.createVipPaymentSession(loginUser.getId());
            return ResultUtils.success(sessionUrl);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("创建支付会话失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建支付会话失败");
        }
    }

    @PostMapping("refund")
    @Operation(summary = "用户退款接口")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Parameter(name = "reason", description = "退款原因", required = false)
    public BaseResponse<Boolean> refund(@RequestParam(required = false) String reason, HttpServletRequest request) {
        LoginUserVO loginUser = userService.getLoginUser(request);
        try {
            boolean success = paymentRecordService.handleRefund(loginUser.getId(), reason);
            return ResultUtils.success(success);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("用户退款失败,userId={}", loginUser.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退款失败");
        }
    }

    @GetMapping("list")
    @Operation(summary = "获取支付信息列表接口")
    public BaseResponse<List<PaymentRecordVO>> getPaymentRecordList(HttpServletRequest request) {
        LoginUserVO loginUser = userService.getLoginUser(request);
        List<PaymentRecordVO> result = paymentRecordService.getPaymentList(loginUser.getId());
        return ResultUtils.success(result);
    }
}
