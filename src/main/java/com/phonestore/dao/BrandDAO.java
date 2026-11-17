package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Brand;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO thực hiện đầy đủ CRUD cho bảng brands (Thương hiệu điện thoại).
 */
public class BrandDAO {

    public List<Brand> findAll() {
        return search("");
    }

    public List<Brand> findAllActive() {
        List<Brand> list = new ArrayList<>();
        String sql = "SELECT brand_id, brand_name, origin_country, description, is_active "
                + "FROM brands WHERE is_active = 1 ORDER BY brand_name ASC";
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

    public List<Brand> search(String keyword) {
        List<Brand> list = new ArrayList<>();
        String sql = "SELECT brand_id, brand_name, origin_country, description, is_active "
                + "FROM brands "
                + "WHERE brand_name LIKE ? OR origin_country LIKE ? OR description LIKE ? "
                + "ORDER BY brand_id ASC";
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
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

    public Brand findById(int brandId) {
        String sql = "SELECT brand_id, brand_name, origin_country, description, is_active "
                + "FROM brands WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, brandId);
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

    public boolean existsByName(String brandName, int excludeId) {
        String sql = "SELECT COUNT(*) FROM brands WHERE LOWER(brand_name) = LOWER(?) AND brand_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brandName.trim());
            ps.setInt(2, excludeId);
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

    public boolean insert(Brand brand) {
        String sql = "INSERT INTO brands (brand_name, origin_country, description, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brand.getBrandName().trim());
            ps.setString(2, brand.getOriginCountry() == null ? "" : brand.getOriginCountry().trim());
            ps.setString(3, brand.getDescription() == null ? "" : brand.getDescription().trim());
            ps.setInt(4, brand.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean update(Brand brand) {
        String sql = "UPDATE brands SET brand_name = ?, origin_country = ?, description = ?, is_active = ? "
                + "WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brand.getBrandName().trim());
            ps.setString(2, brand.getOriginCountry() == null ? "" : brand.getOriginCountry().trim());
            ps.setString(3, brand.getDescription() == null ? "" : brand.getDescription().trim());
            ps.setInt(4, brand.isActive() ? 1 : 0);
            ps.setInt(5, brand.getBrandId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int countProductsByBrand(int brandId) {
        String sql = "SELECT COUNT(*) FROM products WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, brandId);
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

    public boolean delete(int brandId) {
        String sql = "DELETE FROM brands WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, brandId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Brand mapRow(ResultSet rs) throws SQLException {
        return new Brand(
                rs.getInt("brand_id"),
                rs.getString("brand_name"),
                rs.getString("origin_country"),
                rs.getString("description"),
                rs.getInt("is_active") == 1
        );
    }
}
