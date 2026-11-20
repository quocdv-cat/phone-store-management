package com.phonestore.service;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.dao.InvoiceDAO;
import com.phonestore.dao.InvoiceDetailDAO;
import com.phonestore.dao.ProductDAO;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service xử lý nghiệp vụ Hóa đơn và Giao dịch Bán hàng POS (JDBC Transaction).
 * Phụ trách bởi Thành viên 2 (Trung - Core Transaction Developer).
 */
public class InvoiceService {

    private final InvoiceDAO invoiceDAO;
    private final InvoiceDetailDAO invoiceDetailDAO;
    private final ProductDAO productDAO;

    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAO();
        this.invoiceDetailDAO = new InvoiceDetailDAO();
        this.productDAO = new ProductDAO();
    }

    public InvoiceService(InvoiceDAO invoiceDAO, InvoiceDetailDAO invoiceDetailDAO, ProductDAO productDAO) {
        this.invoiceDAO = invoiceDAO;
        this.invoiceDetailDAO = invoiceDetailDAO;
        this.productDAO = productDAO;
    }

    /**
     * Sinh mã hóa đơn tự động theo định dạng chuẩn: HDyyyyMMdd-XXX (ví dụ: HD20261106-001).
     */
    public synchronized String generateNextInvoiceCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "HD" + datePart + "-";
        int currentCount = invoiceDAO.countInvoicesByPrefix(prefix);
        int nextSeq = currentCount + 1;
        return String.format("%s%03d", prefix, nextSeq);
    }

    /**
     * Thực hiện thanh toán và lưu hóa đơn bán hàng bằng JDBC Transaction.
     * Quy trình:
     * 1. setAutoCommit(false)
     * 2. insertInvoice (bảng invoices)
     * 3. Duyệt từng dòng chi tiết:
     *    - Trừ tồn kho sản phẩm bằng updateStockInTransaction (đảm bảo tồn kho >= 0)
     *    - Thêm dòng vào bảng invoice_details
     * 4. commit() nếu thành công hoàn toàn
     * 5. rollback() nếu xảy ra bất kỳ lỗi nào để bảo toàn dữ liệu
     */
    public Invoice createInvoice(Invoice invoice, List<InvoiceDetail> details) throws SQLException {
        // 1. Kiểm tra tính hợp lệ dữ liệu
        if (invoice == null) {
            throw new IllegalArgumentException("Thông tin hóa đơn không được để trống!");
        }
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng hóa đơn không có sản phẩm nào!");
        }
        if (invoice.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng hợp lệ!");
        }
        if (invoice.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Người lập hóa đơn không hợp lệ!");
        }

        // Tự động sinh mã hóa đơn nếu chưa có
        if (invoice.getInvoiceCode() == null || invoice.getInvoiceCode().trim().isEmpty()) {
            invoice.setInvoiceCode(generateNextInvoiceCode());
        }

        // Kiểm tra tính toán số tiền
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (InvoiceDetail d : details) {
            if (d.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng mua của sản phẩm phải lớn hơn 0!");
            }
            if (d.getUnitPrice() == null || d.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá sản phẩm không hợp lệ!");
            }
            BigDecimal subtotal = d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            d.setSubtotal(subtotal);
            calculatedTotal = calculatedTotal.add(subtotal);
        }

        invoice.setTotalAmount(calculatedTotal);
        if (invoice.getDiscountAmount() == null || invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            invoice.setDiscountAmount(BigDecimal.ZERO);
        }
        if (invoice.getDiscountAmount().compareTo(calculatedTotal) > 0) {
            throw new IllegalArgumentException("Số tiền giảm giá không được lớn hơn tổng tiền hàng!");
        }

        BigDecimal finalAmount = calculatedTotal.subtract(invoice.getDiscountAmount());
        invoice.setFinalAmount(finalAmount);
        invoice.setStatus("COMPLETED");

        // 2. Bắt đầu JDBC Transaction
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Bước 2.1: Thêm dòng hóa đơn vào bảng invoices
            boolean invoiceSaved = invoiceDAO.insertInTransaction(conn, invoice);
            if (!invoiceSaved || invoice.getInvoiceId() <= 0) {
                throw new SQLException("Không thể lưu hóa đơn vào cơ sở dữ liệu!");
            }

            // Bước 2.2: Lặp qua từng sản phẩm trong giỏ hàng để trừ kho và lưu chi tiết hóa đơn
            for (InvoiceDetail detail : details) {
                detail.setInvoiceId(invoice.getInvoiceId());

                // Trừ số lượng tồn kho (quantityDelta < 0)
                int sellQty = detail.getQuantity();
                boolean stockUpdated = productDAO.updateStockInTransaction(conn, detail.getProductId(), -sellQty);
                if (!stockUpdated) {
                    throw new IllegalStateException("Sản phẩm ID " + detail.getProductId()
                            + (detail.getProductName() != null ? " (" + detail.getProductName() + ")" : "")
                            + " không đủ tồn kho hoặc đã hết hàng!");
                }

                // Thêm vào bảng invoice_details
                boolean detailSaved = invoiceDetailDAO.insertInTransaction(conn, detail);
                if (!detailSaved) {
                    throw new SQLException("Lỗi khi lưu chi tiết sản phẩm ID " + detail.getProductId());
                }
            }

            // Bước 2.3: Thành công toàn bộ -> Commit transaction
            conn.commit();
            return invoice;

        } catch (Exception ex) {
            // Bước 2.4: Xảy ra lỗi -> Rollback ngay lập tức
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw (ex instanceof SQLException ? (SQLException) ex : new SQLException(ex.getMessage(), ex));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    /**
     * Tra cứu danh sách hóa đơn theo từ khóa, khách hàng, khoảng ngày và trạng thái.
     */
    public List<Invoice> searchInvoices(String keyword, Integer customerId, LocalDate fromDate, LocalDate toDate, String status) {
        return invoiceDAO.search(keyword, customerId, fromDate, toDate, status);
    }

    /**
     * Lấy danh sách toàn bộ hóa đơn.
     */
    public List<Invoice> getAllInvoices() {
        return invoiceDAO.findAll();
    }

    /**
     * Lấy thông tin hóa đơn theo ID.
     */
    public Invoice getInvoiceById(int invoiceId) {
        return invoiceDAO.findById(invoiceId);
    }

    /**
     * Lấy danh sách sản phẩm chi tiết của một hóa đơn.
     */
    public List<InvoiceDetail> getInvoiceDetails(int invoiceId) {
        return invoiceDetailDAO.findByInvoiceId(invoiceId);
    }
}
