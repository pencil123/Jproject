package com.blogs;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class Utils {

  public static void main(String[] args) throws FileNotFoundException {
    Utils.frontProjects();
  }

  public static String versionAddOne(String version) {
    String[] versionArray = version.split("\\.");
    String newVersion = null;
    if (versionArray.length >= 3) {
      int versionInt = Integer.parseInt(versionArray[2]);
      versionArray[2] = String.valueOf(versionInt + 1);
      newVersion = String.join(".", versionArray);
    }
    return newVersion;
  }

  /**
   * 从配置文件中，获取前段的工程
   *
   * @return
   * @throws FileNotFoundException
   */
  public static List<String> frontProjects() throws FileNotFoundException {
    Yaml yaml = new Yaml();
    List<String> projects;
    File configFile = new File("config.yaml");
    Map result = (Map) yaml.load(new FileInputStream(configFile));
    projects = (List<String>) result.get("frontProjects");
    return projects;
  }

  /**
   * 从配置文件中,获取要发版的工程
   * @return
   * @throws FileNotFoundException
   */
  public static List<String> publishProjects() throws FileNotFoundException {
    Yaml yaml = new Yaml();
    List<String> projects;
    File configFile = new File("config.yaml");
    Map result = (Map) yaml.load(new FileInputStream(configFile));
    projects = (List<String>) result.get("publishProjects");
    return projects;
  }
}
