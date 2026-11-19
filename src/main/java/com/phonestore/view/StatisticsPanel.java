package com.phonestore.view;

import com.phonestore.model.Product;
import com.phonestore.model.dto.DashboardSummaryDTO;
import com.phonestore.model.dto.RevenueStatDTO;
import com.phonestore.model.dto.TopProductDTO;
import com.phonestore.service.StatisticsService;
import com.phonestore.utils.CurrencyUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Dashboard Thống kê và Báo cáo (StatisticsPanel).
 * Thuộc Module 8 - Phân hệ TV3.
 * Gồm 4 Thẻ chỉ số tổng quan và JTabbedPane với biểu đồ trực quan.
 */
public class StatisticsPanel extends JPanel {

    private final StatisticsService statisticsService = new StatisticsService();

    // 4 Thẻ chỉ số tổng quan
    private JLabel lblCardProducts;
    private JLabel lblCardCustomers;
    private JLabel lblCardInvoices;
    private JLabel lblCardRevenue;

    // Tab Doanh thu theo Ngày
    private JTextField txtRevenueFromDate;
    private JTextField txtRevenueToDate;
    private JButton btnFilterDailyRevenue;
    private JTable tblDailyRevenue;
    private DefaultTableModel dailyRevenueModel;
    private JLabel lblDailySummary;

    // Tab Doanh thu theo 12 Tháng
    private JComboBox<Integer> cboYear;
    private JButton btnFilterYearlyRevenue;
    private JTable tblYearlyRevenue;
    private DefaultTableModel yearlyRevenueModel;
    private JLabel lblYearlySummary;
    private BarChartPanel barChartPanel;

    // Tab Top sản phẩm bán chạy
    private JComboBox<Integer> cboTopLimit;
    private JTable tblTopProducts;
    private DefaultTableModel topProductsModel;

    // Tab Cảnh báo tồn kho
    private JSpinner spnLowStockThreshold;
    private JTable tblLowStock;
    private DefaultTableModel lowStockModel;
    private JLabel lblLowStockCount;

