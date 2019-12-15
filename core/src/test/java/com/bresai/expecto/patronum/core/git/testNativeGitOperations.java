package com.bresai.expecto.patronum.core.git;

import com.bresai.expecto.patronum.core.AheadBehindTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import pl.project13.core.NativeGitProvider;
import pl.project13.core.log.StdOutLoggerBridge;

import java.io.File;
import java.io.InputStream;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
@Slf4j
public class testNativeGitOperations extends AheadBehindTest<NativeGitProvider> {

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

        //create dev branch
        localRepositoryGit.branchCreate().setName("dev").call();
        log.info(localRepositoryGit.branchList().call().toString());
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        //switch to dev branch
        log.info(nativeGitOperations.switchBranch("dev"));
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        File newFile = localRepository.newFile("NacosValueTest.java");

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("NacosValueTest.java");
        writeFile(is, newFile);
        localRepositoryGit.add().addFilepattern(newFile.getName()).call();
        localRepositoryGit.commit().setMessage("NacosValueTest").call();
        log.info(nativeGitOperations.diffBranch("dev", "origin/master"));
    }

    @Test
    public void testGetCommitId() throws Exception {
        createLocalCommit();

        //create dev branch
        localRepositoryGit.branchCreate().setName("dev").call();
        log.info(nativeGitOperations.getCommitId("origin/master"));
    }

    @Test
    public void testGetFileFromRemote() throws Exception {
        createLocalCommit();

        String commitId = nativeGitOperations.getCommitId("origin/master");
        log.info(commitId);
        //commit file to master
        File newFile = localRepository.newFile("NacosValueTest.java");

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("NacosValueTest.java");
        writeFile(is, newFile);
        localRepositoryGit.add().addFilepattern(newFile.getName()).call();
        localRepositoryGit.commit().setMessage("NacosValueTest").call();
        localRepositoryGit.push().call();

        localRepositoryGit.getRepository().getRemoteNames();
        String str = nativeGitOperations.getFileFromRemote("origin/master", newFile.getName());
        log.info(str);
    }
}
