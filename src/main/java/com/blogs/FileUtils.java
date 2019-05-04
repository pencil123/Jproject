package com.blogs;
import java.io.File;
import java.util.LinkedList;

import ch.qos.logback.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jdk.internal.org.objectweb.asm.commons.GeneratorAdapter.AND;

/**
 * @author lyzhang
 * @since 2019/4/17 13:44
 */
public class FileUtils {
  private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

  public static void main(String[] args) {
    //
    File parrentFolder = new File("D:\\git\\jsh");
    LinkedList<File> paths = new LinkedList<>();
    paths.add(parrentFolder);
    logger.debug(parrentFolder.getName());
    for (File pathsInList : paths) {
      LinkedList<File> subFolders = getSubFolders(pathsInList);
/*      for (File subFolder : subFolders) {
        logger.info(subFolder.getName());
      }*/
    }
  }

  /** 遍历文件子文件夹方法 */
  public static LinkedList getSubFolders(File folderPath) {
    LinkedList<File> list = new LinkedList<>();
    if (folderPath.exists()) {
      File[] files = folderPath.listFiles();
      for (File file2 : files){
        if (file2.isDirectory() && !file2.getName().equals(".idea")) {
          //logger.info(file2.getName());
          list.add(file2);
        }
      }
    }
    return list;
  }

  public static Boolean isMaven(File folderPath) {
    File pomFile = new File(folderPath.getAbsolutePath() +  System.getProperty("file.separator") + "pom.xml");
    if (pomFile.exists()) {
      return true;
    } else {
      return false;
    }
  }
}