    public StatisticsPanel() {
        initComponents();
        loadAllData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. HEADER & 4 THẺ CHỈ SỐ TỔNG QUAN
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));

        JPanel headerBar = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("DASHBOARD THỐNG KÊ & BÁO CÁO TỔNG HỢP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));

        JButton btnRefreshAll = new JButton("Làm mới Dashboard");
        btnRefreshAll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefreshAll.setBackground(new Color(59, 130, 246));
        btnRefreshAll.setForeground(Color.WHITE);
        btnRefreshAll.addActionListener(e -> loadAllData());

        headerBar.add(lblTitle, BorderLayout.WEST);
        headerBar.add(btnRefreshAll, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // 4 Thẻ chỉ số
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setPreferredSize(new Dimension(0, 95));

        lblCardProducts = new JLabel("0", SwingConstants.CENTER);
        lblCardCustomers = new JLabel("0", SwingConstants.CENTER);
        lblCardInvoices = new JLabel("0", SwingConstants.CENTER);
        lblCardRevenue = new JLabel("0 đ", SwingConstants.CENTER);

        cardsPanel.add(createCard("1. ĐIỆN THOẠI ĐANG BÁN", lblCardProducts, new Color(14, 165, 233), new Color(240, 249, 255)));
        cardsPanel.add(createCard("2. TỔNG KHÁCH HÀNG", lblCardCustomers, new Color(99, 102, 241), new Color(238, 242, 255)));
        cardsPanel.add(createCard("3. TỔNG HÓA ĐƠN ĐÃ BÁN", lblCardInvoices, new Color(245, 158, 11), new Color(254, 252, 232)));
        cardsPanel.add(createCard("4. DOANH THU THÁNG NÀY", lblCardRevenue, new Color(16, 185, 129), new Color(236, 253, 245)));

        topContainer.add(cardsPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // 2. JTABBEDPANE CHI TIẾT PHÍA DƯỚI
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Thống kê Doanh thu & Lợi nhuận", createRevenueTab());
        tabbedPane.addTab("Top Sản phẩm Bán chạy", createTopProductsTab());
        tabbedPane.addTab("Cảnh báo Tồn kho thấp (<= 5)", createLowStockTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor, Color bgColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(accentColor.darker());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor.darker());
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    // --- TAB 1: DOANH THU & LỢI NHUẬN ---
    private JPanel createRevenueTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Nửa trái: Doanh thu theo Ngày
        JPanel leftDaily = new JPanel(new BorderLayout(5, 5));
        leftDaily.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Doanh thu & Lợi nhuận theo Ngày (Lọc khoảng ngày)", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)));

        JPanel dailyFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        LocalDate now = LocalDate.now();
        txtRevenueFromDate = new JTextField(now.minusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 8);
        txtRevenueToDate = new JTextField(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 8);
        btnFilterDailyRevenue = new JButton("Lọc");
        btnFilterDailyRevenue.addActionListener(e -> loadDailyRevenue());

        dailyFilter.add(new JLabel("Từ:"));
        dailyFilter.add(txtRevenueFromDate);
        dailyFilter.add(new JLabel("Đến:"));
        dailyFilter.add(txtRevenueToDate);
        dailyFilter.add(btnFilterDailyRevenue);
        leftDaily.add(dailyFilter, BorderLayout.NORTH);

        String[] dailyCols = {"Ngày", "Số HĐ", "Doanh Thu", "LN Ước Tính"};
        dailyRevenueModel = new DefaultTableModel(dailyCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblDailyRevenue = new JTable(dailyRevenueModel);
        tblDailyRevenue.setRowHeight(26);
        tblDailyRevenue.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        leftDaily.add(new JScrollPane(tblDailyRevenue), BorderLayout.CENTER);

        lblDailySummary = new JLabel("Tổng doanh thu: 0 đ | Lợi nhuận ước tính: 0 đ");
        lblDailySummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDailySummary.setForeground(new Color(220, 38, 38));
        lblDailySummary.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        leftDaily.add(lblDailySummary, BorderLayout.SOUTH);

        // Nửa phải: Doanh thu 12 Tháng + Biểu đồ trực quan
        JPanel rightYearly = new JPanel(new BorderLayout(5, 5));
        rightYearly.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Doanh thu 12 Tháng trong Năm & Biểu đồ trực quan", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)));

        JPanel yearlyFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        cboYear = new JComboBox<>(new Integer[]{now.getYear(), now.getYear() - 1, now.getYear() - 2});
        btnFilterYearlyRevenue = new JButton("Xem");
        btnFilterYearlyRevenue.addActionListener(e -> loadYearlyRevenue());

        yearlyFilter.add(new JLabel("Chọn năm:"));
        yearlyFilter.add(cboYear);
        yearlyFilter.add(btnFilterYearlyRevenue);
        rightYearly.add(yearlyFilter, BorderLayout.NORTH);

        String[] yearlyCols = {"Tháng", "Số HĐ", "Doanh Thu", "LN Ước Tính"};
        yearlyRevenueModel = new DefaultTableModel(yearlyCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblYearlyRevenue = new JTable(yearlyRevenueModel);
        tblYearlyRevenue.setRowHeight(24);
        tblYearlyRevenue.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollTableYear = new JScrollPane(tblYearlyRevenue);
        scrollTableYear.setPreferredSize(new Dimension(0, 160));

        // Biểu đồ trực quan cột
        barChartPanel = new BarChartPanel();
        barChartPanel.setPreferredSize(new Dimension(0, 160));

        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTableYear, barChartPanel);
        splitRight.setResizeWeight(0.5);
        splitRight.setDividerSize(4);
        rightYearly.add(splitRight, BorderLayout.CENTER);

        lblYearlySummary = new JLabel("Tổng cả năm: 0 đ | Lợi nhuận: 0 đ");
        lblYearlySummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblYearlySummary.setForeground(new Color(220, 38, 38));
        lblYearlySummary.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        rightYearly.add(lblYearlySummary, BorderLayout.SOUTH);

        panel.add(leftDaily);
        panel.add(rightYearly);
        return panel;
    }

    // --- TAB 2: TOP SẢN PHẨM BÁN CHẠY ---
    private JPanel createTopProductsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        cboTopLimit = new JComboBox<>(new Integer[]{5, 10, 20});
        cboTopLimit.setSelectedItem(10);
        cboTopLimit.addActionListener(e -> loadTopProducts());

        topBar.add(new JLabel("Hiển thị: Top"));
        topBar.add(cboTopLimit);
        topBar.add(new JLabel("sản phẩm có doanh số bán ra cao nhất"));
        panel.add(topBar, BorderLayout.NORTH);

        String[] columns = {"Hạng", "Mã SP", "Tên Điện Thoại", "Thương Hiệu", "Số Lượng Đã Bán", "Tổng Doanh Thu"};
        topProductsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblTopProducts = new JTable(topProductsModel);
        tblTopProducts.setRowHeight(28);
        tblTopProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(new JScrollPane(tblTopProducts), BorderLayout.CENTER);

        return panel;
    }

    // --- TAB 3: CẢNH BÁO TỒN KHO THẤP ---
    private JPanel createLowStockTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        spnLowStockThreshold = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        spnLowStockThreshold.setPreferredSize(new Dimension(65, 28));
        spnLowStockThreshold.addChangeListener(e -> loadLowStockProducts());

        JButton btnReload = new JButton("Quét lại tồn kho");
        btnReload.addActionListener(e -> loadLowStockProducts());

        lblLowStockCount = new JLabel("Đang có 0 sản phẩm có tồn kho thấp cần nhập thêm!");
        lblLowStockCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLowStockCount.setForeground(new Color(220, 38, 38));

        topBar.add(new JLabel("Ngưỡng cảnh báo tồn kho <="));
        topBar.add(spnLowStockThreshold);
        topBar.add(new JLabel("máy"));
        topBar.add(btnReload);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(lblLowStockCount);
        panel.add(topBar, BorderLayout.NORTH);

        String[] columns = {"Mã SP", "Tên Điện Thoại", "Thương Hiệu", "Danh Mục", "Tồn Kho Hiện Tại", "Giá Nhập", "Giá Bán"};
        lowStockModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblLowStock = new JTable(lowStockModel);
        tblLowStock.setRowHeight(28);
        tblLowStock.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tô đỏ nổi bật số lượng tồn kho
        tblLowStock.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(new Color(220, 38, 38));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        });

        panel.add(new JScrollPane(tblLowStock), BorderLayout.CENTER);

        return panel;
    }

    public void loadAllData() {
        loadDashboardCards();
        loadDailyRevenue();
        loadYearlyRevenue();
        loadTopProducts();
        loadLowStockProducts();
    }

    private void loadDashboardCards() {
        DashboardSummaryDTO summary = statisticsService.getDashboardSummary();
        lblCardProducts.setText(String.valueOf(summary.getTotalProducts()));
        lblCardCustomers.setText(String.valueOf(summary.getTotalCustomers()));
        lblCardInvoices.setText(String.valueOf(summary.getTotalInvoices()));
        lblCardRevenue.setText(CurrencyUtils.format(summary.getMonthlyRevenue()));
    }

    private void loadDailyRevenue() {
        dailyRevenueModel.setRowCount(0);
        LocalDate fromDate = parseDate(txtRevenueFromDate.getText().trim());
        LocalDate toDate = parseDate(txtRevenueToDate.getText().trim());

        List<RevenueStatDTO> list = statisticsService.getRevenueByDateRange(fromDate, toDate);
        BigDecimal totalRev = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalInv = 0;
        for (RevenueStatDTO dto : list) {
            dailyRevenueModel.addRow(new Object[]{
                dto.getPeriodLabel(),
                dto.getInvoiceCount(),
                CurrencyUtils.format(dto.getTotalRevenue()),
                CurrencyUtils.format(dto.getEstimatedProfit())
            });
            totalRev = totalRev.add(dto.getTotalRevenue());
            totalProfit = totalProfit.add(dto.getEstimatedProfit());
            totalInv += dto.getInvoiceCount();
        }
        lblDailySummary.setText("Tổng: " + totalInv + " HĐ | Doanh thu: " + CurrencyUtils.format(totalRev)
                + " | Lợi nhuận ước tính: " + CurrencyUtils.format(totalProfit));
    }

    private void loadYearlyRevenue() {
        yearlyRevenueModel.setRowCount(0);
        Integer year = (Integer) cboYear.getSelectedItem();
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        List<RevenueStatDTO> list = statisticsService.getRevenueBy12Months(year);
        BigDecimal totalRev = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalInv = 0;
        for (RevenueStatDTO dto : list) {
            yearlyRevenueModel.addRow(new Object[]{
                dto.getPeriodLabel(),
                dto.getInvoiceCount(),
                CurrencyUtils.format(dto.getTotalRevenue()),
                CurrencyUtils.format(dto.getEstimatedProfit())
            });
            totalRev = totalRev.add(dto.getTotalRevenue());
            totalProfit = totalProfit.add(dto.getEstimatedProfit());
            totalInv += dto.getInvoiceCount();
        }
        lblYearlySummary.setText("Năm " + year + ": " + totalInv + " HĐ | Tổng thu: " + CurrencyUtils.format(totalRev)
                + " | LN: " + CurrencyUtils.format(totalProfit));

        barChartPanel.setData(list);
    }

    private void loadTopProducts() {
        topProductsModel.setRowCount(0);
        Integer limit = (Integer) cboTopLimit.getSelectedItem();
        if (limit == null) {
            limit = 10;
        }
        List<TopProductDTO> list = statisticsService.getTopSellingProducts(limit);
        int rank = 1;
        for (TopProductDTO p : list) {
            topProductsModel.addRow(new Object[]{
                "#" + (rank++),
                p.getProductCode(),
                p.getProductName(),
                p.getBrandName(),
                p.getTotalSold() + " máy",
                CurrencyUtils.format(p.getTotalRevenue())
            });
        }
    }

    public void loadLowStockProducts() {
        lowStockModel.setRowCount(0);
        int threshold = (int) spnLowStockThreshold.getValue();
        List<Product> list = statisticsService.getLowStockProducts(threshold);
        for (Product p : list) {
            lowStockModel.addRow(new Object[]{
                p.getProductCode(),
                p.getProductName(),
                p.getBrandName(),
                p.getCategoryName(),
                p.getStockQuantity(),
                CurrencyUtils.format(p.getImportPrice()),
                CurrencyUtils.format(p.getSellingPrice())
            });
        }
        lblLowStockCount.setText("Đang có " + list.size() + " sản phẩm có tồn kho <= " + threshold + " máy cần nhập thêm!");
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Component vẽ Biểu đồ cột trực quan (Bar Chart) doanh thu 12 tháng bằng Graphics2D.
     */
    static class BarChartPanel extends JPanel {
        private List<RevenueStatDTO> data = new ArrayList<>();

        public BarChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    "Biểu đồ Doanh thu 12 Tháng (VNĐ)", 0, 0, new Font("Segoe UI", Font.BOLD, 11), new Color(100, 116, 139)));
        }

        public void setData(List<RevenueStatDTO> data) {
            this.data = data != null ? data : new ArrayList<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padLeft = 45;
            int padRight = 20;
            int padTop = 25;
            int padBottom = 30;

            int chartWidth = width - padLeft - padRight;
            int chartHeight = height - padTop - padBottom;
            if (chartWidth <= 0 || chartHeight <= 0) {
                g2.dispose();
                return;
            }

            // Tìm giá trị max doanh thu
            BigDecimal maxVal = BigDecimal.ZERO;
            for (RevenueStatDTO dto : data) {
                if (dto.getTotalRevenue().compareTo(maxVal) > 0) {
                    maxVal = dto.getTotalRevenue();
                }
            }
            if (maxVal.compareTo(BigDecimal.ZERO) == 0) {
                maxVal = BigDecimal.valueOf(10000000); // 10 triệu default scale
            }

            // Vẽ trục tọa độ X và Y
            g2.setColor(new Color(203, 213, 225));
            g2.drawLine(padLeft, height - padBottom, width - padRight, height - padBottom);
            g2.drawLine(padLeft, padTop, padLeft, height - padBottom);

            // Vẽ các cột
            int n = data.size();
            int barWidth = Math.max(12, (chartWidth / n) - 8);
            int slotWidth = chartWidth / n;

            Font fontLabel = new Font("Segoe UI", Font.PLAIN, 10);
            FontMetrics fm = g2.getFontMetrics(fontLabel);
            g2.setFont(fontLabel);

            for (int i = 0; i < n; i++) {
                RevenueStatDTO dto = data.get(i);
                int x = padLeft + (i * slotWidth) + ((slotWidth - barWidth) / 2);

                double ratio = dto.getTotalRevenue().doubleValue() / maxVal.doubleValue();
                int barHeight = (int) (ratio * (chartHeight - 15));

                int y = height - padBottom - barHeight;

                // Màu cột gradient xanh dương đẹp mắt
                if (dto.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
                    g2.setColor(new Color(59, 130, 246));
                } else {
                    g2.setColor(new Color(226, 232, 240));
                }
                g2.fillRoundRect(x, y, barWidth, barHeight, 4, 4);

                // Nhãn tháng (T1 -> T12)
                g2.setColor(new Color(71, 85, 105));
                String label = dto.getPeriodLabel();
                int lblWidth = fm.stringWidth(label);
                g2.drawString(label, x + (barWidth - lblWidth) / 2, height - padBottom + 14);
            }

            g2.dispose();
        }
    }
}
