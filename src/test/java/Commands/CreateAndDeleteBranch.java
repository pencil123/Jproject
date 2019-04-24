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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;


/**
 * Simple snippet which shows how to create and delete branches
 *
 * @author dominik.stadler at gmx.at
 */
public class CreateAndDeleteBranch {

  public static void main(String[] args) throws IOException, GitAPIException {
    // prepare test-repository
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try (Repository repository =
                 builder
                         .setGitDir(new File("D:\\codes\\jsh-tally-jobs\\.git"))
                         .readEnvironment()
                         .findGitDir()
                         .build()) {
      try (Git git = new Git(repository)) {
        List<Ref> call = git.branchList().call();
        for (Ref ref : call) {
          System.out.println("Branch-Before: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }

        // make sure the branch is not there
        List<Ref> refs = git.branchList().call();
        for(Ref ref : refs) {
          System.out.println("Had branch: " + ref.getName());
          if(ref.getName().equals("refs/heads/testbranch")) {
            System.out.println("Removing branch before");
            git.branchDelete()
                    .setBranchNames("testbranch")
                    .setForce(true)
                    .call();

            break;
          }
        }

        // run the add-call
        git.branchCreate()
                .setName("testbranch")
                .call();

        call = git.branchList().call();
        for (Ref ref : call) {
          System.out.println("Branch-Created: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }

        // run the delete-call
        git.branchDelete()
                .setBranchNames("testbranch")
                .call();

        call = git.branchList().call();
        for (Ref ref : call) {
          System.out.println("Branch-After: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }

        // run the add-call with a given starting point
        git.branchCreate()
                .setName("testbranch")
                .setStartPoint("d52a1031cd359a5941d0e047aa7ab82053f7f7c3")
                .call();

        call = git.branchList().call();
        for (Ref ref : call) {
          System.out.println("Branch-Created with starting point: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
      }
    }
  }
}