package com.phonestore.view;

import com.phonestore.dao.ProductDAO;
import com.phonestore.model.Employee;
import com.phonestore.model.Product;
import com.phonestore.model.User;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.MessageUtils;
import com.phonestore.utils.SessionManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Cửa sổ chính sử dụng Sidebar + CardLayout.
 * Là khung tích hợp chung cho cả 3 thành viên.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final ProductDAO productDAO = new ProductDAO();
    private DefaultTableModel productTableModel;
    private JTextField txtSearchProduct;

    public MainFrame() {
        initComponents();
        loadProducts("");
    }

    private void initComponents() {
        setTitle("Phần mềm Quản lý Cửa hàng Điện thoại Di động - Mobile Store Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 740);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // 1. SIDEBAR MENU
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(new Color(30, 41, 59));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        User user = SessionManager.getCurrentUser();
        Employee emp = SessionManager.getCurrentEmployee();
        String displayName = emp != null ? emp.getFullName() : (user != null ? user.getUsername() : "User");
        String role = user != null ? user.getRole() : "STAFF";

        JLabel lblBrand = new JLabel("<html><center>MOBILE STORE<br><font size='3' color='#94a3b8'>"
                + displayName + " (" + role + ")</font></center></html>", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        sidebar.add(lblBrand, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 0, 10));
        menuPanel.setOpaque(false);

        JButton btnProducts = createMenuButton("1. Quản lý Điện thoại");
        JButton btnSales = createMenuButton("2. Bán hàng POS");
        JButton btnInvoices = createMenuButton("3. Quản lý Hóa đơn");
        JButton btnCustomers = createMenuButton("4. Quản lý Khách hàng");
        JButton btnPurchase = createMenuButton("5. Quản lý Nhập kho");
        JButton btnSuppliers = createMenuButton("6. Nhà cung cấp");
        JButton btnEmployees = createMenuButton("7. Quản lý Nhân viên");
        JButton btnStatistics = createMenuButton("8. Thống kê Doanh thu");

        // Phân quyền hiển thị theo Role
        if (!SessionManager.isAdmin()) {
            btnEmployees.setEnabled(false);
            btnEmployees.setToolTipText("Chỉ Admin mới có quyền quản lý Nhân viên & Tài khoản");
        }

        menuPanel.add(btnProducts);
        menuPanel.add(btnSales);
        menuPanel.add(btnInvoices);
        menuPanel.add(btnCustomers);
        menuPanel.add(btnPurchase);
        menuPanel.add(btnSuppliers);
        menuPanel.add(btnEmployees);
        menuPanel.add(btnStatistics);
        sidebar.add(menuPanel, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setBackground(new Color(220, 38, 38));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(0, 40));
        btnLogout.addActionListener(e -> {
            if (MessageUtils.showConfirm(this, "Bạn có chắc chắn muốn đăng xuất?")) {
                SessionManager.logout();
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        // 2. CONTENT PANEL (CardLayout cho 3 thành viên gắn JPanel vào)
        contentPanel.add(createProductOverviewPanel(), "PRODUCTS");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ BÁN HÀNG POS - THÀNH VIÊN 2 (SalesPanel)"), "SALES");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ LỊCH SỬ HÓA ĐƠN - THÀNH VIÊN 2 (InvoicePanel)"), "INVOICES");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ QUẢN LÝ KHÁCH HÀNG - THÀNH VIÊN 2 (CustomerPanel)"), "CUSTOMERS");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ QUẢN LÝ NHẬP HÀNG - THÀNH VIÊN 3 (PurchasePanel)"), "PURCHASE");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ NHÀ CUNG CẤP - THÀNH VIÊN 3 (SupplierPanel)"), "SUPPLIERS");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ QUẢN LÝ NHÂN VIÊN & TÀI KHOẢN - THÀNH VIÊN 1 (EmployeePanel)"), "EMPLOYEES");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ THỐNG KÊ & BÁO CÁO - THÀNH VIÊN 3 (StatisticsPanel)"), "STATISTICS");

        btnProducts.addActionListener(e -> {
            loadProducts(txtSearchProduct.getText());
            cardLayout.show(contentPanel, "PRODUCTS");
        });
        btnSales.addActionListener(e -> cardLayout.show(contentPanel, "SALES"));
        btnInvoices.addActionListener(e -> cardLayout.show(contentPanel, "INVOICES"));
        btnCustomers.addActionListener(e -> cardLayout.show(contentPanel, "CUSTOMERS"));
        btnPurchase.addActionListener(e -> cardLayout.show(contentPanel, "PURCHASE"));
        btnSuppliers.addActionListener(e -> cardLayout.show(contentPanel, "SUPPLIERS"));
        btnEmployees.addActionListener(e -> cardLayout.show(contentPanel, "EMPLOYEES"));
        btnStatistics.addActionListener(e -> cardLayout.show(contentPanel, "STATISTICS"));

        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(new Color(51, 65, 85));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private JPanel createProductOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        JLabel lblHeader = new JLabel("DANH SÁCH ĐIỆN THOẠI TRONG KHO");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(lblHeader, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        txtSearchProduct = new JTextField(22);
        JButton btnSearch = new JButton("Tìm kiếm");
        JButton btnReload = new JButton("Làm mới");
        btnSearch.addActionListener(e -> loadProducts(txtSearchProduct.getText()));
        btnReload.addActionListener(e -> {
            txtSearchProduct.setText("");
            loadProducts("");
        });
        searchBox.add(new JLabel("Từ khóa:"));
        searchBox.add(txtSearchProduct);
        searchBox.add(btnSearch);
        searchBox.add(btnReload);
        topBar.add(searchBox, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        String[] columns = {
            "ID", "Mã SP", "Tên Điện Thoại", "Thương Hiệu", "Danh Mục",
            "RAM", "ROM", "Màu Sắc", "Giá Nhập", "Giá Bán", "Tồn Kho", "Bảo Hành"
        };
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(productTableModel);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadProducts(String keyword) {
        productTableModel.setRowCount(0);
        List<Product> products = productDAO.searchProducts(keyword, null, null, false);
        for (Product p : products) {
            productTableModel.addRow(new Object[]{
                p.getProductId(),
                p.getProductCode(),
                p.getProductName(),
                p.getBrandName(),
                p.getCategoryName(),
                p.getRam(),
                p.getRom(),
                p.getColor(),
                CurrencyUtils.format(p.getImportPrice()),
                CurrencyUtils.format(p.getSellingPrice()),
                p.getStockQuantity(),
                p.getWarrantyMonths() + " tháng"
            });
        }
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(new Color(100, 116, 139));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
