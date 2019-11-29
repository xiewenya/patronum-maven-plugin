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

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.file.JavaWalker;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.build.incremental.BuildContext;
import pl.project13.core.GitCommitIdExecutionException;
import pl.project13.core.log.LoggerBridge;
import pl.project13.maven.git.GitDirLocator;
import pl.project13.maven.log.MavenLoggerBridge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Puts git build-time information into property files or maven's properties.
 *
 * @since 1.0
 */
@Mojo(name = "revision", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class PatronumMojo extends AbstractMojo {
  private static final String CONTEXT_KEY = PatronumMojo.class.getName() + ".properties";

  /**
   * The Maven Project.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  MavenProject project;

  /**
   * The list of projects in the reactor.
   */
  @Parameter(defaultValue = "${reactorProjects}", readonly = true)
  List<MavenProject> reactorProjects;

  /**
   * The Maven Session Object.
   */
  @Parameter(property = "session", required = true, readonly = true)
  MavenSession session;

  /**
   * The Maven settings.
   */
  @Parameter(property = "settings", required = true, readonly = true)
  Settings settings;

  /**
   * Set this to {@code 'true'} to print more info while scanning for paths.
   * It will make git-commit-id "eat its own dog food" :-)
   */
  @Parameter(defaultValue = "false")
  boolean verbose;


  /**
   * The root directory of the repository we want to check.
   */
  @Parameter(defaultValue = "${project.basedir}/.git")
  File dotGitDirectory;


  /**
   * Set this to {@code 'true'} to use native Git executable to fetch information about the repository.
   * It is in most cases faster but requires a git executable to be installed in system.
   * By default the plugin will use jGit implementation as a source of information about the repository.
   *
   * @since 2.1.9
   */
  @Parameter(defaultValue = "false")
  boolean useNativeGit;

  /**
   * Option to be used in command-line to override the value of {@code 'useNativeGit'} specified in
   * the pom.xml, or its default value if it's not set explicitly.
   *
   *  NOTE / WARNING:
   *  Do *NOT* set this property inside the configuration of your plugin.
   *  Please read https://github.com/git-commit-id/maven-git-commit-id-plugin/issues/315
   *  to find out why.
   *
   * @since 3.0.2
   */
  @Parameter(property = "maven.gitcommitid.nativegit", defaultValue = "false")
  boolean useNativeGitViaCommandLine;



  /**
   * Allow to tell the plugin what commit should be used as reference to generate the properties from.
   * By default this property is simply set to <p>HEAD</p> which should reference to the latest commit in your repository.
   *
   * In general this property can be set to something generic like <p>HEAD^1</p> or point to a branch or tag-name.
   * To support any kind or use-case this configuration can also be set to an entire commit-hash or it's abbreviated version.
   *
   * A use-case for this feature can be found in https://github.com/git-commit-id/maven-git-commit-id-plugin/issues/338.
   *
   * Please note that for security purposes not all references might be allowed as configuration.
   * If you have a specific use-case that is currently not white listed feel free to file an issue.
   * @since 2.2.4
   */
  @Parameter(defaultValue = "HEAD")
  String evaluateOnCommit;
  protected static final Pattern allowedCharactersForEvaluateOnCommit = Pattern.compile("[a-zA-Z0-9\\_\\-\\^\\/\\.]+");

  /**
   * Allow to specify a timeout (in milliseconds) for fetching information with the native Git executable.
   * Note that {@code useNativeGit} needs to be set to {@code 'true'} to use native Git executable.
   * @since 3.0.0
   */
  @Parameter(defaultValue = "30000")
  long nativeGitTimeoutInMs;

  /**
   * Use branch name from build environment. Set to {@code 'false'} to use JGit/GIT to get current branch name.
   * Useful when using the JGitflow maven plugin.
   * Note: If not using "Check out to specific local branch' and setting this to false may result in getting
   * detached head state and therefore a commit id as branch name.
   * @since 3.0.0
   */
  @Parameter(defaultValue = "true")
  boolean useBranchNameFromBuildEnvironment;


  /**
   * Injected {@link BuildContext} to recognize incremental builds.
   */
  @Component
  private BuildContext buildContext;

  /**
   * Charset to read-write project sources.
   */
  private Charset sourceCharset = StandardCharsets.UTF_8;

  @Nonnull
  private final LoggerBridge log = new MavenLoggerBridge(this, false);

  List<ConfigBean> ret;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      // Set the verbose setting: now it should be correctly loaded from maven.
      log.setVerbose(verbose);

      // read source encoding from project properties for those who still doesn't use UTF-8
      String sourceEncoding = StandardCharsets.UTF_8.name();

      dotGitDirectory = lookupGitDirectory();

      if (dotGitDirectory != null) {
        log.info("dotGitDirectory {}", dotGitDirectory.getAbsolutePath());
      } else {
        log.info("dotGitDirectory is null, aborting execution!");
        return;
      }

      if ((evaluateOnCommit == null) || !allowedCharactersForEvaluateOnCommit.matcher(evaluateOnCommit).matches()) {
        log.error("suspicious argument for evaluateOnCommit, aborting execution!");
        return;
      }

      JavaWalker javaWalker = new JavaWalker(log);
      ret = javaWalker.walkThroughPath(dotGitDirectory);

    } catch (GitCommitIdExecutionException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  @Nullable
  private Properties getContextProperties(MavenProject project) {
    Object stored = project.getContextValue(CONTEXT_KEY);
    if (stored instanceof Properties) {
      return (Properties)stored;
    }
    return null;
  }

  /**
   * Find the git directory of the currently used project.
   * If it's not already specified, this method will try to find it.
   *
   * @return the File representation of the .git directory
   */
  private File lookupGitDirectory() throws GitCommitIdExecutionException {
    return new GitDirLocator(project, reactorProjects).lookupGitDirectory(dotGitDirectory);
  }
}
