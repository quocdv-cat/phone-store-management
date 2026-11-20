package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý truy vấn và giao dịch bảng invoices.
 * Phụ trách bởi Thành viên 2 (Trung - Core Transaction Developer).
 */
public class InvoiceDAO {

    private final InvoiceDetailDAO invoiceDetailDAO = new InvoiceDetailDAO();

    /**
     * Thêm mới hóa đơn trong Transaction bán hàng.
     */
    public boolean insertInTransaction(Connection conn, Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (invoice_code, customer_id, employee_id, total_amount, "
                + "discount_amount, final_amount, note, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invoice.getInvoiceCode());
            ps.setInt(2, invoice.getCustomerId());
            ps.setInt(3, invoice.getEmployeeId());
            ps.setBigDecimal(4, invoice.getTotalAmount());
            ps.setBigDecimal(5, invoice.getDiscountAmount());
            ps.setBigDecimal(6, invoice.getFinalAmount());
            ps.setString(7, invoice.getNote() != null ? invoice.getNote().trim() : "");
            ps.setString(8, invoice.getStatus() != null ? invoice.getStatus() : "COMPLETED");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoice.setInvoiceId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn bán hàng.
     */
    public List<Invoice> findAll() {
        return search(null, null, null, null, null);
    }

    /**
     * Tra cứu danh sách hóa đơn theo nhiều tiêu chí lọc:
     * Từ khóa (Mã HĐ, tên khách, tên NV), Khách hàng, Khoảng ngày (Từ ngày - Đến ngày), Trạng thái.
     */
    public List<Invoice> search(String keyword, Integer customerId, LocalDate fromDate, LocalDate toDate, String status) {
        List<Invoice> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT i.invoice_id, i.invoice_code, i.customer_id, c.full_name AS customer_name, c.phone AS customer_phone, "
                + "i.employee_id, e.full_name AS employee_name, i.invoice_date, "
                + "i.total_amount, i.discount_amount, i.final_amount, i.note, i.status "
                + "FROM invoices i "
                + "JOIN customers c ON i.customer_id = c.customer_id "
                + "JOIN employees e ON i.employee_id = e.employee_id "
                + "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (i.invoice_code LIKE ? OR c.full_name LIKE ? OR c.phone LIKE ? OR e.full_name LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (customerId != null && customerId > 0) {
            sql.append("AND i.customer_id = ? ");
            params.add(customerId);
        }

        if (fromDate != null) {
            sql.append("AND DATE(i.invoice_date) >= ? ");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append("AND DATE(i.invoice_date) <= ? ");
            params.add(Date.valueOf(toDate));
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append("AND i.status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY i.invoice_date DESC, i.invoice_id DESC");

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

    /**
     * Tìm hóa đơn theo ID.
     */
    public Invoice findById(int invoiceId) {
        String sql = "SELECT i.invoice_id, i.invoice_code, i.customer_id, c.full_name AS customer_name, c.phone AS customer_phone, "
                + "i.employee_id, e.full_name AS employee_name, i.invoice_date, "
                + "i.total_amount, i.discount_amount, i.final_amount, i.note, i.status "
                + "FROM invoices i "
                + "JOIN customers c ON i.customer_id = c.customer_id "
                + "JOIN employees e ON i.employee_id = e.employee_id "
                + "WHERE i.invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
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
     * Đếm số lượng hóa đơn theo tiền tố mã ngày (ví dụ: 'HD20261106-%') để sinh mã tự động tiếp theo.
     */
    public int countInvoicesByPrefix(String prefix) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE invoice_code LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy danh sách lịch sử hóa đơn mua hàng của một khách hàng cụ thể.
     */
    public List<Invoice> findByCustomerId(int customerId) {
        return search(null, customerId, null, null, null);
    }

    /**
     * Lấy danh sách sản phẩm chi tiết của một hóa đơn.
     */
    public List<InvoiceDetail> findDetailsByInvoiceId(int invoiceId) {
        return invoiceDetailDAO.findByInvoiceId(invoiceId);
    }

    /**
     * Đếm số lượng hóa đơn của một khách hàng (kiểm tra điều kiện trước khi xóa khách hàng).
     */
    public int countInvoicesByCustomerId(int customerId) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Tính tổng số tiền khách hàng đã thanh toán thành công (status = 'COMPLETED').
     */
    public BigDecimal getTotalSpentByCustomerId(int customerId) {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) FROM invoices WHERE customer_id = ? AND status = 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setInvoiceCode(rs.getString("invoice_code"));
        inv.setCustomerId(rs.getInt("customer_id"));
        inv.setCustomerName(rs.getString("customer_name"));
        inv.setCustomerPhone(rs.getString("customer_phone"));
        inv.setEmployeeId(rs.getInt("employee_id"));
        inv.setEmployeeName(rs.getString("employee_name"));
        Timestamp ts = rs.getTimestamp("invoice_date");
        if (ts != null) {
            inv.setInvoiceDate(ts.toLocalDateTime());
        }
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        inv.setFinalAmount(rs.getBigDecimal("final_amount"));
        inv.setNote(rs.getString("note"));
        inv.setStatus(rs.getString("status"));
        return inv;
    }
}
