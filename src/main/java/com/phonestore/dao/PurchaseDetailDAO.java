package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.PurchaseDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý thao tác với bảng purchase_details trong MySQL.
 */
public class PurchaseDetailDAO {

    /**
     * Thêm từng sản phẩm vào bảng purchase_details trong cùng JDBC Transaction.
     */
    public boolean insertInTransaction(Connection conn, PurchaseDetail detail) throws SQLException {
        String sql = "INSERT INTO purchase_details (purchase_id, product_id, quantity, import_price, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detail.getPurchaseId());
            ps.setInt(2, detail.getProductId());
            ps.setInt(3, detail.getQuantity());
            ps.setBigDecimal(4, detail.getImportPrice());
            ps.setBigDecimal(5, detail.getSubtotal());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        detail.setPurchaseDetailId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy danh sách sản phẩm trong một phiếu nhập, JOIN với products để lấy mã và tên điện thoại.
     */
    public List<PurchaseDetail> findByPurchaseId(int purchaseId) {
        List<PurchaseDetail> list = new ArrayList<>();
        String sql = "SELECT pd.*, p.product_code, p.product_name "
                + "FROM purchase_details pd "
                + "JOIN products p ON pd.product_id = p.product_id "
                + "WHERE pd.purchase_id = ? "
                + "ORDER BY pd.purchase_detail_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, purchaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseDetail pd = new PurchaseDetail();
                    pd.setPurchaseDetailId(rs.getInt("purchase_detail_id"));
                    pd.setPurchaseId(rs.getInt("purchase_id"));
                    pd.setProductId(rs.getInt("product_id"));
                    pd.setProductCode(rs.getString("product_code"));
                    pd.setProductName(rs.getString("product_name"));
                    pd.setQuantity(rs.getInt("quantity"));
                    pd.setImportPrice(rs.getBigDecimal("import_price"));
                    pd.setSubtotal(rs.getBigDecimal("subtotal"));
                    list.add(pd);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
