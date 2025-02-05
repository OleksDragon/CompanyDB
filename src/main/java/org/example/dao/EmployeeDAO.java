package org.example.dao;

import org.example.util.DataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeDAO {

    // Добавление нового сотрудника
    public void addEmployee(String name, double salary, int departmentId) throws SQLException {
        String sql = "INSERT INTO employees (name, salary, department_id) VALUES (?, ?, ?)";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, salary);
            stmt.setInt(3, departmentId);
            stmt.executeUpdate();
        }
    }

    // Обновление данных сотрудника
    public void updateEmployee(int id, String newName, double newSalary, int newDepartmentId) throws SQLException {
        try (Connection conn = DataBase.getConnection()) {
            if (!isBudgetEnough(newDepartmentId, newSalary, conn)) {
                throw new SQLException("Ошибка: Зарплата превышает бюджет департамента!");
            }

            String sql = "UPDATE employees SET name = ?, salary = ?, department_id = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newName);
                stmt.setDouble(2, newSalary);
                stmt.setInt(3, newDepartmentId);
                stmt.setInt(4, id);
                stmt.executeUpdate();
            }
        }
    }

    // Удаление сотрудника
    public void deleteEmployee(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Перевод сотрудника в другой департамент
    public void transferEmployee(int employeeId, int newDepartmentId) throws SQLException {
        String getEmployeeInfo = "SELECT salary, department_id FROM employees WHERE id = ?";
        String updateEmployeeDepartment = "UPDATE employees SET department_id = ? WHERE id = ?";
        String decreaseOldBudget = "UPDATE departments SET budget = budget + ? WHERE id = ?";
        String increaseNewBudget = "UPDATE departments SET budget = budget - ? WHERE id = ?";

        try (Connection conn = DataBase.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(getEmployeeInfo);
                 PreparedStatement stmt2 = conn.prepareStatement(updateEmployeeDepartment);
                 PreparedStatement stmt3 = conn.prepareStatement(decreaseOldBudget);
                 PreparedStatement stmt4 = conn.prepareStatement(increaseNewBudget)) {

                // Получаем информацию о сотруднике
                stmt1.setInt(1, employeeId);
                ResultSet rs = stmt1.executeQuery();

                if (!rs.next()) {
                    throw new SQLException("Сотрудник не найден.");
                }

                double salary = rs.getDouble("salary");
                int oldDepartmentId = rs.getInt("department_id");

                // Проверяем, достаточно ли бюджета у нового департамента
                if (!isBudgetEnough(newDepartmentId, salary, conn)) {
                    throw new SQLException("Ошибка: Недостаточно бюджета в новом департаменте.");
                }

                // Обновляем департамент сотрудника
                stmt2.setInt(1, newDepartmentId);
                stmt2.setInt(2, employeeId);
                stmt2.executeUpdate();

                // Уменьшаем бюджет старого департамента
                stmt3.setDouble(1, salary);
                stmt3.setInt(2, oldDepartmentId);
                stmt3.executeUpdate();

                // Увеличиваем бюджет нового департамента
                stmt4.setDouble(1, salary);
                stmt4.setInt(2, newDepartmentId);
                stmt4.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Перевод нескольких сотрудников (batch update)
    public void transferMultipleEmployees(List<Integer> employeeIds, int newDepartmentId) throws SQLException {
        String getEmployeeInfo = "SELECT id, salary, department_id FROM employees WHERE id = ?";
        String updateEmployeeDepartment = "UPDATE employees SET department_id = ? WHERE id = ?";
        String decreaseOldBudget = "UPDATE departments SET budget = budget + ? WHERE id = ?";
        String increaseNewBudget = "UPDATE departments SET budget = budget - ? WHERE id = ?";

        try (Connection conn = DataBase.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(getEmployeeInfo);
                 PreparedStatement stmt2 = conn.prepareStatement(updateEmployeeDepartment);
                 PreparedStatement stmt3 = conn.prepareStatement(decreaseOldBudget);
                 PreparedStatement stmt4 = conn.prepareStatement(increaseNewBudget)) {

                double totalSalary = 0;
                Map<Integer, Double> employeeData = new HashMap<>();

                // Получаем информацию о всех сотрудниках
                for (int employeeId : employeeIds) {
                    stmt1.setInt(1, employeeId);
                    ResultSet rs = stmt1.executeQuery();

                    if (rs.next()) {
                        double salary = rs.getDouble("salary");
                        int oldDepartmentId = rs.getInt("department_id");

                        employeeData.put(employeeId, salary);
                        totalSalary += salary;
                    } else {
                        throw new SQLException("Сотрудник с ID " + employeeId + " не найден.");
                    }
                }

                // Проверяем бюджет нового департамента
                if (!isBudgetEnough(newDepartmentId, totalSalary, conn)) {
                    throw new SQLException("Ошибка: Недостаточно бюджета в новом департаменте.");
                }

                // Обновляем департамент сотрудников пакетно
                for (int employeeId : employeeData.keySet()) {
                    stmt2.setInt(1, newDepartmentId);
                    stmt2.setInt(2, employeeId);
                    stmt2.addBatch();
                }
                stmt2.executeBatch();

                // Корректируем бюджеты старых департаментов
                for (Map.Entry<Integer, Double> entry : employeeData.entrySet()) {
                    int oldDepartmentId = getOldDepartmentId(entry.getKey(), conn);
                    stmt3.setDouble(1, entry.getValue());
                    stmt3.setInt(2, oldDepartmentId);
                    stmt3.addBatch();
                }
                stmt3.executeBatch();

                // Корректируем бюджет нового департамента
                stmt4.setDouble(1, totalSalary);
                stmt4.setInt(2, newDepartmentId);
                stmt4.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Проверка бюджета перед переводом сотрудника
    private boolean isBudgetEnough(int departmentId, double salary, Connection conn) throws SQLException {
        String sql = "SELECT budget - COALESCE((SELECT SUM(salary) FROM employees WHERE department_id = ?), 0) AS remaining_budget FROM departments WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            stmt.setInt(2, departmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double remainingBudget = rs.getDouble("remaining_budget");
                return salary <= remainingBudget;
            }
        }
        return false;
    }

    // Получаем ID старого департамента
    private int getOldDepartmentId(int employeeId, Connection conn) throws SQLException {
        String sql = "SELECT department_id FROM employees WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("department_id");
            }
        }
        throw new SQLException("Ошибка: Не удалось найти старый департамент для сотрудника ID " + employeeId);
    }

    // Отримати список співробітників, відсортованих за зарплатою (найвища перша)
    public List<String> getEmployeeSalaryRanking() throws SQLException {
        List<String> ranking = new ArrayList<>();
        String sql = "SELECT name, salary FROM employees ORDER BY salary DESC";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String employee = rs.getString("name") + " - $" + rs.getDouble("salary");
                ranking.add(employee);
            }
        }
        return ranking;
    }
}
