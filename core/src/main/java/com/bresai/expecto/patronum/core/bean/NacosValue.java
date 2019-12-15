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
public class NacosValue extends Config {

    /**
     * default value of nacos value
     */
    private String defaultValue;

    /**
     * values for different env
     */
    private Map<String, String> envValueMap;

    private boolean isAutoRefreshed;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (comments != null && comments.size() > 0){
            comments.forEach(comment -> builder.append(comment.toString()));
        }

        builder.append(getConfigName()).append("(")
                .append(defaultValue).append(",")
                .append(isAutoRefreshed).append(")@")
                .append(fileMeta.toString());
        return builder.toString();
    }
}
