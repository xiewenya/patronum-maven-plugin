package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.Config;

import java.io.File;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public interface Parser {
    List<Config> parser(File file);

    List<Config> parser(String code);
}
