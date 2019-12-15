package com.bresai.expecto.patronum.core.printer;

import com.bresai.expecto.patronum.core.bean.Config;

import java.io.IOException;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
public interface Printer {

    void close() throws IOException;

    boolean title(String title);

    boolean subTitle(String title);

    boolean subSubTitle(String subSubTitle);

    boolean subSubSubTitle(String subSubSubTitle);

    boolean config(Config config);

    boolean configs(List<Config> configs);
}
