package com.bresai.expecto.patronum.core.file;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
public interface DataWalker {
    List<File> search(File dir, Predicate<File> predicate);

    Predicate<File> excludeFiles(List<String> excludeFileNames);

    Predicate<File> searchSpecified(String fileName);

    Predicate<File> searchWithKeyword(String fileName);

    Predicate<File> searchJavaFiles();

    Predicate<File> searchXmlFiles();
}
