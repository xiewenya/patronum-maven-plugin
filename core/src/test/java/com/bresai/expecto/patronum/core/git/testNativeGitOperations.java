package com.bresai.expecto.patronum.core.git;

import com.bresai.expecto.patronum.core.AheadBehindTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import pl.project13.core.NativeGitProvider;
import pl.project13.core.log.StdOutLoggerBridge;

import java.io.File;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
@Slf4j
public class testNativeGitOperations  extends AheadBehindTest<NativeGitProvider> {

    private NativeGitOperations nativeGitOperations;

    private StdOutLoggerBridge loggerBridge;

    @Override
    protected NativeGitProvider gitProvider() {
        return NativeGitProvider.on(localRepository.getRoot(), 1000L, loggerBridge);
    }

    @Override
    protected void extraSetup() {
        nativeGitOperations = new NativeGitOperations(NativeGitRunner.of(localRepository.getRoot(), 1000L, loggerBridge));
        gitProvider.setEvaluateOnCommit("HEAD");
    }


    @Test
    public void testSwitchBranchName() throws Exception {
        createLocalCommit();
        localRepositoryGit.branchCreate().setName("dev").call();
        log.info(localRepositoryGit.branchList().call().toString());
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        log.info(nativeGitOperations.switchBranch("dev"));

        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());
    }

    @Test
    public void testDiffBranchNoDiff() throws Exception {
        createLocalCommit();
        localRepositoryGit.branchCreate().setName("dev").call();
        log.info(localRepositoryGit.branchList().call().toString());
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        log.info(nativeGitOperations.switchBranch("dev"));
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        log.info(nativeGitOperations.diffBranch("master", "dev"));
    }


    @Test
    public void testDiffBranchOneFileDiff() throws Exception {
        createLocalCommit();
        localRepositoryGit.branchCreate().setName("dev").call();
        log.info(localRepositoryGit.branchList().call().toString());
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        log.info(nativeGitOperations.switchBranch("dev"));
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        File newFile = localRepository.newFile();
        writeFile("hello word", newFile);
        localRepositoryGit.add().addFilepattern(newFile.getName()).call();
        localRepositoryGit.commit().setMessage("hello world").call();
        log.info(nativeGitOperations.diffBranch("master", "dev"));
    }
}
