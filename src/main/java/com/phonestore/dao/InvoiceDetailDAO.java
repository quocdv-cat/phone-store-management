package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.InvoiceDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý truy xuất và lưu trữ chi tiết hóa đơn (invoice_details).
 * Phụ trách bởi Thành viên 2 (Trung - Core Transaction Developer).
 */
public class InvoiceDetailDAO {

    /**
     * Thêm mới một dòng chi tiết hóa đơn trong Transaction.
     */
    public boolean insertInTransaction(Connection conn, InvoiceDetail detail) throws SQLException {
        String sql = "INSERT INTO invoice_details (invoice_id, product_id, quantity, unit_price, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detail.getInvoiceId());
            ps.setInt(2, detail.getProductId());
            ps.setInt(3, detail.getQuantity());
            ps.setBigDecimal(4, detail.getUnitPrice());
            ps.setBigDecimal(5, detail.getSubtotal());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        detail.setInvoiceDetailId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Thêm toàn bộ danh sách chi tiết hóa đơn trong Transaction.
     */
    public void insertBatchInTransaction(Connection conn, int invoiceId, List<InvoiceDetail> details) throws SQLException {
        for (InvoiceDetail detail : details) {
            detail.setInvoiceId(invoiceId);
            boolean ok = insertInTransaction(conn, detail);
            if (!ok) {
                throw new SQLException("Không thể lưu chi tiết hóa đơn cho sản phẩm ID: " + detail.getProductId());
            }
        }
    }

    /**
     * Lấy danh sách sản phẩm chi tiết của một hóa đơn (kèm theo Mã SP và Tên SP).
     */
    public List<InvoiceDetail> findByInvoiceId(int invoiceId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findByInvoiceId(conn, invoiceId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách sản phẩm chi tiết của hóa đơn với Connection truyền vào.
     */
    public List<InvoiceDetail> findByInvoiceId(Connection conn, int invoiceId) throws SQLException {
        List<InvoiceDetail> list = new ArrayList<>();
        String sql = "SELECT d.invoice_detail_id, d.invoice_id, d.product_id, "
                + "p.product_code, p.product_name, d.quantity, d.unit_price, d.subtotal "
                + "FROM invoice_details d "
                + "JOIN products p ON d.product_id = p.product_id "
                + "WHERE d.invoice_id = ? "
                + "ORDER BY d.invoice_detail_id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setInvoiceDetailId(rs.getInt("invoice_detail_id"));
                    detail.setInvoiceId(rs.getInt("invoice_id"));
                    detail.setProductId(rs.getInt("product_id"));
                    detail.setProductCode(rs.getString("product_code"));
                    detail.setProductName(rs.getString("product_name"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setUnitPrice(rs.getBigDecimal("unit_price"));
                    detail.setSubtotal(rs.getBigDecimal("subtotal"));
                    list.add(detail);
                }
            }
        }
        return list;
    }
}
