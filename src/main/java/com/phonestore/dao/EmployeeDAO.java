package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Employee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO thực hiện đầy đủ CRUD cho bảng employees (Nhân viên).
 */
public class EmployeeDAO {

    public List<Employee> findAll() {
        return search("");
    }

    public List<Employee> search(String keyword) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, employee_code, full_name, phone, email, address, position, status, created_at "
                + "FROM employees "
                + "WHERE employee_code LIKE ? OR full_name LIKE ? OR phone LIKE ? OR position LIKE ? "
                + "ORDER BY employee_id ASC";
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Employee findById(int employeeId) {
        String sql = "SELECT employee_id, employee_code, full_name, phone, email, address, position, status, created_at "
                + "FROM employees WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean existsByCode(String employeeCode, int excludeEmployeeId) {
        String sql = "SELECT COUNT(*) FROM employees WHERE LOWER(employee_code) = LOWER(?) AND employee_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeCode.trim());
            ps.setInt(2, excludeEmployeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean existsByPhone(String phone, int excludeEmployeeId) {
        String sql = "SELECT COUNT(*) FROM employees WHERE phone = ? AND employee_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            ps.setInt(2, excludeEmployeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public int insert(Employee emp) {
        String sql = "INSERT INTO employees (employee_code, full_name, phone, email, address, position, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, emp.getEmployeeCode().trim());
            ps.setString(2, emp.getFullName().trim());
            ps.setString(3, emp.getPhone().trim());
            ps.setString(4, emp.getEmail() == null ? "" : emp.getEmail().trim());
            ps.setString(5, emp.getAddress() == null ? "" : emp.getAddress().trim());
            ps.setString(6, emp.getPosition().trim());
            ps.setString(7, emp.getStatus() == null ? "ACTIVE" : emp.getStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public boolean update(Employee emp) {
        String sql = "UPDATE employees SET employee_code = ?, full_name = ?, phone = ?, email = ?, "
                + "address = ?, position = ?, status = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getEmployeeCode().trim());
            ps.setString(2, emp.getFullName().trim());
            ps.setString(3, emp.getPhone().trim());
            ps.setString(4, emp.getEmail() == null ? "" : emp.getEmail().trim());
            ps.setString(5, emp.getAddress() == null ? "" : emp.getAddress().trim());
            ps.setString(6, emp.getPosition().trim());
            ps.setString(7, emp.getStatus() == null ? "ACTIVE" : emp.getStatus());
            ps.setInt(8, emp.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean hasInvoicesOrPurchases(int employeeId) {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM invoices WHERE employee_id = ?) + "
                + "(SELECT COUNT(*) FROM purchase_orders WHERE employee_id = ?) AS total_tx";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_tx") > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deletePhysical(int employeeId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean softDelete(int employeeId) {
        String sql = "UPDATE employees SET status = 'INACTIVE' WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
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
