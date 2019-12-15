package com.bresai.expecto.patronum.core.git;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
public interface GitOperator {
    String switchBranch(String branchName);

    String diffBranch(String currentBranchName, String remoteBranchName);

    String diffBranchNameStatus(String currentBranchName, String remoteBranchName);

    String diffBranchNameStatus(String currentBranchName, String remoteBranchName, String type);

    String getCommitId(String remoteBranchName);

    String getFileChangedList(String remoteBranchName);

    String getFileFromRemote(String branchName, String uri);
}
