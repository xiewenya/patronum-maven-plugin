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

package com.bresai.expecto.patronum.core.git;

import com.google.common.annotations.VisibleForTesting;
import com.sun.istack.internal.NotNull;
import pl.project13.core.AheadBehind;
import pl.project13.core.GitCommitIdExecutionException;
import pl.project13.core.GitDataProvider;
import pl.project13.core.git.GitDescribeConfig;

import javax.annotation.Nonnull;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NativeGitProvider extends GitDataProvider {

  private NativeGitRunner gitRunner;

  final File dotGitDirectory;

  final long nativeGitTimeoutInMs;

  final File canonical;

  @Nonnull
  public static NativeGitProvider on(@NotNull NativeGitRunner gitRunner) {
    return new NativeGitProvider(gitRunner);
  }

  NativeGitProvider(@NotNull NativeGitRunner gitRunner) {
    super(gitRunner.getLog());
    this.dotGitDirectory = gitRunner.getDotGitDirectory();
    this.nativeGitTimeoutInMs = gitRunner.getNativeGitTimeoutInMs();
    this.canonical = gitRunner.getCanonical();
  }

  @Override
  public void init() throws GitCommitIdExecutionException {
    // noop ...
  }

  @Override
  public String getBuildAuthorName() throws GitCommitIdExecutionException {
    try {
      return gitRunner.runGitCommand(canonical, nativeGitTimeoutInMs, "config --get user.name");
    } catch (NativeGitRunner.NativeCommandException e) {
      if (e.getExitCode() == 1) { // No config file found
        return "";
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getBuildAuthorEmail() throws GitCommitIdExecutionException {
    try {
      return gitRunner.runGitCommand(canonical, nativeGitTimeoutInMs, "config --get user.email");
    } catch (NativeGitRunner.NativeCommandException e) {
      if (e.getExitCode() == 1) { // No config file found
        return "";
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  public void prepareGitToExtractMoreDetailedRepoInformation() throws GitCommitIdExecutionException {
  }

  @Override
  public String getBranchName() throws GitCommitIdExecutionException {
    if (evalCommitIsNotHead()) {
      // git branch --points-at $evaluateOnCommit
      return getBranchForCommitish(canonical);
    } else {
      // git symbolic-ref --short HEAD
      return getBranchForHead(canonical);
    }
  }

  private boolean evalCommitIsNotHead() {
    return (evaluateOnCommit != null) && !evaluateOnCommit.equals("HEAD");
  }

  private String getBranchForHead(File canonical) throws GitCommitIdExecutionException {
    String branch;
    try {
      branch = gitRunner.runGitCommand(canonical, nativeGitTimeoutInMs, "symbolic-ref --short " + evaluateOnCommit);
    } catch (NativeGitRunner.NativeCommandException e) {
      // it seems that git repo is in 'DETACHED HEAD'-State, using Commit-Id as Branch
      String err = e.getStderr();
      if (err != null) {
        boolean noSymbolicRef = err.contains("ref " + evaluateOnCommit + " is not a symbolic ref");
        boolean noSuchRef = err.contains("No such ref: " + evaluateOnCommit);
        if (noSymbolicRef || noSuchRef) {
          branch = getCommitId();
        } else {
          throw new RuntimeException(e);
        }
      } else {
        throw new RuntimeException(e);
      }
    }
    return branch;
  }

  private String getBranchForCommitish(File canonical) throws GitCommitIdExecutionException {
    String branch = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "branch --points-at " + evaluateOnCommit);
    if (branch != null && !branch.isEmpty()) {
      // multiple branches could point to the same commit - return them all...
      branch = Stream.of(branch.split("\n"))
              .map(s -> s.replaceAll("[\\* ]+", ""))
              // --points-at returns detached HEAD as branch, we don't like that
              .filter(s -> !s.startsWith("(HEAD detached at"))
              .collect(Collectors.joining(","));
    } else {
      // it seems that nothing is pointing to the commit, using Commit-Id as Branch
      branch = getCommitId();
    }
    return branch;
  }

  @Override
  public String getGitDescribe() throws GitCommitIdExecutionException {
    final String argumentsForGitDescribe = getArgumentsForGitDescribe(gitDescribe);
    return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "describe" + argumentsForGitDescribe);
  }

  private String getArgumentsForGitDescribe(GitDescribeConfig describeConfig) {
    if (describeConfig == null) {
      return "";
    }

    StringBuilder argumentsForGitDescribe = new StringBuilder();
    boolean hasCommitish = evalCommitIsNotHead();
    if (hasCommitish) {
      argumentsForGitDescribe.append(" " + evaluateOnCommit);
    }

    if (describeConfig.isAlways()) {
      argumentsForGitDescribe.append(" --always");
    }

    final String dirtyMark = describeConfig.getDirty();
    if ((dirtyMark != null) && !dirtyMark.isEmpty()) {
      // we can either have evaluateOnCommit or --dirty flag set
      if (hasCommitish) {
        log.warn("You might use strange arguments since it's unfortunately not supported to have evaluateOnCommit and the --dirty flag for the describe command set at the same time");
      } else {
        argumentsForGitDescribe.append(" --dirty=").append(dirtyMark);
      }
    }

    final String matchOption = describeConfig.getMatch();
    if (matchOption != null && !matchOption.isEmpty()) {
      argumentsForGitDescribe.append(" --match=").append(matchOption);
    }

    argumentsForGitDescribe.append(" --abbrev=").append(describeConfig.getAbbrev());

    if (describeConfig.getTags()) {
      argumentsForGitDescribe.append(" --tags");
    }

    if (describeConfig.getForceLongFormat()) {
      argumentsForGitDescribe.append(" --long");
    }
    return argumentsForGitDescribe.toString();
  }

  @Override
  public String getCommitId() throws GitCommitIdExecutionException {
    boolean evaluateOnCommitIsSet = evalCommitIsNotHead();
    if (evaluateOnCommitIsSet) {
      // if evaluateOnCommit represents a tag we need to perform the rev-parse on the actual commit reference
      // in case evaluateOnCommit is not a reference rev-list will just return the argument given
      // and thus it's always safe(r) to unwrap it
      // however when evaluateOnCommit is not set we don't want to waste calls to the native binary
      String actualCommitId = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "rev-list -n 1 " + evaluateOnCommit);
      return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "rev-parse " + actualCommitId);
    } else {
      return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "rev-parse HEAD");
    }
  }

  @Override
  public String getAbbrevCommitId() throws GitCommitIdExecutionException {
    // we could run: tryToRunGitCommand(canonical, "rev-parse --short="+abbrevLength+" HEAD");
    // but minimum length for --short is 4, our abbrevLength could be 2
    String commitId = getCommitId();
    String abbrevCommitId = "";

    if (commitId != null && !commitId.isEmpty()) {
      abbrevCommitId = commitId.substring(0, abbrevLength);
    }

    return abbrevCommitId;
  }

  @Override
  public boolean isDirty() throws GitCommitIdExecutionException {
    return !gitRunner.tryCheckEmptyRunGitCommand(canonical, nativeGitTimeoutInMs, "status -s");
  }

  @Override
  public String getCommitAuthorName() throws GitCommitIdExecutionException {
    return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "log -1 --pretty=format:%an " + evaluateOnCommit);
  }

  @Override
  public String getCommitAuthorEmail() throws GitCommitIdExecutionException {
    return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "log -1 --pretty=format:%ae " + evaluateOnCommit);
  }

  @Override
  public String getCommitMessageFull() throws GitCommitIdExecutionException {
    return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "log -1 --pretty=format:%B " + evaluateOnCommit);
  }

  @Override
  public String getCommitMessageShort() throws GitCommitIdExecutionException {
    return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "log -1 --pretty=format:%s " + evaluateOnCommit);
  }

  @Override
  public String getCommitTime() throws GitCommitIdExecutionException {
    String value = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "log -1 --pretty=format:%ct " + evaluateOnCommit);
    SimpleDateFormat smf = getSimpleDateFormatWithTimeZone();
    return smf.format(Long.parseLong(value) * 1000L);
  }

  @Override
  public String getTags() throws GitCommitIdExecutionException {
    final String result = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "tag --contains " + evaluateOnCommit);
    return result.replace('\n', ',');
  }

  @Override
  public String getRemoteOriginUrl() throws GitCommitIdExecutionException {
    return getOriginRemote(canonical, nativeGitTimeoutInMs);
  }

  @Override
  public String getClosestTagName() throws GitCommitIdExecutionException {
    try {
      StringBuilder argumentsForGitDescribe = new StringBuilder();
      argumentsForGitDescribe.append("describe " + evaluateOnCommit + " --abbrev=0");
      if (gitDescribe != null) {
        if (gitDescribe.getTags()) {
          argumentsForGitDescribe.append(" --tags");
        }

        final String matchOption = gitDescribe.getMatch();
        if (matchOption != null && !matchOption.isEmpty()) {
          argumentsForGitDescribe.append(" --match=").append(matchOption);
        }
      }
      return gitRunner.runGitCommand(canonical, nativeGitTimeoutInMs, argumentsForGitDescribe.toString());
    } catch (NativeGitRunner.NativeCommandException ignore) {
      // could not find any tags to describe
    }
    return "";
  }

  @Override
  public String getClosestTagCommitCount() throws GitCommitIdExecutionException {
    String closestTagName = getClosestTagName();
    if (closestTagName != null && !closestTagName.trim().isEmpty()) {
      return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "rev-list " + closestTagName + ".." + evaluateOnCommit + " --count");
    }
    return "";
  }

  @Override
  public String getTotalCommitCount() throws GitCommitIdExecutionException {
    return gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "rev-list " + evaluateOnCommit + " --count");
  }

  @Override
  public void finalCleanUp() throws GitCommitIdExecutionException {
  }

  private String getOriginRemote(File directory, long nativeGitTimeoutInMs) throws GitCommitIdExecutionException {
    try {
      String remoteUrl = gitRunner.runGitCommand(directory, nativeGitTimeoutInMs, "ls-remote --get-url");

      return stripCredentialsFromOriginUrl(remoteUrl);
    } catch (NativeGitRunner.NativeCommandException ignore) {
      // No remote configured to list refs from
    }
    return null;
  }


  @Override
  public AheadBehind getAheadBehind() throws GitCommitIdExecutionException {
    try {
      Optional<String> remoteBranch = remoteBranch();
      if (!remoteBranch.isPresent()) {
        return AheadBehind.NO_REMOTE;
      }
      if (!offline) {
        fetch(remoteBranch.get());
      }
      String localBranchName = getBranchName();
      String ahead = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "rev-list --right-only --count " + remoteBranch.get() + "..." + localBranchName);
      String behind = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "rev-list --left-only --count " + remoteBranch.get() + "..." + localBranchName);
      return AheadBehind.of(ahead, behind);
    } catch (Exception e) {
      throw new GitCommitIdExecutionException("Failed to read ahead behind count: " + e.getMessage(), e);
    }
  }

  private Optional<String> remoteBranch() {
    try {
      String remoteRef = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "symbolic-ref -q " + evaluateOnCommit);
      if (remoteRef == null || remoteRef.isEmpty()) {
        log.debug("Could not find ref for: " + evaluateOnCommit);
        return Optional.empty();
      }
      String remoteBranch = gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "for-each-ref --format=%(upstream:short) " + remoteRef);
      return Optional.ofNullable(remoteBranch.isEmpty() ? null : remoteBranch);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
  
  private void fetch(String remoteBranch) {
    try {
        gitRunner.runQuietGitCommand(canonical, nativeGitTimeoutInMs, "fetch " + remoteBranch.replaceFirst("/", " "));
    } catch (Exception e) {
      log.error("Failed to execute fetch", e);
    }
  }
  
  @VisibleForTesting
  public void setEvaluateOnCommit(String evaluateOnCommit) {
    this.evaluateOnCommit = evaluateOnCommit;
  }
}
