package com.acard.backend.service.impl;

import com.acard.backend.constant.*;
import com.acard.backend.dto.response.*;
import com.acard.backend.model.*;
import com.acard.backend.service.*;
import com.acard.backend.thirdparty.aliyun.SMTPMailClient;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static com.acard.backend.constant.Constant.*;
import static com.acard.backend.constant.ConsumeOrderStatus.*;

public class NacosValueTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosValueTest.class);

    //test1
    @NacosValue("${account.date}")//subtest1
    //test2
    private String billDate;//test3

    /**
     * test4
     */
    @NacosValue("${account.date:1000}")
    private String test;

    @NacosValue(value = "${settling.duration.days}")
    /**
     * test5
     */
    private Integer settlingDuration;

    //test6
    //test7
    @NacosValue(value = "${repay.month.bill.trailEntId:10000}", autoRefreshed = true)
    private long trailEntId;

    @NacosValue(value = "${bill.repayment.lastDay}", autoRefreshed = true)
    private int repaymentLastDay;

    @NacosValue(value = "${SITI.repay.day:15}", autoRefreshed = true)
    private String repayDay;

    @NacosValue(value = "${mail.html.entStaffMonthBill}", autoRefreshed = true)
    private String staffMonthBillEmailHtml;

    @NacosValue(value = "${mail.html.entStaffMonthBillLite}", autoRefreshed = true)
    private String staffMonthBillEmailLiteHtml;

    @NacosValue(value = "${month.bill.overdue.exception.emails}", autoRefreshed = true)
    private String notifyEmails;

    @Autowired
    private SMTPMailClient smtpMailClient;

    @Value("${env}")
    private String env;
}

