package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.PurchaseOrder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý thao tác với bảng purchase_orders trong MySQL.
 */
public class PurchaseOrderDAO {

    /**
     * Thêm phiếu nhập mới trong cùng Transaction với chi tiết phiếu nhập.
     * @return generated purchase_id nếu thành công, hoặc -1 nếu thất bại.
     */
    public int insertInTransaction(Connection conn, PurchaseOrder po) throws SQLException {
        String sql = "INSERT INTO purchase_orders (purchase_code, supplier_id, employee_id, "
                + "purchase_date, total_amount, note, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, po.getPurchaseCode());
            ps.setInt(2, po.getSupplierId());
            ps.setInt(3, po.getEmployeeId());
            if (po.getPurchaseDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(po.getPurchaseDate()));
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.setBigDecimal(5, po.getTotalAmount());
            ps.setString(6, po.getNote());
            ps.setString(7, po.getStatus() != null ? po.getStatus() : "COMPLETED");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        po.setPurchaseId(generatedId);
                        return generatedId;
                    }
                }
            }
        }
        return -1;
    }

    public List<PurchaseOrder> findAll() {
        return filter(null, null, null, null);
    }

    /**
     * Lọc và tìm kiếm danh sách phiếu nhập theo Mã phiếu, Nhà cung cấp, Khoảng ngày.
     */
    public List<PurchaseOrder> filter(String searchCode, Integer supplierId, LocalDate fromDate, LocalDate toDate) {
        List<PurchaseOrder> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT po.*, s.supplier_name, e.full_name AS employee_name "
                + "FROM purchase_orders po "
                + "JOIN suppliers s ON po.supplier_id = s.supplier_id "
                + "JOIN employees e ON po.employee_id = e.employee_id "
                + "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (searchCode != null && !searchCode.trim().isEmpty()) {
            sql.append("AND po.purchase_code LIKE ? ");
            params.add("%" + searchCode.trim() + "%");
        }
        if (supplierId != null && supplierId > 0) {
            sql.append("AND po.supplier_id = ? ");
            params.add(supplierId);
        }
        if (fromDate != null) {
            sql.append("AND DATE(po.purchase_date) >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND DATE(po.purchase_date) <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY po.purchase_date DESC, po.purchase_id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
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

    public PurchaseOrder findById(int purchaseId) {
        String sql = "SELECT po.*, s.supplier_name, e.full_name AS employee_name "
                + "FROM purchase_orders po "
                + "JOIN suppliers s ON po.supplier_id = s.supplier_id "
                + "JOIN employees e ON po.employee_id = e.employee_id "
                + "WHERE po.purchase_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, purchaseId);
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

    /**
     * Tự động sinh mã phiếu nhập theo định dạng PNyyyyMMdd-XXX (ví dụ: PN20261118-001).
     */
    public String generatePurchaseCode() {
        String prefix = "PN" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String sql = "SELECT purchase_code FROM purchase_orders WHERE purchase_code LIKE ? ORDER BY purchase_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastCode = rs.getString("purchase_code");
                    String numberPart = lastCode.substring(prefix.length());
                    int nextSeq = Integer.parseInt(numberPart) + 1;
                    return String.format("%s%03d", prefix, nextSeq);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return prefix + "001";
    }

    private PurchaseOrder mapRow(ResultSet rs) throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setPurchaseId(rs.getInt("purchase_id"));
        po.setPurchaseCode(rs.getString("purchase_code"));
        po.setSupplierId(rs.getInt("supplier_id"));
        po.setSupplierName(rs.getString("supplier_name"));
        po.setEmployeeId(rs.getInt("employee_id"));
        po.setEmployeeName(rs.getString("employee_name"));
        Timestamp ts = rs.getTimestamp("purchase_date");
        if (ts != null) {
            po.setPurchaseDate(ts.toLocalDateTime());
        }
        po.setTotalAmount(rs.getBigDecimal("total_amount"));
        po.setNote(rs.getString("note"));
        po.setStatus(rs.getString("status"));
        return po;
    }
}
