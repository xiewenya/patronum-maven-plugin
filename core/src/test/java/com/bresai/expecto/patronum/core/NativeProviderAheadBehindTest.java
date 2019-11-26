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

package com.bresai.expecto.patronum.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import pl.project13.core.NativeGitProvider;

@Slf4j
public class NativeProviderAheadBehindTest extends AheadBehindTest<NativeGitProvider> {

  @Override
  protected NativeGitProvider gitProvider() {
    return NativeGitProvider.on(localRepository.getRoot(), 1000L, null);
  }

  @Override
  protected void extraSetup() {
    gitProvider.setEvaluateOnCommit("HEAD");
  }

  @Test
  public void testNativeGitProvider() throws Exception {
    createLocalCommit();
    localRepositoryGit.branchCreate().setName("dev").call();

    log.info(localRepositoryGit.branchList().call().toString());

    log.info(gitProvider.getBranchName());
    log.info(gitProvider.getCommitId());
  }
}
