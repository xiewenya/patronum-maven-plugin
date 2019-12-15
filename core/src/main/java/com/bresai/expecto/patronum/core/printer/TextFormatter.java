package com.bresai.expecto.patronum.core.printer;

import com.bresai.expecto.patronum.core.bean.Config;
import com.github.javaparser.ast.comments.Comment;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
public class TextFormatter implements Formatter {

    @Override
    public String title(String title){
        return title + System.lineSeparator() + System.lineSeparator();
    }

    @Override
    public String subTitle(String subTitle){
        return subTitle + System.lineSeparator();
    }

    @Override
    public String subSubTitle(String subSubTitle){
        return subSubTitle + System.lineSeparator();
    }

    @Override
    public String subSubSubTitle(String subSubSubTitle){
        return subSubSubTitle + System.lineSeparator();
    }

    @Override
    public String config(Config config){
        StringBuilder builder = new StringBuilder();

        List<Comment> comments = config.getComments();
        if (CollectionUtils.isNotEmpty(comments)){
            builder.append(comments(comments));
        }

        return builder.append(config.getConfigName())
                .append("=")
                .append(System.lineSeparator())
                .toString();
    }

    @Override
    public String comment(Comment comment){
        return comment.toString();
    }

    @Override
    public String comments(List<Comment> comments){
        StringBuilder builder = new StringBuilder();
        comments.forEach(comment ->
                builder.append(comment(comment))
                        .append(System.lineSeparator()));
        return builder.toString();
    }
}
