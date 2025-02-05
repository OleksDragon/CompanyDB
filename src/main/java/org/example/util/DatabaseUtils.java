package org.example.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtils {
    // Метод для отримання структури таблиці
    public static void printTableStructure(String tableName) {
        String sql = "SHOW COLUMNS FROM " + tableName;
        try (Connection connection = DataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            System.out.println("\n======================================================");
            System.out.println("Структура таблиці: " + tableName);
            System.out.printf("%-20s %-20s %-20s\n", "Колонка", "Тип", "Обмеження");
            System.out.println("------------------------------------------------------");

            while (rs.next()) {
                String column = rs.getString("Field");
                String type = rs.getString("Type");
                String isNullable = rs.getString("Null").equals("NO") ? "NOT NULL" : "NULL";
                String key = rs.getString("Key").equals("PRI") ? "PRIMARY KEY" :
                        rs.getString("Key").equals("UNI") ? "UNIQUE" :
                                rs.getString("Key").equals("MUL") ? "INDEX" : "";
                String extra = rs.getString("Extra");

                String constraints = (key.isEmpty() ? "" : key + " ") + (extra.isEmpty() ? "" : extra);
                System.out.printf("%-20s %-20s %-20s\n", column, type, constraints);
            }
            System.out.println();
        } catch (SQLException e) {
            System.out.println("Помилка отримання структури таблиці " + tableName + ": " + e.getMessage());
        }
    }

    // Метод для отримання кількості рядків у таблиці
    public static void printRowCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection connection = DataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Кількість рядків у таблиці " + tableName + ": " + count);
            }
        } catch (SQLException e) {
            System.out.println("Помилка отримання кількості рядків у таблиці " + tableName + ": " + e.getMessage());
        }
    }

    public static void printDatabaseInfo() {
        printTableStructure("departments");
        printTableStructure("employees");
        printRowCount("departments");
        printRowCount("employees");
    }
}
