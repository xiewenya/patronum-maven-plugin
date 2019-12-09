/*
 * This file is part of git-commit-id-plugin by Konrad 'ktoso' Malawski <konrad.malawski@java.pl>
 *
 * git-commit-id-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * git-commit-id-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with git-commit-id-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bresai.expecto.patronum.maven.git;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Before;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class GitIntegrationTest {

  private static final String SANDBOX_DIR = "target" + File.separator + "sandbox" + File.separator;
  protected static final String evaluateOnCommit = "HEAD";

  /**
   * Sandbox directory with unique name for current test.
   */
  private String currSandbox;

  protected PatronumMojo mojo;
  protected FileSystemMavenSandbox mavenSandbox;

  @Before
  public void setUp() throws Exception {
    // generate unique sandbox for this test
    File sandbox;
    do {
      currSandbox = SANDBOX_DIR + "sandbox" + Integer.toString(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
      sandbox = new File(currSandbox);
    } while (sandbox.exists());

    mavenSandbox = new FileSystemMavenSandbox(currSandbox);
    mojo = new PatronumMojo();
    initializeMojoWithDefaults(mojo);
  }

  @After
  public void tearDown() throws Exception {
    final boolean keep = mavenSandbox != null && mavenSandbox.isKeepSandboxWhenFinishedTest();

    mojo = null;
    mavenSandbox = null;

    final File sandbox = new File(currSandbox);
    try {
      if (sandbox.exists() && !keep) {
        FileUtils.deleteDirectory(sandbox);
      }
    } catch (IOException e) {
      System.out.println("Unable to delete sandbox. Scheduling deleteOnExit: " + currSandbox);
      sandbox.deleteOnExit();
    }
  }

  protected Git git(String dir) throws IOException, InterruptedException {
    return Git.open(dotGitDir(Optional.of(dir)));
  }

  protected Git git() throws IOException, InterruptedException {
    return Git.open(dotGitDir(projectDir()));
  }

  protected Optional<String> projectDir() {
    return Optional.empty();
  }

  @Nonnull
  protected File dotGitDir(@Nonnull Optional<String> projectDir) {
    if (projectDir.isPresent()) {
      return new File(currSandbox + File.separator + projectDir.get() + File.separator + ".git");
    } else {
      return new File(currSandbox + File.separator + ".git");
    }
  }

  public static void initializeMojoWithDefaults(PatronumMojo mojo) {
    mojo.verbose = false;
    mojo.evaluateOnCommit = evaluateOnCommit;
    mojo.nativeGitTimeoutInMs = (30 * 1000);
    mojo.session = mockSession();
    mojo.settings = mockSettings();
  }


  private static MavenSession mockSession() {
    MavenSession session = mock(MavenSession.class);
    when(session.getUserProperties()).thenReturn(new Properties());
    when(session.getSystemProperties()).thenReturn(new Properties());
    return session;
  }

  private static Settings mockSettings() {
    Settings settings = mock(Settings.class);
    when(settings.isOffline()).thenReturn(false);
    return settings;
  }

  private static List<MavenProject> getReactorProjects(@Nonnull MavenProject project) {
    List<MavenProject> reactorProjects = new ArrayList<>();
    MavenProject mavenProject = project;
    while (mavenProject != null) {
      reactorProjects.add(mavenProject);
      mavenProject = mavenProject.getParent();
    }
    return reactorProjects;
  }

}
