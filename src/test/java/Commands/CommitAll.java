package Commands;

/**
 * @author lyzhang
 * @since 2019/4/23 15:59
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;


/**
 * Simple snippet which shows how to commit all files
 *
 * @author dominik.stadler@gmx.at
 */
public class CommitAll {

  public static void main(String[] args) throws IOException, GitAPIException {
    final File localPath;
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    // prepare a new test-repository
    try (Repository repository = builder.setGitDir(new File("D:\\codes\\jgit\\.git"))
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

        // Stage all files in the repo including new files
        git.add().addFilepattern(".").call();

        // and then commit the changes.
        git.commit()
                .setMessage("Commit all changes including additions")
                .call();

        try(PrintWriter writer = new PrintWriter(myFile)) {
          writer.append("Hello, world!");
        }

        // Stage all changed files, omitting new files, and commit with one command
        git.commit()
                .setAll(true)
                .setMessage("Commit changes to all files")
                .call();


        System.out.println("Committed all changes to repository at " + repository.getDirectory());
      }
    }

    // clean up here to not keep using more and more disk-space for these samples
//    FileUtils.deleteDirectory(localPath);
  }
}