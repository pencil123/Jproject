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

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Simple snippet which shows how to add a file to the index
 *
 * @author dominik.stadler at gmx.at
 */
public class AddFile {

  public static void main(String[] args) throws IOException, GitAPIException {
    final File localPath;
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    // prepare a new test-repository
    try (Repository repository = builder.setGitDir(new File("D:\\codes\\jgit\\.git"))
    .readEnvironment()
    .findGitDir()
    .build()
    ) {
      localPath = repository.getWorkTree();
      System.out.println(localPath.getName());
      try (Git git = new Git(repository)) {
        // create the file
        File myFile = new File(repository.getDirectory().getParent(), "testfile");
        if(!myFile.createNewFile()) {
          throw new IOException("Could not create file " + myFile);
        }

        // run the add-call
        git.add()
                .addFilepattern("testfile")
                .call();

        System.out.println("Added file " + myFile + " to repository at " + repository.getDirectory());
      }
    }

    // clean up here to not keep using more and more disk-space for these samples
    //FileUtils.deleteDirectory(localPath);
  }
}