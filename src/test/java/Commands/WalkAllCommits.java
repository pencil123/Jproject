package Commands;

/**
 * @author lyzhang
 * @since 2019/4/23 16:44
 */
/*
   Copyright 2013, 2014 Dominik Stadler
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;



/**
 * Simple snippet which shows how to use RevWalk to quickly iterate over all available commits,
 * not just the ones on the current branch
 */
public class WalkAllCommits {

  public static void main(String[] args) throws IOException, GitAPIException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try (Repository repository =
                 builder
                         .setGitDir(new File("D:\\codes\\jsh-tally-jobs\\.git"))
                         .readEnvironment()
                         .findGitDir()
                         .build()) {
      try (Git git = new Git(repository)) {
        Iterable<RevCommit> commits = git.log().all().call();
        int count = 0;
        for (RevCommit commit : commits) {
          System.out.println("LogCommit: " + commit);
          count++;
        }
        System.out.println(count);
      }
    }
  }
}