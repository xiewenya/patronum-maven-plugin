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
public class FileReporter implements Reporter{

    private Printer printer;

    private String fileName;

    public FileReporter(Printer printer, String fileName) {
        this.printer = printer;
        this.fileName = fileName;
    }

    public void report(List<Config> newConfigs, List<Config> removedConfigs) throws IOException, IllegalAccessException, InstantiationException {


        printer.title("上线准备报告");
        printer.subTitle("本次新增的配置");
        printer.subSubTitle("新增nacos配置");
        printer.configs(newConfigs);
        printer.subTitle("本次待修改的配置");
        printer.subSubTitle("待修改nacos配置");
        printer.subTitle("本次删除的配置（仅作为提示，不建议删除线上配置）");
        printer.subTitle("本次删除的nacos配置");
        printer.configs(removedConfigs);
        printer.close();
    }
}
