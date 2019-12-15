package com.bresai.expecto.patronum.core.file;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.bean.FileBean;
import com.bresai.expecto.patronum.core.git.NativeGitOperations;
import com.bresai.expecto.patronum.core.git.NativeGitRunner;
import com.bresai.expecto.patronum.core.parser.JavaFileParser;
import com.bresai.expecto.patronum.core.parser.NacosValueResolver;
import com.bresai.expecto.patronum.core.result.Result;
import lombok.Getter;
import pl.project13.core.log.LoggerBridge;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
@Getter
public class JavaWalker extends FileWalker {

    private JavaFileParser javaFileParser;

    private NativeGitOperations gitOperations;

    public JavaWalker(LoggerBridge log, File dotGitRepo) {
        super(log);
        javaFileParser = new JavaFileParser(log, new NacosValueResolver());
        gitOperations = new NativeGitOperations(NativeGitRunner.of(dotGitRepo, 1000L, log));
    }

    public Result<ConfigBean> walkThroughLocal(@Nonnull File gitDir){

        Predicate<File> predicate = this.searchJavaFiles();

        log.info("search dir {}", gitDir.getAbsolutePath());

        List<File> fileList = search(gitDir, predicate);

        List<ConfigBean> list = new LinkedList<>();
        fileList.forEach(file -> list.addAll(javaFileParser.parser(file)));

        return new Result<>(list);
    }

    public Result<ConfigBean> walkThroughRemote(@Nonnull String remoteBranch, @Nonnull File projectDir, Set<FileBean> set){
        List<ConfigBean> newConfigBean = new LinkedList<>();
        List<ConfigBean> removedConfigBean = new LinkedList<>();

        set.forEach(fileBean -> {
            String relativePath = fileBean.getRelativePath(projectDir);
            String code = gitOperations.getFileFromRemote("origin/master", relativePath);
            List<ConfigBean> remoteConfigBean = javaFileParser.parser(code);
            List<ConfigBean> localConfigBean = fileBean.getConfigBeanList();

        });


        return null;

//        return new Result<>(list);
    }



}

