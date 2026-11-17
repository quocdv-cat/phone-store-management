package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.PositionPermission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng phân quyền theo từng Chức vụ (position_permissions).
 * Lưu cả trạng thái bật/tắt (Checkbox) và mức độ quyền (Chỉ xem / Toàn quyền / Bán hàng / Nhập hàng).
 */
public class PositionPermissionDAO {

    public PositionPermissionDAO() {
        ensureTableAndDefaults();
    }

    private void ensureTableAndDefaults() {
        String createSql = "CREATE TABLE IF NOT EXISTS position_permissions ("
                + "position_name VARCHAR(100) PRIMARY KEY, "
                + "can_products TINYINT(1) NOT NULL DEFAULT 1, "
                + "mode_products VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem', "
                + "can_cat_brand TINYINT(1) NOT NULL DEFAULT 1, "
                + "mode_cat_brand VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem', "
                + "can_sales TINYINT(1) NOT NULL DEFAULT 1, "
                + "mode_sales VARCHAR(30) NOT NULL DEFAULT 'Bán hàng', "
                + "can_invoices TINYINT(1) NOT NULL DEFAULT 1, "
                + "mode_invoices VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem', "
                + "can_customers TINYINT(1) NOT NULL DEFAULT 1, "
                + "mode_customers VARCHAR(30) NOT NULL DEFAULT 'Toàn quyền', "
                + "can_purchase TINYINT(1) NOT NULL DEFAULT 1, "
                + "mode_purchase VARCHAR(30) NOT NULL DEFAULT 'Nhập hàng', "
                + "can_suppliers TINYINT(1) NOT NULL DEFAULT 1, "
                + "mode_suppliers VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem', "
                + "can_employees TINYINT(1) NOT NULL DEFAULT 0, "
                + "mode_employees VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem', "
                + "can_statistics TINYINT(1) NOT NULL DEFAULT 0, "
                + "mode_statistics VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem'"
                + ") ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";

        String[] alterCols = {
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_products VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_cat_brand VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_sales VARCHAR(30) NOT NULL DEFAULT 'Bán hàng'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_invoices VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_customers VARCHAR(30) NOT NULL DEFAULT 'Toàn quyền'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_purchase VARCHAR(30) NOT NULL DEFAULT 'Nhập hàng'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_suppliers VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_employees VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem'",
            "ALTER TABLE position_permissions ADD COLUMN IF NOT EXISTS mode_statistics VARCHAR(30) NOT NULL DEFAULT 'Chỉ xem'"
        };

        String seedSql = "INSERT IGNORE INTO position_permissions "
                + "(position_name, can_products, mode_products, can_cat_brand, mode_cat_brand, "
                + "can_sales, mode_sales, can_invoices, mode_invoices, can_customers, mode_customers, "
                + "can_purchase, mode_purchase, can_suppliers, mode_suppliers, can_employees, mode_employees, "
                + "can_statistics, mode_statistics) VALUES "
                + "('Cửa hàng trưởng', 1, 'Toàn quyền', 1, 'Toàn quyền', 1, 'Bán hàng', 1, 'Toàn quyền', 1, 'Toàn quyền', 1, 'Nhập hàng', 1, 'Toàn quyền', 1, 'Toàn quyền', 1, 'Toàn quyền'), "
                + "('Nhân viên bán hàng', 1, 'Chỉ xem', 0, 'Chỉ xem', 1, 'Bán hàng', 1, 'Chỉ xem', 1, 'Toàn quyền', 0, 'Nhập hàng', 0, 'Chỉ xem', 0, 'Chỉ xem', 0, 'Chỉ xem'), "
                + "('Nhân viên kho', 1, 'Toàn quyền', 1, 'Toàn quyền', 0, 'Bán hàng', 0, 'Chỉ xem', 0, 'Chỉ xem', 1, 'Nhập hàng', 1, 'Toàn quyền', 0, 'Chỉ xem', 0, 'Chỉ xem'), "
                + "('Kế toán', 1, 'Chỉ xem', 0, 'Chỉ xem', 0, 'Bán hàng', 1, 'Toàn quyền', 1, 'Chỉ xem', 1, 'Nhập hàng', 1, 'Chỉ xem', 0, 'Chỉ xem', 1, 'Toàn quyền'), "
                + "('Kỹ thuật viên', 1, 'Chỉ xem', 1, 'Chỉ xem', 0, 'Bán hàng', 1, 'Chỉ xem', 1, 'Chỉ xem', 0, 'Nhập hàng', 0, 'Chỉ xem', 0, 'Chỉ xem', 0, 'Chỉ xem')";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(createSql);
            for (String alter : alterCols) {
                try {
                    st.execute(alter);
                } catch (SQLException ignored) {
                }
            }
            st.executeUpdate(seedSql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<PositionPermission> findAll() {
        List<PositionPermission> list = new ArrayList<>();
        String sql = "SELECT * FROM position_permissions ORDER BY position_name ASC";
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

    public PositionPermission findByPosition(String positionName) {
        if (positionName == null || positionName.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM position_permissions WHERE LOWER(position_name) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, positionName.trim());
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

    public boolean saveOrUpdate(PositionPermission perm) {
        String sql = "INSERT INTO position_permissions "
                + "(position_name, can_products, mode_products, can_cat_brand, mode_cat_brand, "
                + "can_sales, mode_sales, can_invoices, mode_invoices, can_customers, mode_customers, "
                + "can_purchase, mode_purchase, can_suppliers, mode_suppliers, can_employees, mode_employees, "
                + "can_statistics, mode_statistics) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "can_products = VALUES(can_products), mode_products = VALUES(mode_products), "
                + "can_cat_brand = VALUES(can_cat_brand), mode_cat_brand = VALUES(mode_cat_brand), "
                + "can_sales = VALUES(can_sales), mode_sales = VALUES(mode_sales), "
                + "can_invoices = VALUES(can_invoices), mode_invoices = VALUES(mode_invoices), "
                + "can_customers = VALUES(can_customers), mode_customers = VALUES(mode_customers), "
                + "can_purchase = VALUES(can_purchase), mode_purchase = VALUES(mode_purchase), "
                + "can_suppliers = VALUES(can_suppliers), mode_suppliers = VALUES(mode_suppliers), "
                + "can_employees = VALUES(can_employees), mode_employees = VALUES(mode_employees), "
                + "can_statistics = VALUES(can_statistics), mode_statistics = VALUES(mode_statistics)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm.getPositionName().trim());
            ps.setInt(2, perm.isCanProducts() ? 1 : 0);
            ps.setString(3, perm.getModeProducts());
            ps.setInt(4, perm.isCanCatBrand() ? 1 : 0);
            ps.setString(5, perm.getModeCatBrand());
            ps.setInt(6, perm.isCanSales() ? 1 : 0);
            ps.setString(7, perm.getModeSales());
            ps.setInt(8, perm.isCanInvoices() ? 1 : 0);
            ps.setString(9, perm.getModeInvoices());
            ps.setInt(10, perm.isCanCustomers() ? 1 : 0);
            ps.setString(11, perm.getModeCustomers());
            ps.setInt(12, perm.isCanPurchase() ? 1 : 0);
            ps.setString(13, perm.getModePurchase());
            ps.setInt(14, perm.isCanSuppliers() ? 1 : 0);
            ps.setString(15, perm.getModeSuppliers());
            ps.setInt(16, perm.isCanEmployees() ? 1 : 0);
            ps.setString(17, perm.getModeEmployees());
            ps.setInt(18, perm.isCanStatistics() ? 1 : 0);
            ps.setString(19, perm.getModeStatistics());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private PositionPermission mapRow(ResultSet rs) throws SQLException {
        PositionPermission p = new PositionPermission(
                rs.getString("position_name"),
                rs.getInt("can_products") == 1,
                rs.getInt("can_cat_brand") == 1,
                rs.getInt("can_sales") == 1,
                rs.getInt("can_invoices") == 1,
                rs.getInt("can_customers") == 1,
                rs.getInt("can_purchase") == 1,
                rs.getInt("can_suppliers") == 1,
                rs.getInt("can_employees") == 1,
                rs.getInt("can_statistics") == 1
        );
        p.setModeProducts(getSafeString(rs, "mode_products", "Chỉ xem"));
        p.setModeCatBrand(getSafeString(rs, "mode_cat_brand", "Chỉ xem"));
        p.setModeSales(getSafeString(rs, "mode_sales", "Bán hàng"));
        p.setModeInvoices(getSafeString(rs, "mode_invoices", "Chỉ xem"));
        p.setModeCustomers(getSafeString(rs, "mode_customers", "Toàn quyền"));
        p.setModePurchase(getSafeString(rs, "mode_purchase", "Nhập hàng"));
        p.setModeSuppliers(getSafeString(rs, "mode_suppliers", "Chỉ xem"));
        p.setModeEmployees(getSafeString(rs, "mode_employees", "Chỉ xem"));
        p.setModeStatistics(getSafeString(rs, "mode_statistics", "Chỉ xem"));
        return p;
    }

    private String getSafeString(ResultSet rs, String col, String defaultVal) {
        try {
            String val = rs.getString(col);
            return (val != null && !val.isEmpty()) ? val : defaultVal;
        } catch (SQLException ex) {
            return defaultVal;
        }
    }
}
