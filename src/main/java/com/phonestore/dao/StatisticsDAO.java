package com.phonestore.dao;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.model.Product;
import com.phonestore.model.dto.DashboardSummaryDTO;
import com.phonestore.model.dto.RevenueStatDTO;
import com.phonestore.model.dto.TopProductDTO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO phục vụ Dashboard Thống kê và Báo cáo.
 * Sử dụng triệt để COALESCE(SUM(...), 0) để phòng ngừa NullPointerException khi chưa có dữ liệu.
 */
public class StatisticsDAO {

    /**
     * Lấy 4 chỉ số tổng quan hiển thị trên thẻ Dashboard.
     */
    public DashboardSummaryDTO getDashboardSummary() {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setMonthlyRevenue(BigDecimal.ZERO);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Tổng số mẫu điện thoại đang kinh doanh
            String sqlProducts = "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'";
            try (PreparedStatement ps = conn.prepareStatement(sqlProducts);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setTotalProducts(rs.getInt(1));
                }
            }

            // 2. Tổng số khách hàng
            String sqlCustomers = "SELECT COUNT(*) FROM customers";
            try (PreparedStatement ps = conn.prepareStatement(sqlCustomers);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setTotalCustomers(rs.getInt(1));
                }
            }

            // 3. Tổng nhân viên đang hoạt động
            String sqlEmployees = "SELECT COUNT(*) FROM employees WHERE status = 'ACTIVE'";
            try (PreparedStatement ps = conn.prepareStatement(sqlEmployees);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setTotalEmployees(rs.getInt(1));
                }
            }

            // 4. Tổng số hóa đơn bán ra đã hoàn tất
            String sqlInvoices = "SELECT COUNT(*) FROM invoices WHERE status = 'COMPLETED'";
            try (PreparedStatement ps = conn.prepareStatement(sqlInvoices);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setTotalInvoices(rs.getInt(1));
                }
            }

            // 5. Tổng doanh thu trong tháng hiện tại (COALESCE chống null)
            String sqlRevenue = "SELECT COALESCE(SUM(final_amount), 0) FROM invoices "
                    + "WHERE status = 'COMPLETED' "
                    + "AND MONTH(invoice_date) = MONTH(CURRENT_DATE()) "
                    + "AND YEAR(invoice_date) = YEAR(CURRENT_DATE())";
            try (PreparedStatement ps = conn.prepareStatement(sqlRevenue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal rev = rs.getBigDecimal(1);
                    dto.setMonthlyRevenue(rev != null ? rev : BigDecimal.ZERO);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return dto;
    }

    /**
     * Thống kê doanh thu và lợi nhuận ước tính theo ngày.
     */
    public List<RevenueStatDTO> getRevenueByDateRange(LocalDate fromDate, LocalDate toDate) {
        List<RevenueStatDTO> list = new ArrayList<>();
        String sql = "SELECT DATE(inv.invoice_date) AS stat_date, "
                + "COUNT(inv.invoice_id) AS total_invoices, "
                + "COALESCE(SUM(inv.final_amount), 0) AS total_revenue, "
                + "COALESCE(SUM(inv.final_amount - COALESCE(inv_cost.total_cost, 0)), 0) AS estimated_profit "
                + "FROM invoices inv "
                + "LEFT JOIN ("
                + "    SELECT invoice_id, SUM(id.quantity * p.import_price) AS total_cost "
                + "    FROM invoice_details id "
                + "    JOIN products p ON id.product_id = p.product_id "
                + "    GROUP BY invoice_id"
                + ") inv_cost ON inv.invoice_id = inv_cost.invoice_id "
                + "WHERE inv.status = 'COMPLETED' "
                + "AND DATE(inv.invoice_date) >= ? AND DATE(inv.invoice_date) <= ? "
                + "GROUP BY DATE(inv.invoice_date) "
                + "ORDER BY stat_date ASC";

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("stat_date");
                    String label = d != null ? d.toLocalDate().format(df) : "";
                    int count = rs.getInt("total_invoices");
                    BigDecimal revenue = rs.getBigDecimal("total_revenue");
                    BigDecimal profit = rs.getBigDecimal("estimated_profit");
                    list.add(new RevenueStatDTO(label, count, revenue != null ? revenue : BigDecimal.ZERO,
                            profit != null ? profit : BigDecimal.ZERO));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Thống kê doanh thu và lợi nhuận ước tính theo 12 tháng trong năm.
     * Đảm bảo luôn trả về đủ 12 tháng (kể cả những tháng chưa có đơn).
     */
    public List<RevenueStatDTO> getRevenueBy12Months(int year) {
        Map<Integer, RevenueStatDTO> monthMap = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            monthMap.put(m, new RevenueStatDTO("T" + m, 0, BigDecimal.ZERO, BigDecimal.ZERO));
        }

        String sql = "SELECT MONTH(inv.invoice_date) AS stat_month, "
                + "COUNT(inv.invoice_id) AS total_invoices, "
                + "COALESCE(SUM(inv.final_amount), 0) AS total_revenue, "
                + "COALESCE(SUM(inv.final_amount - COALESCE(inv_cost.total_cost, 0)), 0) AS estimated_profit "
                + "FROM invoices inv "
                + "LEFT JOIN ("
                + "    SELECT invoice_id, SUM(id.quantity * p.import_price) AS total_cost "
                + "    FROM invoice_details id "
                + "    JOIN products p ON id.product_id = p.product_id "
                + "    GROUP BY invoice_id"
                + ") inv_cost ON inv.invoice_id = inv_cost.invoice_id "
                + "WHERE inv.status = 'COMPLETED' AND YEAR(inv.invoice_date) = ? "
                + "GROUP BY MONTH(inv.invoice_date) "
                + "ORDER BY stat_month ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("stat_month");
                    int count = rs.getInt("total_invoices");
                    BigDecimal rev = rs.getBigDecimal("total_revenue");
                    BigDecimal profit = rs.getBigDecimal("estimated_profit");
                    if (monthMap.containsKey(month)) {
                        RevenueStatDTO dto = monthMap.get(month);
                        dto.setInvoiceCount(count);
                        dto.setTotalRevenue(rev != null ? rev : BigDecimal.ZERO);
                        dto.setEstimatedProfit(profit != null ? profit : BigDecimal.ZERO);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        List<RevenueStatDTO> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(monthMap.get(m));
        }
        return result;
    }

    /**
     * Thống kê Top N sản phẩm bán chạy nhất.
     */
    public List<TopProductDTO> getTopSellingProducts(int limit) {
        List<TopProductDTO> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_code, p.product_name, b.brand_name, "
                + "COALESCE(SUM(id.quantity), 0) AS total_sold, "
                + "COALESCE(SUM(id.subtotal), 0) AS total_revenue "
                + "FROM invoice_details id "
                + "JOIN invoices inv ON id.invoice_id = inv.invoice_id "
                + "JOIN products p ON id.product_id = p.product_id "
                + "JOIN brands b ON p.brand_id = b.brand_id "
                + "WHERE inv.status = 'COMPLETED' "
                + "GROUP BY p.product_id, p.product_code, p.product_name, b.brand_name "
                + "ORDER BY total_sold DESC, total_revenue DESC "
                + "LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 10);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductDTO dto = new TopProductDTO();
                    dto.setProductId(rs.getInt("product_id"));
                    dto.setProductCode(rs.getString("product_code"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setBrandName(rs.getString("brand_name"));
                    dto.setTotalSold(rs.getInt("total_sold"));
                    BigDecimal rev = rs.getBigDecimal("total_revenue");
                    dto.setTotalRevenue(rev != null ? rev : BigDecimal.ZERO);
                    list.add(dto);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Cảnh báo sản phẩm sắp hết hàng (stock_quantity <= threshold, mặc định 5).
     */
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, b.brand_name, c.category_name "
                + "FROM products p "
                + "JOIN brands b ON p.brand_id = b.brand_id "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.status = 'ACTIVE' AND p.stock_quantity <= ? "
                + "ORDER BY p.stock_quantity ASC, p.product_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    list.add(p);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
