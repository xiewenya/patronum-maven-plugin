package com.bresai.expecto.patronum.core.walker;

import com.bresai.expecto.patronum.core.bean.Config;
import com.bresai.expecto.patronum.core.bean.result.FileDiff;
import com.bresai.expecto.patronum.core.enums.FileDiffEnum;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
public interface DataWalker {
    Map<FileDiffEnum, List<FileDiff>> searchGitFileDiff(String result);

    List<File> search(File dir, Predicate<File> predicate);

    Predicate<File> excludeFiles(List<String> excludeFileNames);

    Predicate<File> searchSpecified(String fileName);

    Predicate<File> searchWithKeyword(String fileName);

    Predicate<File> searchJavaFiles();

    Predicate<File> searchXmlFiles();

    List<Config> parseFromRemoteFile(String targetRemoteBranch, String filepath);
}
