package org.example.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class DatabaseBackup {
    // Метод для експорту таблиці у CSV
    private static void exportTableToCSV(String tableName, String filePath) {
        String sql = "SELECT * FROM " + tableName;

        // Створюємо папку backup, якщо її немає
        File backupDir = new File("backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        try (Connection connection = DataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql);
             FileWriter csvWriter = new FileWriter(filePath)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Записуємо заголовки колонок
            for (int i = 1; i <= columnCount; i++) {
                csvWriter.append(metaData.getColumnName(i));
                if (i < columnCount) csvWriter.append(",");
            }
            csvWriter.append("\n");

            // Записуємо рядки
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    csvWriter.append(rs.getString(i));
                    if (i < columnCount) csvWriter.append(",");
                }
                csvWriter.append("\n");
            }

            System.out.println("Експортовано таблицю " + tableName + " у файл: " + filePath);
        } catch (SQLException | IOException e) {
            System.out.println("Помилка резервного копіювання таблиці " + tableName + ": " + e.getMessage());
        }
    }

    public static void backupToCSV() {
        exportTableToCSV("departments", "backup/departments.csv");
        exportTableToCSV("employees", "backup/employees.csv");
    }
}
