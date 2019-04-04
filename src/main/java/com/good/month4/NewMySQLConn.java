package com.good.month4;
import com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lyzhang
 * @since 2019/4/4 15:58
 */
public class NewMySQLConn {

  public static Connection getConn() throws Exception {
      Class.forName("com.mysql.jdbc.Driver");
      Connection conn= DriverManager.getConnection("jdbc:mysql://12.168.3.75:3306/test_lyzhang", "dba", "dba?ylh79ak");
      return conn;
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

  public static void main(String[] args){
      try{
          getConn();
          System.out.println("MySQL connection sucession;");
        }catch(Exception e){
          e.printStackTrace();
          System.out.println("MySQL connection false");
        }
  }
}
