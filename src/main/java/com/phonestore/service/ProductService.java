package com.phonestore.service;

import com.phonestore.dao.ProductDAO;
import com.phonestore.model.Product;
import com.phonestore.utils.ValidationUtils;
import java.math.BigDecimal;
import java.util.List;

/**
 * Xử lý nghiệp vụ CRUD và kiểm tra dữ liệu cho Sản phẩm Điện thoại (Product).
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<Product> searchProducts(String keyword, Integer brandId, Integer categoryId, boolean onlyInStock, String statusFilter) {
        return productDAO.searchProducts(keyword, brandId, categoryId, onlyInStock, statusFilter);
    }

    public Product getById(int productId) {
        return productDAO.findById(productId);
    }

    public String addProduct(Product product) {
        String validationError = validateProduct(product, 0);
        if (validationError != null) {
            return validationError;
        }
        return productDAO.insert(product) ? null : "Lỗi cơ sở dữ liệu khi thêm điện thoại mới!";
    }

    public String updateProduct(Product product) {
        if (product.getProductId() <= 0) {
            return "Vui lòng chọn một điện thoại từ bảng để cập nhật!";
        }
        String validationError = validateProduct(product, product.getProductId());
        if (validationError != null) {
            return validationError;
        }
        return productDAO.update(product) ? null : "Lỗi cơ sở dữ liệu khi cập nhật thông tin điện thoại!";
    }

    /**
     * Xóa sản phẩm:
     * - Nếu sản phẩm chưa từng phát sinh Hóa đơn hay Phiếu nhập: xóa vật lý khỏi DB.
     * - Nếu đã phát sinh giao dịch: tự động chuyển sang trạng thái INACTIVE (Ngừng kinh doanh)
     *   để bảo toàn lịch sử hóa đơn và báo cáo thống kê.
     */
    public String deleteProduct(int productId) {
        if (productId <= 0) {
            return "Vui lòng chọn điện thoại cần xóa!";
        }
        if (productDAO.hasTransactions(productId)) {
            boolean softDeleted = productDAO.softDelete(productId);
            return softDeleted
                    ? "SOFT_DELETED"
                    : "Không thể chuyển trạng thái ngừng kinh doanh cho sản phẩm này!";
        } else {
            boolean deleted = productDAO.deletePhysical(productId);
            return deleted ? null : "Không thể xóa sản phẩm khỏi cơ sở dữ liệu!";
        }
    }

    private String validateProduct(Product p, int excludeId) {
        if (ValidationUtils.isEmpty(p.getProductCode())) {
            return "Mã sản phẩm không được để trống (VD: SP009)!";
        }
        if (productDAO.existsByCode(p.getProductCode(), excludeId)) {
            return "Mã sản phẩm '" + p.getProductCode().trim() + "' đã tồn tại trong hệ thống!";
        }
        if (ValidationUtils.isEmpty(p.getProductName())) {
            return "Tên điện thoại không được để trống!";
        }
        if (p.getBrandId() <= 0) {
            return "Vui lòng chọn Thương hiệu!";
        }
        if (p.getCategoryId() <= 0) {
            return "Vui lòng chọn Danh mục!";
        }
        if (ValidationUtils.isEmpty(p.getRam())) {
            return "Thông số RAM không được để trống (VD: 8GB)!";
        }
        if (ValidationUtils.isEmpty(p.getRom())) {
            return "Thông số Bộ nhớ trong (ROM) không được để trống (VD: 256GB)!";
        }
        if (ValidationUtils.isEmpty(p.getColor())) {
            return "Màu sắc không được để trống!";
        }
        if (p.getSellingPrice() == null || p.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return "Giá bán phải lớn hơn 0!";
        }
        if (p.getStockQuantity() < 0) {
            return "Số lượng tồn kho không được âm!";
        }
        if (p.getWarrantyMonths() < 0) {
            return "Thời gian bảo hành (tháng) không được âm!";
        }
        return null;
    }
}
