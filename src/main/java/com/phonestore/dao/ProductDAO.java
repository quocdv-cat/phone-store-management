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
 * DAO quản lý đầy đủ CRUD bảng products và hỗ trợ Transaction cập nhật tồn kho
 * dùng chung cho cả 3 thành viên.
 */
public class ProductDAO {

    public List<Product> findAllActive() {
        return searchProducts("", null, null, false, "ACTIVE");
    }

    public List<Product> searchProducts(String keyword, Integer brandId, Integer categoryId, boolean onlyInStock) {
        return searchProducts(keyword, brandId, categoryId, onlyInStock, "ACTIVE");
    }

    public List<Product> searchProducts(String keyword, Integer brandId, Integer categoryId, boolean onlyInStock, String statusFilter) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, b.brand_name, c.category_name "
                + "FROM products p "
                + "JOIN brands b ON p.brand_id = b.brand_id "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append("AND p.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.product_code LIKE ? OR p.product_name LIKE ? OR p.model LIKE ? OR b.brand_name LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
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

    public Product findById(int productId) {
        String sql = "SELECT p.*, b.brand_name, c.category_name "
                + "FROM products p "
                + "JOIN brands b ON p.brand_id = b.brand_id "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
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

    public Product findByCode(String productCode) {
        String sql = "SELECT p.*, b.brand_name, c.category_name "
                + "FROM products p "
                + "JOIN brands b ON p.brand_id = b.brand_id "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE LOWER(p.product_code) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productCode.trim());
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

    public boolean existsByCode(String productCode, int excludeProductId) {
        String sql = "SELECT COUNT(*) FROM products WHERE LOWER(product_code) = LOWER(?) AND product_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productCode.trim());
            ps.setInt(2, excludeProductId);
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

    public boolean insert(Product p) {
        String sql = "INSERT INTO products (product_code, product_name, brand_id, category_id, model, "
                + "ram, rom, color, import_price, selling_price, stock_quantity, warranty_months, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductCode().trim());
            ps.setString(2, p.getProductName().trim());
            ps.setInt(3, p.getBrandId());
            ps.setInt(4, p.getCategoryId());
            ps.setString(5, p.getModel() == null ? "" : p.getModel().trim());
            ps.setString(6, p.getRam().trim());
            ps.setString(7, p.getRom().trim());
            ps.setString(8, p.getColor().trim());
            ps.setBigDecimal(9, p.getImportPrice() == null ? BigDecimal.ZERO : p.getImportPrice());
            ps.setBigDecimal(10, p.getSellingPrice());
            ps.setInt(11, p.getStockQuantity());
            ps.setInt(12, p.getWarrantyMonths());
            ps.setString(13, p.getStatus() == null ? "ACTIVE" : p.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean update(Product p) {
        String sql = "UPDATE products SET product_code = ?, product_name = ?, brand_id = ?, category_id = ?, "
                + "model = ?, ram = ?, rom = ?, color = ?, selling_price = ?, "
                + "stock_quantity = ?, warranty_months = ?, status = ? "
                + "WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductCode().trim());
            ps.setString(2, p.getProductName().trim());
            ps.setInt(3, p.getBrandId());
            ps.setInt(4, p.getCategoryId());
            ps.setString(5, p.getModel() == null ? "" : p.getModel().trim());
            ps.setString(6, p.getRam().trim());
            ps.setString(7, p.getRom().trim());
            ps.setString(8, p.getColor().trim());
            ps.setBigDecimal(9, p.getSellingPrice());
            ps.setInt(10, p.getStockQuantity());
            ps.setInt(11, p.getWarrantyMonths());
            ps.setString(12, p.getStatus() == null ? "ACTIVE" : p.getStatus());
            ps.setInt(13, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean hasTransactions(int productId) {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM invoice_details WHERE product_id = ?) + "
                + "(SELECT COUNT(*) FROM purchase_details WHERE product_id = ?) AS total_refs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_refs") > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean softDelete(int productId) {
        String sql = "UPDATE products SET status = 'INACTIVE' WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deletePhysical(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
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
