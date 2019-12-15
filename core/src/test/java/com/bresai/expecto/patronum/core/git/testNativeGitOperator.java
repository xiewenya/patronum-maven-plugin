package com.bresai.expecto.patronum.core.git;

import com.bresai.expecto.patronum.core.AheadBehindTest;
import com.bresai.expecto.patronum.core.StdOutLoggerBridge;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import pl.project13.core.NativeGitProvider;

import java.io.File;
import java.io.InputStream;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
@Slf4j
public class testNativeGitOperator extends AheadBehindTest<NativeGitProvider> {

    private NativeGitOperator nativeGitOperator;

    private StdOutLoggerBridge loggerBridge = new StdOutLoggerBridge(false);

    @Override
    protected NativeGitProvider gitProvider() {
        return NativeGitProvider.on(localRepository.getRoot(), 1000L, loggerBridge);
    }

    @Override
    protected void extraSetup() {
        nativeGitOperator = new NativeGitOperator(NativeGitRunner.of(localRepository.getRoot(), 1000L, loggerBridge));
        gitProvider.setEvaluateOnCommit("HEAD");
    }


    @Test
    public void testSwitchBranchName() throws Exception {
        createLocalCommit();
        localRepositoryGit.branchCreate().setName("dev").call();
        log.info(localRepositoryGit.branchList().call().toString());
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        log.info(nativeGitOperator.switchBranch("dev"));

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

        log.info(nativeGitOperator.switchBranch("dev"));
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        log.info(nativeGitOperator.diffBranch("master", "dev"));
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
        log.info(nativeGitOperator.switchBranch("dev"));
        log.info(gitProvider.getBranchName());
        log.info(gitProvider.getCommitId());

        File newFile = localRepository.newFile("NacosValueTest.java");

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("NacosValueTest.java");
        writeFile(is, newFile);
        localRepositoryGit.add().addFilepattern(newFile.getName()).call();
        localRepositoryGit.commit().setMessage("NacosValueTest").call();
        log.info(nativeGitOperator.diffBranch("dev", "origin/master"));
    }

    @Test
    public void testDiffBranchFileNameList() throws Exception {
        nativeGitOperator.switchBranch("master");

        File fileToDelete = createAndPushCommit("fileToDelete.java");
        File fileToModify = createAndPushCommit("fileToModify.java");

        //create dev branch
        localRepositoryGit.branchCreate().setName("dev").call();
        nativeGitOperator.switchBranch("dev");

        removeAndPushCommit(fileToDelete);

        localRepositoryGit.add().addFilepattern(fileToModify.getName()).call();
        writeFile("fileToModify and fileToModify", fileToModify, true);
        localRepositoryGit.commit().setMessage("modify").call();
        localRepositoryGit.push().call();
        log.info(nativeGitOperator.diffBranchNameStatus("dev", "origin/master"));

        createAndPushCommit("fileToAdd.java", "fileToAdd");
        createAndPushCommit("fileToAdd2.java", "fileToAdd2");

        log.info(nativeGitOperator.diffBranchNameStatus("dev", "origin/master"));


    }

    @Test
    public void testGetCommitId() throws Exception {
        createLocalCommit();

        //create dev branch
        localRepositoryGit.branchCreate().setName("dev").call();
        log.info(nativeGitOperator.getCommitId("origin/master"));
    }

    @Test
    public void testGetFileFromRemote() throws Exception {
        createLocalCommit();

        String commitId = nativeGitOperator.getCommitId("origin/master");
        log.info(commitId);
        //commit file to master
        File newFile = localRepository.newFile("NacosValueTest.java");

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("NacosValueTest.java");
        writeFile(is, newFile);
        localRepositoryGit.add().addFilepattern(newFile.getName()).call();
        localRepositoryGit.commit().setMessage("NacosValueTest").call();
        localRepositoryGit.push().call();

        localRepositoryGit.getRepository().getRemoteNames();
        String str = nativeGitOperator.getFileFromRemote("origin/master", newFile.getName());
        log.info(str);
    }
}
