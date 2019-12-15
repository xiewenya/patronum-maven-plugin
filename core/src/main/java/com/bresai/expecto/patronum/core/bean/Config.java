package com.bresai.expecto.patronum.core.bean;

import com.github.javaparser.ast.comments.Comment;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
@Getter
@Setter
public class Config {
    /**
     * the name of config
     */
    private String configName;

    protected FileMeta fileMeta;

    protected List<Comment> comments;

    public FileMeta getFileMeta() {
        return fileMeta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Config bean = (Config) o;

        return new EqualsBuilder()
                .append(configName, bean.configName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(configName)
                .toHashCode();
    }
}
