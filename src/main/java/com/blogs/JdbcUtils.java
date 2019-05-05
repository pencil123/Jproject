package com.blogs;

import javax.annotation.Resource;
import java.sql.*;

public class JdbcUtils {
    @Resource
    public Connection connection;
    public ResultSet rs;
    public PreparedStatement prst;


    JdbcUtils() throws SQLException {
        connection =
                DriverManager.getConnection(
                        "jdbc:mysql://12.168.3.75:3306/test_lyzhang", "lyzhang", "lyzhang");
    }

    public boolean executeSql (String stringSQL) throws SQLException {
        prst = connection.prepareStatement(stringSQL);
        prst.executeUpdate();
        return true;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        //System.out.println(connection.isClosed());
        PreparedStatement prst = connection.prepareStatement(sql);
        rs = prst.executeQuery();
        return rs;
    }

    public boolean execute(String sql) throws SQLException{
        prst = connection.prepareStatement(sql);
        prst.executeUpdate();
        return true;
    }

    public int count(String sql) throws SQLException {
        prst = connection.prepareStatement(sql);
        ResultSet rs = prst.executeQuery();
        rs.last();
        return rs.getRow();
    }

    public boolean isExistColumn (ResultSet rs,String columnNmae) {
        try {
            if (rs.findColumn(columnNmae) > 0) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }
}
