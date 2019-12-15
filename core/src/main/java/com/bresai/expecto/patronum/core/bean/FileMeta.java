package com.bresai.expecto.patronum.core.bean;

import com.bresai.expecto.patronum.core.utils.ProjectFileUtils;
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
public class FileMeta {
    private File file;

    private String relativePath;
    private List<Config> configList;

    public String getRelativePath(File projectDir){
        return ProjectFileUtils.getRelativePath(projectDir, file);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileMeta fileMeta = (FileMeta) o;

        return new EqualsBuilder()
                .append(file, fileMeta.file)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(file)
                .toHashCode();
    }
}
