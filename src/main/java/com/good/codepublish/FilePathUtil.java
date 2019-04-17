package com.good.codepublish;

import java.io.File;
import java.util.LinkedList;

/**
 * @author lyzhang
 * @since 2019/4/17 13:44
 */
public class FilePathUtil {
  public static void main(String[] args) {
    //
    File parrentFolder = new File("D:\\codes");
      LinkedList<File> paths = new LinkedList<>();
      paths.add(parrentFolder);
    System.out.println(parrentFolder.getName());
      for (File pathsInList:paths){
          LinkedList<File> subFolders = getSubFolders(pathsInList);
          for (File subFolder:subFolders){
              System.out.println(subFolder.getName());
          }
      }
  }
  public static LinkedList<File> getSubFolders(File folderPath){
   // System.out.println(folderPath);
    int fileNum =0,folderNum=0;
    LinkedList<File> list= new LinkedList<File>();
    if(folderPath.exists()){
        File files[] = folderPath.listFiles();
        for (File file2 : files){
            if (file2.isDirectory()){
                list.add(file2);
            }
        }
    }
    return list;
  }

}
