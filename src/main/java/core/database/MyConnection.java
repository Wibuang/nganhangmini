/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Mở kết nối tới SQL Server. Mọi DAO đều gọi MyConnection.getConnection().
 * Chạy riêng file này (Shift+F6) để kiểm tra kết nối.
 */
public class MyConnection {
    private static final String SERVER   = "localhost";
    private static final String DATABASE = "QLNganHang";
    private static final String USER     = "sa";
    private static final String PASSWORD = "1234";   // đổi theo máy của nhóm

    private static final String URL = "jdbc:sqlserver://" + SERVER + ":1433;"
            + "databaseName=" + DATABASE + ";"
            + "encrypt=true;trustServerCertificate=true;";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Không thể kết nối CSDL!");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println(conn != null ? "Kết nối thành công!" : "Kết nối thất bại!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


