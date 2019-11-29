package com.bresai.expecto.patronum.core.file;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.parser.JavaFileParser;
import com.bresai.expecto.patronum.core.parser.NacosValueResolver;
import com.sun.istack.internal.NotNull;
import pl.project13.core.log.LoggerBridge;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public class JavaWalker extends FileWalker {

    private JavaFileParser javaFileParser;

    public JavaWalker(LoggerBridge log) {
        super(log);
        javaFileParser = new JavaFileParser(log, new NacosValueResolver());
    }

    public List<ConfigBean> walkThroughPath(@NotNull File gitDir){

        Predicate<File> predicate = this.searchJavaFiles();

        List<File> fileList = search(gitDir, predicate);

        List<ConfigBean> list = new LinkedList<>();
        fileList.forEach(file -> list.addAll(javaFileParser.parser(file)));

        return list;
    }

}

