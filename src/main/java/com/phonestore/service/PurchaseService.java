package com.phonestore.service;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.dao.ProductDAO;
import com.phonestore.dao.PurchaseDetailDAO;
import com.phonestore.dao.PurchaseOrderDAO;
import com.phonestore.model.PurchaseDetail;
import com.phonestore.model.PurchaseOrder;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý nghiệp vụ Quản lý Nhập hàng & Lịch sử Phiếu nhập.
 * Chứa JDBC Transaction đảm bảo tính ACID khi nhập hàng.
 */
public class PurchaseService {

    private final PurchaseOrderDAO purchaseOrderDAO;
    private final PurchaseDetailDAO purchaseDetailDAO;
    private final ProductDAO productDAO;

    public PurchaseService() {
        this.purchaseOrderDAO = new PurchaseOrderDAO();
        this.purchaseDetailDAO = new PurchaseDetailDAO();
        this.productDAO = new ProductDAO();
    }

    public PurchaseService(PurchaseOrderDAO purchaseOrderDAO, PurchaseDetailDAO purchaseDetailDAO, ProductDAO productDAO) {
        this.purchaseOrderDAO = purchaseOrderDAO;
        this.purchaseDetailDAO = purchaseDetailDAO;
        this.productDAO = productDAO;
    }

    public String generatePurchaseCode() {
        return purchaseOrderDAO.generatePurchaseCode();
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderDAO.findAll();
    }

    public List<PurchaseOrder> filterPurchaseOrders(String searchCode, Integer supplierId, LocalDate fromDate, LocalDate toDate) {
        return purchaseOrderDAO.filter(searchCode, supplierId, fromDate, toDate);
    }

    public PurchaseOrder getPurchaseOrderById(int purchaseId) {
        return purchaseOrderDAO.findById(purchaseId);
    }

    public List<PurchaseDetail> getPurchaseDetails(int purchaseId) {
        return purchaseDetailDAO.findByPurchaseId(purchaseId);
    }

    /**
     * THỰC HIỆN GIAO DỊCH NHẬP KHO BẰNG JDBC TRANSACTION:
     * Đồng bộ:
     * 1. Thêm purchase_orders
     * 2. Thêm từng purchase_details
     * 3. Cộng dồn stock_quantity vào products
     * 4. Cập nhật import_price mới nhất vào products
     * Đảm bảo tính toàn vẹn ACID:
     * - Thành công -> COMMIT
     * - Có bất kỳ lỗi nào -> ROLLBACK toàn bộ
     *
     * @return null nếu thành công, hoặc thông báo lỗi nếu thất bại.
     */
    public String createPurchaseOrder(PurchaseOrder order, List<PurchaseDetail> details) {
        // Validation nghiệp vụ trước khi mở Transaction
        if (order == null) {
            return "Thông tin phiếu nhập không được để trống!";
        }
        if (order.getSupplierId() <= 0) {
            return "Vui lòng chọn Nhà cung cấp!";
        }
        if (order.getEmployeeId() <= 0) {
            return "Không xác định được nhân viên lập phiếu (Vui lòng đăng nhập)!";
        }
        if (details == null || details.isEmpty()) {
            return "Phiếu nhập phải có ít nhất 1 mặt hàng!";
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (PurchaseDetail d : details) {
            if (d.getProductId() <= 0) {
                return "Sản phẩm không hợp lệ trong danh sách nhập!";
            }
            if (d.getQuantity() <= 0) {
                return "Số lượng nhập của sản phẩm phải lớn hơn 0!";
            }
            if (d.getImportPrice() == null || d.getImportPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return "Đơn giá nhập của sản phẩm phải lớn hơn 0!";
            }
            BigDecimal subtotal = d.getImportPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            d.setSubtotal(subtotal);
            calculatedTotal = calculatedTotal.add(subtotal);
        }
        order.setTotalAmount(calculatedTotal);

        if (order.getPurchaseCode() == null || order.getPurchaseCode().trim().isEmpty()) {
            order.setPurchaseCode(purchaseOrderDAO.generatePurchaseCode());
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

            // 1. Thêm Master (bảng purchase_orders)
            int purchaseId = purchaseOrderDAO.insertInTransaction(conn, order);
            if (purchaseId <= 0) {
                throw new SQLException("Thêm dữ liệu vào bảng purchase_orders thất bại!");
            }

            // 2. Thêm Details & Cập nhật tồn kho + giá nhập cho từng sản phẩm
            for (PurchaseDetail d : details) {
                d.setPurchaseId(purchaseId);
                boolean detailSaved = purchaseDetailDAO.insertInTransaction(conn, d);
                if (!detailSaved) {
                    throw new SQLException("Thêm chi tiết phiếu nhập thất bại cho sản phẩm ID: " + d.getProductId());
                }

                // Tái sử dụng hàm của TV1: tăng tồn kho và cập nhật giá nhập mới
                boolean stockUpdated = productDAO.updateStockAndImportPriceInTransaction(
                        conn,
                        d.getProductId(),
                        d.getQuantity(),
                        d.getImportPrice()
                );
                if (!stockUpdated) {
                    throw new SQLException("Cập nhật tồn kho và giá nhập thất bại cho sản phẩm ID: " + d.getProductId());
                }
            }

            // Tất cả bước thành công -> COMMIT
            conn.commit();
            return null; // Thành công

        } catch (Exception ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // ROLLBACK TOÀN BỘ KHI CÓ LỖI
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
            }
            ex.printStackTrace();
            return "Lỗi thực thi giao dịch nhập kho: " + ex.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException clEx) {
                    clEx.printStackTrace();
                }
            }
        }
    }
}
