package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý thao tác cơ sở dữ liệu với bảng customers.
 */
public class CustomerDAO {

    /**
     * Lấy toàn bộ danh sách khách hàng, kèm theo số lượng hóa đơn đã mua.
     */
    public List<Customer> findAll() {
        return search("");
    }

    /**
     * Tìm kiếm nhanh khách hàng theo Số điện thoại hoặc Họ tên.
     * Có tính toán số lượng hóa đơn khách đã mua thông qua LEFT JOIN.
     */
    public List<Customer> search(String keyword) {
        List<Customer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at, "
                + "COUNT(i.invoice_id) AS total_orders "
                + "FROM customers c "
                + "LEFT JOIN invoices i ON c.customer_id = i.customer_id "
        );

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql.append("WHERE c.full_name LIKE ? OR c.phone LIKE ? ");
        }
        sql.append("GROUP BY c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at ");
        sql.append("ORDER BY c.customer_id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasKeyword) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithOrders(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm khách hàng theo ID.
     */
    public Customer findById(int customerId) {
        String sql = "SELECT c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at, "
                + "COUNT(i.invoice_id) AS total_orders "
                + "FROM customers c "
                + "LEFT JOIN invoices i ON c.customer_id = i.customer_id "
                + "WHERE c.customer_id = ? "
                + "GROUP BY c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithOrders(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm khách hàng theo số điện thoại chính xác.
     */
    public Customer findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at, "
                + "COUNT(i.invoice_id) AS total_orders "
                + "FROM customers c "
                + "LEFT JOIN invoices i ON c.customer_id = i.customer_id "
                + "WHERE c.phone = ? "
                + "GROUP BY c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithOrders(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại hay chưa (loại trừ ID khách hàng đang sửa nếu có).
     */
    public boolean isPhoneExists(String phone, Integer excludeCustomerId) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM customers WHERE phone = ?");
        if (excludeCustomerId != null && excludeCustomerId > 0) {
            sql.append(" AND customer_id <> ?");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, phone.trim());
            if (excludeCustomerId != null && excludeCustomerId > 0) {
                ps.setInt(2, excludeCustomerId);
            }
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

    /**
     * Thêm mới khách hàng và gán ID vừa sinh vào đối tượng.
     */
    public boolean insert(Customer customer) {
        String sql = "INSERT INTO customers (full_name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName().trim());
            ps.setString(2, customer.getPhone().trim());
            ps.setString(3, customer.getEmail() != null ? customer.getEmail().trim() : "");
            ps.setString(4, customer.getAddress() != null ? customer.getAddress().trim() : "");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setCustomerId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin khách hàng.
     */
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ?, address = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName().trim());
            ps.setString(2, customer.getPhone().trim());
            ps.setString(3, customer.getEmail() != null ? customer.getEmail().trim() : "");
            ps.setString(4, customer.getAddress() != null ? customer.getAddress().trim() : "");
            ps.setInt(5, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa khách hàng theo ID.
     */
    public boolean delete(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private Customer mapRowWithOrders(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        c.setTotalOrders(rs.getInt("total_orders"));
        return c;
    }
}
