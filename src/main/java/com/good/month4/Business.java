package com.good.month4;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库调用类
 *
 * @author lyzhang
 * @since 2019/4/4 14:39
 */
public class Business {


  public static void main(String[] args) {
    /*主函数
     * */
    MysqlConn conn = new MysqlConn();
    String sql = "select user_name,password from t_user";
    ResultSet rs = conn.executeQuery(sql);
    while (true) {
      try {
        if (!rs.next()) {
          break;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      try {
        System.out.println("用户名:" + rs.getString("user_name"));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    System.out.println("end");
  }
}
