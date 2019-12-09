package com.bresai.expecto.patronum.core.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
@Setter
@Getter
public class NacosValueBean extends ConfigBean{

    private String configName;

    private String defaultValue;

    private Map<String, String> envValueMap;

    private boolean isAutoRefreshed;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (comments != null && comments.size() > 0){
            comments.forEach(comment -> builder.append(comment.toString()));
        }

        builder.append(configName).append("(")
                .append(defaultValue).append(",")
                .append(isAutoRefreshed).append(")@")
                .append(fileMeta.toString());
        return builder.toString();
    }
}
