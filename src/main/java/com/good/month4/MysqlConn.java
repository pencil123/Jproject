package com.good.month4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.annotation.Resource;

/**数据库类
 * @author lyzhang
 * @since 2019/4/4 12:20
 */
public class MysqlConn {
  @Resource public static Connection conection;
  public static ResultSet rs;
  public static PreparedStatement prs;

  public static Connection getConnect() {
    try {
      conection =
          DriverManager.getConnection(
              "jdbc:mysql://12.168.3.75:3306/test_lyzhang", "dba", "dba?ylh79ak");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return conection;
  }

  public ResultSet executeQuery(String sql) {
    getConnect();
    try {
      PreparedStatement prst = conection.prepareStatement(sql);
      rs = prst.executeQuery();
    } catch (Exception e) {
      e.printStackTrace();
    }
    close();
    return rs;
  }

  public ResultSet executeUpdate(String sql) {
    getConnect();
    try {
      PreparedStatement prst = conection.prepareStatement(sql);
      rs = prst.executeQuery();
    } catch (Exception e) {
      e.printStackTrace();
    }
    close();
    return rs;
  }

  public void close() {
    try {
      if (rs != null) { // 当ResultSet对象的实例rs不为空时
        rs.close(); // 关闭ResultSet对象
      }
      if (prs != null) { // 当Statement对象的实例stmt不为空时
        prs.close(); // 关闭Statement对象
      }
      if (conection != null) { // 当Connection对象的实例conn不为空时
        conection.close(); // 关闭Connection对象
      }
    } catch (Exception e) {
      e.printStackTrace(); // 输出异常信息
    }
  }
}
