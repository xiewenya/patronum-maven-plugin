package com.bresai.expecto.patronum.core.printer;

import com.bresai.expecto.patronum.core.bean.Config;
import com.github.javaparser.ast.comments.Comment;

import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
public interface Formatter {
    String title(String title);

    String subTitle(String head);

    String subSubTitle(String subSubTitle);

    String subSubSubTitle(String subSubSubTitle);

    String config(Config config);

    String comment(Comment comment);

    String comments(List<Comment> comments);
}
