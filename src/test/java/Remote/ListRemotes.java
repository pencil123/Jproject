package Remote;

/**
 * @author lyzhang
 * @since 2019/4/24 11:23
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Snippet which shows how to iterate remotes, i.e. "git ls-remote"
 *
 * @author dominik.stadler at gmx.at
 */
public class ListRemotes {

  public static void main(String[] args) throws IOException, GitAPIException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try (Repository repository =
                 builder
                         .setGitDir(new File("D:\\codes\\TestGitRepository6614451434305899181\\.git"))
                         .readEnvironment()
                         .findGitDir()
                         .build())  {
      // all refs
      try (Git git = new Git(repository)) {
        Collection<Ref> refs = git.lsRemote().call();
        int i=0;
        for (Ref ref : refs) {
          System.out.println("Ref: " + ref);
          i++;
          if (i > 5 ){
            break;
          }
        }

        // heads only
        refs = git.lsRemote().setHeads(true).call();

        for (Ref ref : refs) {
          System.out.println("Head: " + ref);
          i++;
          if (i > 10 ){
            break;
          }
        }

        // tags only
        refs = git.lsRemote().setTags(true).call();
        for (Ref ref : refs) {
          System.out.println("Remote tag: " + ref);
          i++;
          if (i > 20){
            break;
          }
        }
      }
    }
  }
}