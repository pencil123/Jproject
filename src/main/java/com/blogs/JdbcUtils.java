package com.blogs;

import javax.annotation.Resource;
import java.sql.*;

public class JdbcUtils {
    @Resource
    public Connection connection;
    public ResultSet rs;
    public PreparedStatement prst;

    /**'
     * 构造函数，连接数据库
     * @throws SQLException
     */
    JdbcUtils() throws SQLException {
        connection =
                DriverManager.getConnection(
                        "jdbc:mysql://12.168.3.75:3306/test_lyzhang", "lyzhang", "lyzhang");
    }

    /**
     * 执行更新|插入|删除的SQL语句
     * @param stringSQL
     * @return
     * @throws SQLException
     */
    public boolean executeSql(String stringSQL) throws SQLException {
        prst = connection.prepareStatement(stringSQL);
        prst.executeUpdate();
        return true;
    }

    /**
     * 执行查询数据的语句
     * @param sql
     * @return
     * @throws SQLException
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        //System.out.println(connection.isClosed());
        PreparedStatement prst = connection.prepareStatement(sql);
        rs = prst.executeQuery();
        return rs;
    }

    /**
     * 执行修改表结构的SQL语句
     * @param sql
     * @return
     * @throws SQLException
     */
    public boolean execute(String sql) throws SQLException {
        prst = connection.prepareStatement(sql);
        prst.executeUpdate();
        return true;
    }

    /**
     * 返回SQL查询语句执行结果匹配的条数
     * @param sql
     * @return
     * @throws SQLException
     */
    public int count(String sql) throws SQLException {
        prst = connection.prepareStatement(sql);
        ResultSet rs = prst.executeQuery();
        rs.last();
        return rs.getRow();
    }

    /**
     * 判断查询结果中，是否有指定的列
     * @param rs
     * @param columnNmae
     * @return
     */
    public boolean isExistColumn(ResultSet rs, String columnNmae) {
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
