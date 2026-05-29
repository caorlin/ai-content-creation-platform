package com.caoerlin.aicontentcreation.controller;

import com.caoerlin.aicontentcreation.common.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return new BaseResponse<>(200,"ok");
    }
}
