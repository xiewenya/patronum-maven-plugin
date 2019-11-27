package com.bresai.expecto.patronum.core.file;

import com.sun.istack.internal.NotNull;

import java.io.File;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public class JavaWalker extends FileWalker {

    public File walkThroughPath(@NotNull String path){
        if (path.startsWith("classpath:")){
            String[] classpathEntries = getAllClassPaths();
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        return null;
    }

    private String[] getAllClassPaths() {
        String classpath = System.getProperty("java.class.path");
        return classpath.split(File.pathSeparator);
    }
}

