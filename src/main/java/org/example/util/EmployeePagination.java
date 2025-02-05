package org.example.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeePagination {
    public static List<String> getEmployeesPage(int page, int pageSize) {
        List<String> employees = new ArrayList<>();
        String sql = "SELECT id, name, salary FROM employees ORDER BY id LIMIT ? OFFSET ?";

        try (Connection connection = DataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            int offset = (page - 1) * pageSize;
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                employees.add(rs.getInt("id") + " | " +
                        rs.getString("name") + " | " +
                        rs.getDouble("salary"));
            }
        } catch (SQLException e) {
            System.out.println("Помилка отримання сторінки співробітників: " + e.getMessage());
        }
        return employees;
    }
}
