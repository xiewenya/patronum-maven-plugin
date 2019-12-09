package com.bresai.expecto.patronum.core.bean;

import com.github.javaparser.ast.comments.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
@Getter
@Setter
public class ConfigBean {
    protected FileBean fileMeta;

    protected List<Comment> comments;

    public FileBean getFileMeta() {
        return fileMeta;
    }
}
