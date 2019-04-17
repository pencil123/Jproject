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
    File parrentFolder = new File("D:\\codes");
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

  public  static LinkedList getSubFolders(File folderPath) {

    LinkedList<File> list = new LinkedList<>();
    if (folderPath.exists()) {
      File[] files = folderPath.listFiles();
      for (File file2 : files) {
        if (file2.isDirectory()) {
          list.add(file2);
        }
      }
    }
    return list;
  }

}
