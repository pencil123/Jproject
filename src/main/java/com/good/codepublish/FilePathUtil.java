package com.good.codepublish;

import java.io.File;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lyzhang
 * @since 2019/4/17 13:44
 */
public class FilePathUtil {
  private static Logger logger = LoggerFactory.getLogger(FilePathUtil.class);

  public static void main(String[] args) {
    //
    File parrentFolder = new File("D:\\git\\jsh");
    LinkedList<File> paths = new LinkedList<>();
    paths.add(parrentFolder);
    logger.debug(parrentFolder.getName());
    for (File pathsInList:paths) {
      LinkedList<File> subFolders = getSubFolders(pathsInList);
      for (File subFolder:subFolders) {
        logger.info(subFolder.getName());
      }
    }
  }

/**
 *  遍历文件子目录方法
*/
  public  static LinkedList getSubFolders(File folderPath) {
    LinkedList<File> list = new LinkedList<>();
    if (folderPath.exists()) {
      File[] files = folderPath.listFiles();
      for (File file2 : files) {
        if ( isGitFolder(file2)) {
          list.add(file2);
        }
      }
    }
    return list;
  }

/**
 *判断目录下是否有.git文件夹
  */
  public static boolean isGitFolder(File folderPath) {
     if (folderPath.exists()) {
          File gitFolder = new File(folderPath.getParent() + "/" + folderPath.getName() + "/.git");
          if (gitFolder.isDirectory()) {
              return true;
          }else {
              return false;
          }
     }else {
          return false;
     }
  }

}
