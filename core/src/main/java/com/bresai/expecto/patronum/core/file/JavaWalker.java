package com.bresai.expecto.patronum.core.file;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.result.Result;
import com.bresai.expecto.patronum.core.parser.JavaFileParser;
import com.bresai.expecto.patronum.core.parser.NacosValueResolver;
import pl.project13.core.log.LoggerBridge;

import javax.annotation.Nonnull;
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

    public Result<ConfigBean> walkThroughPath(@Nonnull File gitDir){

        Predicate<File> predicate = this.searchJavaFiles();

        log.info("search dir {}", gitDir.getAbsolutePath());

        List<File> fileList = search(gitDir, predicate);

        List<ConfigBean> list = new LinkedList<>();
        fileList.forEach(file -> list.addAll(javaFileParser.parser(file)));

        Result<ConfigBean> result = new Result<>(list);

        return result;
    }

}

