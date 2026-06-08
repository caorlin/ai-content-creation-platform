package com.caoerlin.aicontentcreation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.model.entity.PaymentRecord;
import com.caoerlin.aicontentcreation.service.PaymentRecordService;
import com.caoerlin.aicontentcreation.mapper.PaymentRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【payment_record(支付记录表)】的数据库操作Service实现
* @createDate 2026-06-08 17:07:27
*/
@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
    implements PaymentRecordService{

}




