package com.blogs;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author lyzhang
 * @since 2019/4/25 19:55
 */
public class Application {
  public static void main(String[] args) throws IOException, GitAPIException {
   // ;
/*    gu.getLog();
    gu.getLastTag();
    gu.commitTag();*/
   // gu.branchPull("master");
    LinkedList<File> list = FileUtils.getSubFolders(new File("D:\\git\\jsh-bak"));
    for (File file:list){
      //System.out.println(file);
      GitUtils gu = new GitUtils(file);
      gu.getLastTag();
//      System.out.println(file.getName() + "----" + gu.getLastTag().name);
      gu.branchPull("master");
    }
  }
}
