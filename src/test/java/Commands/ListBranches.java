package Commands;

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

import java.io.File;
import java.io.IOException;
import java.util.List;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;


/**
 * Simple snippet which shows how to list all Branches in a Git repository
 *
 * @author dominik.stadler at gmx.at
 */
public class ListBranches {

  public static void main(String[] args) throws IOException, GitAPIException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try (Repository repository =
                 builder
                         .setGitDir(new File("D:\\codes\\jsh-tally-jobs\\.git"))
                         .readEnvironment()
                         .findGitDir()
                         .build()) {
      System.out.println("Listing local branches:");
      try (Git git = new Git(repository)) {
        List<Ref> call = git.branchList().call();
        for (Ref ref : call) {
          System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }

        System.out.println("Now including remote branches:");

        //所有的分支信息
        call = git.branchList().setListMode(ListMode.ALL).call();
        for (Ref ref : call) {
          System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
      }
    }
  }
}