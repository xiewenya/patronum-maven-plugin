package com.bresai.expecto.patronum.core.walker;

import com.bresai.expecto.patronum.core.git.GitOperator;
import com.bresai.expecto.patronum.core.git.NativeGitOperator;
import com.bresai.expecto.patronum.core.git.NativeGitRunner;
import com.bresai.expecto.patronum.core.parser.JavaFileParser;
import com.bresai.expecto.patronum.core.parser.NacosValueResolver;
import com.bresai.expecto.patronum.core.parser.Parser;
import lombok.Getter;
import pl.project13.core.log.LoggerBridge;

import java.io.File;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
@Getter
public class JavaWalker extends FileWalker {


    public JavaWalker(LoggerBridge log, File dotGitRepo, File projectDir) {
        super(log, dotGitRepo, projectDir);
    }

    @Override
    public GitOperator setGitOperator(LoggerBridge log, File dotGitRepo) {
        return new NativeGitOperator(NativeGitRunner.of(dotGitRepo, 1000L, log));
    }

    @Override
    public Parser setParser(LoggerBridge log) {
        return new JavaFileParser(log, new NacosValueResolver());
    }
}

