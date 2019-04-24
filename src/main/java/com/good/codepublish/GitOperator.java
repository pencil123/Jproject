package com.good.codepublish;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lyzhang
 * @since 2019/4/22 15:34
 */
public class GitOperator {
  private static Logger logger = LoggerFactory.getLogger(FilePathUtil.class);
  private static final String POM_FILE = "pom.xml";

/*  public static void main(String[] args) {
    String projectFolder = "D:\\codes\\githup\\Jproject2";
    List<String> dstBrach = ["dev" , "master"];
    checkOutToBranch(projectFolder,dstBrach);
  }*/

  public static void checkOutToBranch(String projectFolderPath, List<String> dstBranches)
          throws IOException, GitAPIException {
    logger.info("开始更新工程｛｝", projectFolderPath);
    Git git = Git.open(new File(projectFolderPath));

    Status status = git.status().call();
    if (!status.hasUncommittedChanges()) {
        logger.debug("工程{}没有修改过",projectFolderPath);
        return ;
    }
      Set <String> modifiedFile = status.getModified();

  }
}
