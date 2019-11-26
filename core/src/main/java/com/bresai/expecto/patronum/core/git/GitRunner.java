package com.bresai.expecto.patronum.core.git;

import java.io.File;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
public interface GitRunner {
    boolean tryCheckEmptyRunGitCommand(File directory, long nativeGitTimeoutInMs, String gitCommand);

    String runQuietGitCommand(File directory, long nativeGitTimeoutInMs, String gitCommand);

    String runGitCommand(File directory, long nativeGitTimeoutInMs, String gitCommand) throws NativeGitRunner.NativeCommandException;
}
