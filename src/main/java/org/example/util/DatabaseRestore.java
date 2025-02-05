package org.example.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DatabaseRestore {

    public static void restoreFromCSV() {
        try (Connection connection = DataBase.getConnection();
             Statement stmt = connection.createStatement()) {

            // 1️⃣ Очищаем таблицы перед восстановлением
            System.out.println("🗑 Очищення таблиць перед відновленням...");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("TRUNCATE TABLE employees");
            stmt.executeUpdate("TRUNCATE TABLE departments");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("✅ Таблиці очищено!");

        } catch (SQLException e) {
            System.out.println("❌ Помилка очищення таблиць: " + e.getMessage());
            return;
        }

        // 2️⃣ Восстанавливаем департаменты
        importTableFromCSV("departments", "backup/departments.csv");

        // 3️⃣ Восстанавливаем сотрудников
        importTableFromCSV("employees", "backup/employees.csv");
    }

    private static void importTableFromCSV(String tableName, String filePath) {
        String sql = (tableName.equals("departments"))
                ? "REPLACE INTO departments (id, name, budget) VALUES (?, ?, ?)"
                : "REPLACE INTO employees (id, name, salary, department_id) VALUES (?, ?, ?, ?)";

        try (Connection connection = DataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isFirstLine = true;
            int batchSize = 1000; // Размер батча
            int count = 0;

            connection.setAutoCommit(false);

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");

                if ((tableName.equals("departments") && values.length < 3) ||
                        (tableName.equals("employees") && values.length < 4)) {
                    System.out.println("❌ Помилка у файлі " + filePath + ": не вистачає колонок.");
                    continue;
                }

                try {
                    ps.setInt(1, Integer.parseInt(values[0])); // id
                    ps.setString(2, values[1]); // name

                    if (tableName.equals("departments")) {
                        ps.setDouble(3, Double.parseDouble(values[2])); // budget
                    } else {
                        ps.setDouble(3, Double.parseDouble(values[2])); // salary
                        ps.setInt(4, Integer.parseInt(values[3])); // department_id
                    }

                    ps.addBatch();
                    count++;

                    // Выполняем batch каждые 1000 строк
                    if (count % batchSize == 0) {
                        ps.executeBatch();
                        System.out.println("✅ Завантажено " + count + " рядків...");
                    }

                } catch (NumberFormatException e) {
                    System.out.println("❌ Помилка у файлі " + filePath + ": " + e.getMessage());
                }
            }

            // Финальный executeBatch(), если осталось меньше 1000 строк
            if (count % batchSize != 0) {
                ps.executeBatch();
            }

            connection.commit();
            System.out.println("✅ Усього імпортовано " + count + " рядків у таблицю " + tableName);

        } catch (SQLException | IOException e) {
            System.out.println("❌ Помилка імпорту таблиці " + tableName + ": " + e.getMessage());
        }
    }
}
