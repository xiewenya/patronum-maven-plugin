package com.bresai.expecto.patronum.core.bean;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/9
 * @content:
 */
@Getter
@Setter
public class FileBean {
    private File file;

    private String relativePath;
    private List<ConfigBean> configBeanList;

    public String getRelativePath(File projectDir){
        if (file == null){
            return "";
        }

        return projectDir.toURI().relativize(file.toURI()).getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileBean fileBean = (FileBean) o;

        return new EqualsBuilder()
                .append(file, fileBean.file)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(file)
                .toHashCode();
    }
}
