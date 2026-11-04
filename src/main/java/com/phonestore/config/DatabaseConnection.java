package com.phonestore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối JDBC tới cơ sở dữ liệu MySQL (mobile_store_db).
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/mobile_store_db"
            + "?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh"
            + "&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
