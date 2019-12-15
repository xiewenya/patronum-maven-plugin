package com.bresai.expecto.patronum.core.git;

import org.apache.commons.lang3.StringUtils;
import pl.project13.core.log.LoggerBridge;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
public class NativeGitOperator implements GitOperator{

    private LoggerBridge log;

    private NativeGitRunner nativeGitRunner;

    public NativeGitOperator(@Nonnull NativeGitRunner nativeGitRunner) {
        this.nativeGitRunner = nativeGitRunner;
        this.log = nativeGitRunner.getLog();
    }

    @Override
    public String switchBranch(String branchName) {
        try {
            return nativeGitRunner.runGitCommand(nativeGitRunner.dotGitDirectory, nativeGitRunner.getNativeGitTimeoutInMs(), "checkout " + branchName);
        } catch (NativeGitRunner.NativeCommandException e) {
            log.info(e.getStdout());
            log.error(e.getStderr());
            return "";
        }
    }

    @Override
    public String diffBranch(String currentBranchName, String remoteBranchName) {

        if (StringUtils.isEmpty(currentBranchName)) {
            currentBranchName = "";
        }

        try {
            return nativeGitRunner.runGitCommand(nativeGitRunner.dotGitDirectory, nativeGitRunner.getNativeGitTimeoutInMs(), "diff " + currentBranchName + " " + remoteBranchName);
        } catch (NativeGitRunner.NativeCommandException e) {
            log.info(e.getStdout());
            log.error(e.getStderr());
            return "";
        }
    }

    @Override
    public String diffBranchNameStatus(String currentBranchName, String remoteBranchName) {
        return diffBranchNameStatus(currentBranchName, remoteBranchName, null);
    }

    @Override
    public String diffBranchNameStatus(String currentBranchName, String remoteBranchName, String type) {

        if (StringUtils.isEmpty(currentBranchName)) {
            currentBranchName = "";
        }

        String command = StringUtils.isNotEmpty(type)
                && "name".equalsIgnoreCase(type) ?
                "diff --name-only " : "diff --name-status ";
        try {
            return nativeGitRunner.runGitCommand(nativeGitRunner.dotGitDirectory, nativeGitRunner.getNativeGitTimeoutInMs(), command + remoteBranchName + " " + currentBranchName);
        } catch (NativeGitRunner.NativeCommandException e) {
            log.info(e.getStdout());
            log.error(e.getStderr());
            return "";
        }
    }

    @Override
    public String getCommitId(String remoteBranchName) {
        try {
            return nativeGitRunner.runGitCommand(nativeGitRunner.dotGitDirectory, nativeGitRunner.getNativeGitTimeoutInMs(), "rev-parse " + remoteBranchName);
        } catch (NativeGitRunner.NativeCommandException e) {
            log.info(e.getStdout());
            log.error(e.getStderr());
            return "";
        }
    }

    @Override
    public String getFileChangedList(String remoteBranchName) {
        try {
            return nativeGitRunner.runGitCommand(nativeGitRunner.dotGitDirectory, nativeGitRunner.getNativeGitTimeoutInMs(), "rev-parse " + remoteBranchName);
        } catch (NativeGitRunner.NativeCommandException e) {
            log.info(e.getStdout());
            log.error(e.getStderr());
            return "";
        }
    }

    @Override
    public String getFileFromRemote(String branchName, String uri) {
        try {
            return nativeGitRunner.runGitCommand(nativeGitRunner.dotGitDirectory, nativeGitRunner.getNativeGitTimeoutInMs(),
                    "show " + branchName + ":" + uri);
        } catch (IOException e) {
            return "";
        }
    }
}
