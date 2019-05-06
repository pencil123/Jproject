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

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.hooks.CommitMsgHook;
import org.eclipse.jgit.hooks.Hooks;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static java.lang.System.out;


/**
 * Simple snippet which shows how to commit a file
 *
 * @author dominik.stadler at gmx.at
 */
public class CommitFileHook {

  public static void main(String[] args) throws IOException, GitAPIException {
    final File localPath;
    FileRepositoryBuilder builder = new FileRepositoryBuilder();


    // prepare a new test-repository
    try (Repository repository = builder.setGitDir(new File("D:\\codes\\jsh-tally-jobs\\.git"))
            .readEnvironment()
            .findGitDir()
            .build()) {
      localPath = repository.getWorkTree();

      try (Git git = new Git(repository)) {
        // create the file
        File myFile = new File(repository.getDirectory().getParent(), "testfile");
        if(!myFile.createNewFile()) {
          throw new IOException("Could not create file " + myFile);
        }

        // run the add
        git.add()
                .addFilepattern("testfile")
                .call();

        // and then commit the changes
/*        CommitMsgHook hook = new CommitMsgHook();*/
        CommitMsgHook hook = Hooks.commitMsg(repository,out);

/*        out.println(hook.getHookName());
        out.println(hook.toString());
        //String he = hook.call();

        //out.println(he);
        out.println(hook.getRepository());
        out.println(CommitMsgHook.NAME);*/
        RevCommit revCommit = git.commit()
                .setMessage("Added testfile")
                .setInsertChangeId(true)
                //.setHookOutputStream(new PrintStream(out))
                .call();
        out.println(revCommit.getFullMessage());
        out.println("Committed file " + myFile + " to repository at " + repository.getDirectory());
      }
    }
    // clean up here to not keep using more and more disk-space for these samples
//    FileUtils.deleteDirectory(localPath);
  }
}