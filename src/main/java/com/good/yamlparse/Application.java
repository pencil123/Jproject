package com.good.yamlparse;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lyzhang
 * @since 2019/4/9 16:35
 */
public class Application {
  public static void main(String[] args) {
    //
      Yaml yaml = new Yaml();
      String yamlfile = "demo-cloud-config-server-dev.yml";
      File f = new File(yamlfile);
      try {
          Map result = (Map) yaml.load(new FileInputStream(f));
      /*          Set set = result.keySet();
      System.out.println("map实例中所有的key为:"+set);*/
      result.forEach(
          (k, v) -> {
            System.out.println(k + ":" + v);
          });

      } catch (FileNotFoundException e) {
              e.printStackTrace();
      }

  }
}
