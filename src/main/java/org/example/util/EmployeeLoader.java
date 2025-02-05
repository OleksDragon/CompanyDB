package org.example.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class EmployeeLoader {

    //Реалізація звичайного SQL-запиту в циклі
    public static void insertEmployeesIndividually(int count) {
        String sql = "INSERT INTO employees (name, salary, department_id) VALUES (?, ?, ?)";

        long startTime = System.currentTimeMillis();

        try (Connection connection = DataBase.getConnection()) {
            for (int i = 1; i <= count; i++) {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, "Employee " + i);

                // Генеруємо випадкову зарплату (10 000 - 100 000)
                double salary = ThreadLocalRandom.current().nextInt(10000, 100001);
                ps.setDouble(2, salary);

                // Вибираємо випадковий департамент (от 1 до 5)
                int departmentId = ThreadLocalRandom.current().nextInt(1, 6);
                ps.setInt(3, departmentId);

                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("❌ Помилка при вставці даних: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("✅ Час виконання (звичайний INSERT в циклі): " + (endTime - startTime) + " мс");
    }

    //Реалізація пакетного (batch) вставлення
    public static void insertEmployeesBatch(int count) {
        String sql = "INSERT INTO employees (name, salary, department_id) VALUES (?, ?, ?)";

        long startTime = System.currentTimeMillis();

        try (Connection connection = DataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Вимикаємо автоматичний коміт

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "Employee " + i);
                ps.setDouble(2, ThreadLocalRandom.current().nextInt(10000, 100001));
                ps.setDouble(3,ThreadLocalRandom.current().nextInt(1, 6));
                ps.addBatch();

                if (i % 1000 == 0) { // Виконуємо пакет кожні 1000 записів
                    ps.executeBatch();
                }
            }
            ps.executeBatch(); // Виконуємо залишки пакету
            connection.commit(); // Комітуємо транзакцію
        } catch (SQLException e) {
            System.out.println("Помилка при пакетній вставці: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Час виконання (batch INSERT): " + (endTime - startTime) + " мс");
    }
}
