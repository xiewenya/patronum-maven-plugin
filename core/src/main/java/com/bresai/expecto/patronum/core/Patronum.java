package com.bresai.expecto.patronum.core;

import com.bresai.expecto.patronum.core.bean.Config;
import lombok.Getter;
import pl.project13.core.log.LoggerBridge;

import java.io.File;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
@Getter
public abstract class Patronum {
    protected LoggerBridge log;

    private File projectDir;

    public Patronum(LoggerBridge log, File projectDir) {
        this.log = log;
        this.projectDir = projectDir;
    }

    public abstract List<Config> getNewConfig();

    public abstract List<Config> getRemovedConfig();
}
