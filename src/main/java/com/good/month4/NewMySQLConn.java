package com.good.month4;
import com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;

import java.io.*;
import org.yaml.snakeyaml.Yaml;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.sql.DriverManager.*;

/**
 * @author lyzhang
 * @since 2019/4/4 15:58
 */
public class NewMySQLConn {

    public static Map readFile() throws FileNotFoundException{
        Yaml yaml = new Yaml();
        //String configfile = ".\\src\\main\\java\\com\\good\\month4\\config.yaml";
        String configfile = "config.yaml";
        File f = new File(configfile);
        Map result = (Map) yaml.load(new FileInputStream(f));
        return (Map) result.get("MySQL");
    }

  public static Connection getConn() throws Exception {
      Map connConfig = readFile();
      Class.forName("com.mysql.jdbc.Driver");
      try (Connection conn = (Connection) getConnection((String) connConfig.get("url"), (String) connConfig.get("user"),(String) connConfig.get("password"))) {
          return conn;
      }
  }

  public static void closeConn(Connection conn ) throws Exception {
    if(conn != null) {
        conn.close();
        conn=null;
    }
  }

  public static void closeRS(ResultSet rs) throws Exception{
      if(rs != null){
          rs.close();
          rs=null;
      }
  }

  public static void main(String[] args) {
      try{
          getConn();
          System.out.println("MySQL connection sucession;");
        }catch(Exception e){
          e.printStackTrace();
          System.out.println("MySQL connection false");
        }
  }
}
