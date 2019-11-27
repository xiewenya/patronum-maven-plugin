package com.acard.backend.service.impl;

import com.acard.backend.constant.*;
import com.acard.backend.constant.enumeration.RepayOrderState;
import com.acard.backend.core.exception.ServiceException;
import com.acard.backend.core.service.AbstractService;
import com.acard.backend.dto.response.*;
import com.acard.backend.exception.RecordNotFoundException;
import com.acard.backend.export.pdf.CommonTools;
import com.acard.backend.export.task.EntStaffMonthBillExportManager;
import com.acard.backend.export.task.MonthBillData;
import com.acard.backend.export.task.StatementExportManager;
import com.acard.backend.mapper.EntStaffMonthBillMapper;
import com.acard.backend.model.*;
import com.acard.backend.mq.RocketMQConstants;
import com.acard.backend.query.ConsumeOrderQuery;
import com.acard.backend.service.*;
import com.acard.backend.thirdparty.aliyun.OssService;
import com.acard.backend.thirdparty.aliyun.SMTPMailClient;
import com.acard.backend.thirdparty.aliyun.bean.AttachInfo;
import com.acard.backend.thirdparty.sms.SingleSmsRequest;
import com.acard.backend.thirdparty.sms.SmsService;
import com.acard.backend.util.AsyncUtil;
import com.acard.backend.util.FileUtil;
import com.acard.backend.util.RedisUtils;
import com.acard.backend.util.TimeUtils;
import com.acard.backend.util.qrcode.QRCodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.collect.Lists;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example.Criteria;
import tk.mybatis.mapper.util.StringUtil;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.acard.backend.constant.Constant.*;
import static com.acard.backend.constant.ConsumeOrderStatus.*;
import static com.acard.backend.constant.SmsTemplateType.STAFF_MONTH_BILL;
import static com.acard.backend.export.task.AbstractExportManager.EMAIL_SENT;

public class NacosValueTestEmpty {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosValueTest.class);

    @Autowired
    private String string;
}

