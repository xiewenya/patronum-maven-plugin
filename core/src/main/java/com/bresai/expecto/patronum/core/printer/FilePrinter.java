package com.bresai.expecto.patronum.core.printer;

import com.bresai.expecto.patronum.core.bean.Config;
import pl.project13.core.log.LoggerBridge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
public class FilePrinter implements Printer {
    private Formatter formatter;

    private FileWriter fileWriter;

    private String fileName;

    private LoggerBridge log;

    public FilePrinter(String fileName, Formatter formatter, LoggerBridge log) throws IOException {
        this.fileName = fileName;
        this.formatter = formatter;
        this.fileWriter = new FileWriter(new File(fileName));
        this.log = log;
    }


    public FilePrinter(String fileName, LoggerBridge log) throws IOException {
        this.fileName = fileName;
        this.formatter = new TextFormatter();
        this.fileWriter = new FileWriter(new File(fileName));
        this.log = log;
    }

    private boolean writeContent(String content){
        try {
            fileWriter.write(content);
            return true;
        } catch (IOException e) {
            log.warn("content write failed for file {}", this.fileName);
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }

    @Override
    public boolean title(String title){
        return writeContent(formatter.title(title));
    }

    @Override
    public boolean subTitle(String title){
        return writeContent(formatter.subTitle(title));
    }

    @Override
    public boolean subSubTitle(String subSubTitle){
        return writeContent(formatter.subSubTitle(subSubTitle));
    }

    @Override
    public boolean subSubSubTitle(String subSubSubTitle){
        return writeContent(formatter.subSubSubTitle(subSubSubTitle));
    }

    @Override
    public boolean config(Config config){
        return writeContent(formatter.config(config));
    }

    @Override
    public boolean configs(List<Config> configs){
        configs.forEach(config -> {
            writeContent(formatter.config(config));
            writeContent(System.lineSeparator());
        });
        return true;
    }
}
