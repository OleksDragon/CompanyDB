package org.example.dao;

import org.example.util.DataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    // Добавление департамента
    public void addDepartment(String name, double budget) throws SQLException {
        String sql = "INSERT INTO departments (name, budget) VALUES (?, ?)";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, budget);
            stmt.executeUpdate();
        }
    }

    // Обновление данных департамента
    public void updateDepartment(int id, String newName, double newBudget) throws SQLException {
        String sql = "UPDATE departments SET name = ?, budget = ? WHERE id = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setDouble(2, newBudget);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }

    // Удаление департамента
    public void deleteDepartment(int id) throws SQLException {
        String sql = "DELETE FROM departments WHERE id = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Получить всех сотрудников конкретного департамента
    public List<String> getEmployeesByDepartment(int departmentId) throws SQLException {
        String sql = "SELECT name FROM employees WHERE department_id = ?";
        List<String> employees = new ArrayList<>();
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(rs.getString("name"));
                }
            }
        }
        return employees;
    }

    // Департамент с наибольшим бюджетом
    public String getDepartmentWithHighestBudget() throws SQLException {
        String sql = "SELECT name FROM departments ORDER BY budget DESC LIMIT 1"; // FIXED
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("name");
            }
        }
        return null;
    }

    // Департамент с наибольшим числом сотрудников
    public String getDepartmentWithMostEmployees() throws SQLException {
        String sql = "SELECT d.name FROM departments d " +
                "LEFT JOIN employees e ON d.id = e.department_id " +
                "GROUP BY d.id, d.name " +
                "ORDER BY COUNT(e.id) DESC LIMIT 1"; // FIXED
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("name");
            }
        }
        return null;
    }

    // Средняя зарплата по департаментам
    public List<String> getAverageSalaryPerDepartment() throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT d.name, COALESCE(AVG(e.salary), 0) AS avg_salary " +
                "FROM departments d " +
                "LEFT JOIN employees e ON d.id = e.department_id " +
                "GROUP BY d.name";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String department = rs.getString("name");
                double avgSalary = rs.getDouble("avg_salary");
                result.add(department + ": " + avgSalary);
            }
        }
        return result;
    }
}
