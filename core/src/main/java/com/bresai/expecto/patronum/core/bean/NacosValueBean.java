package com.bresai.expecto.patronum.core.bean;

import com.github.javaparser.ast.comments.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
@Setter
@Getter
public class NacosValueBean implements ConfigBean{

    private String configName;

    private String defaultValue;

    private Map<String, String> envValueMap;

    private boolean isAutoRefreshed;

    private JavaFileBean fileMeta;

    private List<Comment> comments;

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
