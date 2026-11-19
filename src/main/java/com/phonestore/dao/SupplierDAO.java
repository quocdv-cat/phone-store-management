package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Supplier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý thao tác CRUD với bảng suppliers trong MySQL.
 */
public class SupplierDAO {

    public List<Supplier> findAll() {
        return search("");
    }

    public List<Supplier> findAllActive() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE is_active = 1 ORDER BY supplier_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Supplier findById(int id) {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
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

    public List<Supplier> search(String keyword) {
        List<Supplier> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM suppliers ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("WHERE supplier_name LIKE ? OR phone LIKE ? OR contact_person LIKE ? OR email LIKE ? ");
        }
        sql.append("ORDER BY supplier_id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
                ps.setString(3, kw);
                ps.setString(4, kw);
            }
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

    public boolean insert(Supplier s) {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, email, address, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setInt(6, s.isActive() ? 1 : 0);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setSupplierId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean update(Supplier s) {
        String sql = "UPDATE suppliers SET supplier_name = ?, contact_person = ?, phone = ?, "
                + "email = ?, address = ?, is_active = ? WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setInt(6, s.isActive() ? 1 : 0);
            ps.setInt(7, s.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean delete(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Kiểm tra xem nhà cung cấp đã có phiếu nhập (purchase_orders) hay chưa.
     * Dùng để đáp ứng tiêu chí: Chỉ cho phép xóa nếu chưa có phiếu nhập liên quan.
     */
    public boolean hasPurchaseOrders(int supplierId) {
        String sql = "SELECT COUNT(*) FROM purchase_orders WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
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

    public boolean isPhoneExists(String phone, Integer excludeSupplierId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM suppliers WHERE phone = ?");
        if (excludeSupplierId != null && excludeSupplierId > 0) {
            sql.append(" AND supplier_id != ?");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, phone != null ? phone.trim() : "");
            if (excludeSupplierId != null && excludeSupplierId > 0) {
                ps.setInt(2, excludeSupplierId);
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

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setActive(rs.getInt("is_active") == 1);
        return s;
    }
}
