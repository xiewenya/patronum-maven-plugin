package com.bresai.expecto.patronum.core.bean;

import lombok.Data;

import java.util.Map;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
@Data
public class NacosValueBean implements ConfigBean{

    private String configName;

    private String defaultValue;

    private Map<String, String> envValueMap;

    private boolean isAutoRefreshed;

    private JavaFileBean fileMeta;

}
