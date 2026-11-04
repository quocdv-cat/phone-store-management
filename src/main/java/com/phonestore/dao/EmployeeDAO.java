package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Employee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class EmployeeDAO {

    public Employee findById(int employeeId) {
        String sql = "SELECT employee_id, employee_code, full_name, phone, email, address, position, status, created_at "
                + "FROM employees WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmployeeId(rs.getInt("employee_id"));
                    emp.setEmployeeCode(rs.getString("employee_code"));
                    emp.setFullName(rs.getString("full_name"));
                    emp.setPhone(rs.getString("phone"));
                    emp.setEmail(rs.getString("email"));
                    emp.setAddress(rs.getString("address"));
                    emp.setPosition(rs.getString("position"));
                    emp.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        emp.setCreatedAt(ts.toLocalDateTime());
                    }
                    return emp;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
