package com.bresai.expecto.patronum.maven.git;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import junitparams.JUnitParamsRunner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/28
 * @content:
 */
@RunWith(JUnitParamsRunner.class)
public class testPatronumMojo extends GitIntegrationTest {

    @Test
    public void testMojo() throws MojoExecutionException {
        mavenSandbox.withParentProject("my-jar-project", "jar")
                .withNoChildProject()
                .withGitRepoInParent(AvailableGitTestRepo.WITH_ONE_COMMIT)
                .create();
        MavenProject targetProject = mavenSandbox.getParentProject();
        mojo.dotGitDirectory = new File("/Users/bresai/src/aCard/acardBackend/src/main/java/com/acard/backend/service/impl");
        mojo.execute();
        List<ConfigBean> ret = mojo.ret;

        System.out.println(ret);
    }

}