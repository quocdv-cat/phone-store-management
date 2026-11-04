package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Product;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý truy xuất bảng products và hỗ trợ Transaction cập nhật tồn kho
 * dùng chung cho cả 3 thành viên.
 */
public class ProductDAO {

    public List<Product> findAllActive() {
        return searchProducts("", null, null, false);
    }

    public List<Product> searchProducts(String keyword, Integer brandId, Integer categoryId, boolean onlyInStock) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, b.brand_name, c.category_name "
                + "FROM products p "
                + "JOIN brands b ON p.brand_id = b.brand_id "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.status = 'ACTIVE' "
        );

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.product_code LIKE ? OR p.product_name LIKE ? OR p.model LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (brandId != null && brandId > 0) {
            sql.append("AND p.brand_id = ? ");
            params.add(brandId);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (onlyInStock) {
            sql.append("AND p.stock_quantity > 0 ");
        }
        sql.append("ORDER BY p.product_id ASC");

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
     * Dùng trong Transaction Bán hàng (quantityDelta < 0) hoặc Nhập hàng (quantityDelta > 0).
     * Đảm bảo tồn kho không bao giờ bị âm.
     */
    public boolean updateStockInTransaction(Connection conn, int productId, int quantityDelta) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? "
                + "WHERE product_id = ? AND (stock_quantity + ?) >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityDelta);
            ps.setInt(2, productId);
            ps.setInt(3, quantityDelta);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Dùng trong Transaction Nhập kho của Thành viên 3: Cộng tồn kho và cập nhật giá nhập mới nhất.
     */
    public boolean updateStockAndImportPriceInTransaction(Connection conn, int productId, int quantityAdded, BigDecimal newImportPrice) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ?, import_price = ? "
                + "WHERE product_id = ? AND ? > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityAdded);
            ps.setBigDecimal(2, newImportPrice);
            ps.setInt(3, productId);
            ps.setInt(4, quantityAdded);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductCode(rs.getString("product_code"));
        p.setProductName(rs.getString("product_name"));
        p.setBrandId(rs.getInt("brand_id"));
        p.setBrandName(rs.getString("brand_name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setModel(rs.getString("model"));
        p.setRam(rs.getString("ram"));
        p.setRom(rs.getString("rom"));
        p.setColor(rs.getString("color"));
        p.setImportPrice(rs.getBigDecimal("import_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setWarrantyMonths(rs.getInt("warranty_months"));
        p.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
