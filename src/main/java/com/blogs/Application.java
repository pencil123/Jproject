package com.blogs;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author lyzhang
 * @since 2019/4/25 19:55
 */
public class Application {
  public static void main(String[] args) throws IOException, GitAPIException, IOException, SAXException, ParserConfigurationException {

    LinkedList<File> list = FileUtils.getSubFolders(new File("D:\\git\\jsh-bak"));
    //File file = new File("D:\\jsh\\jsh\\jsh-service-log-provider");

    for (File file:list) {

      File pomFile = new File(file.getAbsolutePath() +  System.getProperty("file.separator") + "pom.xml");
      if (pomFile.exists()) {
        PomUtils obj = new PomUtils(pomFile);
        System.out.println(file.getName());
        System.out.println(file.getName() + " ---"  +obj.getDependency());
      } else {
        System.out.println(file.getName() + " is not maven project");
      }
      break;
    }
/*      GitUtils gu = new GitUtils(file);
      System.out.println("Project 获取最新的Tag 信息");
      System.out.println(file.getName() + "----" + gu.getLastTag().name);


      *//*System.out.println("DEV分支从远程拉去代码并合并");
      System.out.println(gu.branchPull("dev"));
      System.out.println("Master分支从远程拉去代码并合并");
      System.out.println(gu.branchPull("master"));*//*


      System.out.println("将dev分支的代码合并到master分支");
      System.out.println(gu.mergeBranch("dev","master"));
      System.out.println("将master分支的代码合并到dev分支");
      System.out.println(gu.mergeBranch("master","dev"));
    }*/



  }
}
