package com.phonestore.view;

import com.phonestore.dao.PositionPermissionDAO;
import com.phonestore.model.Employee;
import com.phonestore.model.PositionPermission;
import com.phonestore.model.User;
import com.phonestore.utils.IconUtils;
import com.phonestore.utils.IconUtils.IconType;
import com.phonestore.utils.MessageUtils;
import com.phonestore.utils.SessionManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Cửa sổ Trang chủ sử dụng Sidebar có Icon trực quan + CardLayout.
 * Tích hợp đầy đủ các module của Thành viên 1 (Sản phẩm, Thương hiệu & Danh mục, Nhân viên & Tài khoản)
 * và Thành viên 3 (Nhập kho, Nhà cung cấp, Thống kê Doanh thu).
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final PositionPermissionDAO permissionDAO = new PositionPermissionDAO();

    // Các Panel của Thành viên 1
    private ProductPanel productPanel;
    private CategoryBrandPanel categoryBrandPanel;
    private EmployeePanel employeePanel;

    // Các Panel của Thành viên 3
    private PurchasePanel purchasePanel;
    private SupplierPanel supplierPanel;
    private StatisticsPanel statisticsPanel;

    private JButton btnProducts;
    private JButton btnCatBrand;
    private JButton btnSales;
    private JButton btnInvoices;
    private JButton btnCustomers;
    private JButton btnPurchase;
    private JButton btnSuppliers;
    private JButton btnEmployees;
    private JButton btnStatistics;

    public MainFrame() {
        initComponents();
        applyPositionPermissions();
    }

    private void initComponents() {
        setTitle("Phần mềm Quản lý Cửa hàng Điện thoại Di động");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 800);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // 1. SIDEBAR MENU VỚI ICON TRỰC QUAN
        JPanel sidebar = new JPanel(new BorderLayout(0, 12));
        sidebar.setPreferredSize(new Dimension(255, 0));
        sidebar.setBackground(new Color(30, 41, 59));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        User user = SessionManager.getCurrentUser();
        Employee emp = SessionManager.getCurrentEmployee();
        String fullName = emp != null ? emp.getFullName() : (user != null ? user.getUsername() : "");
        String position = emp != null ? emp.getPosition() : "";

        JLabel lblBrand = new JLabel("<html><center>CỬA HÀNG ĐIỆN THOẠI<br>"
                + "<font size='3' color='#38bdf8'>" + position + "</font><br>"
                + "<font size='3' color='#cbd5e1'>" + fullName + "</font>"
                + "</center></html>", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        sidebar.add(lblBrand, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(9, 1, 0, 8));
        menuPanel.setOpaque(false);

        btnProducts = createMenuButton("Quản lý Điện thoại", IconType.PHONE);
        btnCatBrand = createMenuButton("Thương hiệu & Danh mục", IconType.CATEGORY);
        btnSales = createMenuButton("Quản lý Bán hàng", IconType.CART);
        btnInvoices = createMenuButton("Quản lý Hóa đơn", IconType.INVOICE);
        btnCustomers = createMenuButton("Quản lý Khách hàng", IconType.CUSTOMER);
        btnPurchase = createMenuButton("Quản lý Nhập kho", IconType.WAREHOUSE);
        btnSuppliers = createMenuButton("Nhà cung cấp", IconType.SUPPLIER);
        btnEmployees = createMenuButton("Nhân viên & Tài khoản", IconType.EMPLOYEE);
        btnStatistics = createMenuButton("Thống kê Doanh thu", IconType.CHART);

        menuPanel.add(btnProducts);
        menuPanel.add(btnCatBrand);
        menuPanel.add(btnSales);
        menuPanel.add(btnInvoices);
        menuPanel.add(btnCustomers);
        menuPanel.add(btnPurchase);
        menuPanel.add(btnSuppliers);
        menuPanel.add(btnEmployees);
        menuPanel.add(btnStatistics);
        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Khu vực nút Đổi mật khẩu & Đăng xuất ở dưới cùng Sidebar
        JPanel bottomAccountPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        bottomAccountPanel.setOpaque(false);

        JButton btnChangePassword = new JButton("Đổi mật khẩu", IconUtils.createSidebarIcon(IconType.KEY, Color.WHITE));
        btnChangePassword.setIconTextGap(10);
        btnChangePassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChangePassword.setBackground(new Color(71, 85, 105));
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setPreferredSize(new Dimension(0, 36));
        btnChangePassword.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));

        JButton btnLogout = new JButton("Đăng xuất", IconUtils.createSidebarIcon(IconType.LOGOUT, Color.WHITE));
        btnLogout.setIconTextGap(10);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setBackground(new Color(220, 38, 38));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(0, 36));
        btnLogout.addActionListener(e -> {
            if (MessageUtils.showConfirm(this, "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?")) {
                SessionManager.logout();
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        bottomAccountPanel.add(btnChangePassword);
        bottomAccountPanel.add(btnLogout);
        sidebar.add(bottomAccountPanel, BorderLayout.SOUTH);

        // 2. CONTENT PANEL (CardLayout tích hợp TV1 & TV3)
        productPanel = new ProductPanel();
        categoryBrandPanel = new CategoryBrandPanel(() -> productPanel.refreshData());
        employeePanel = new EmployeePanel(this::applyPositionPermissions);

        purchasePanel = new PurchasePanel();
        supplierPanel = new SupplierPanel();
        statisticsPanel = new StatisticsPanel();

        contentPanel.add(productPanel, "PRODUCTS");
        contentPanel.add(categoryBrandPanel, "CAT_BRAND");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ QUẢN LÝ BÁN HÀNG"), "SALES");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ QUẢN LÝ HÓA ĐƠN"), "INVOICES");
        contentPanel.add(createPlaceholderPanel("PHÂN HỆ QUẢN LÝ KHÁCH HÀNG"), "CUSTOMERS");
        contentPanel.add(purchasePanel, "PURCHASE");
        contentPanel.add(supplierPanel, "SUPPLIERS");
        contentPanel.add(employeePanel, "EMPLOYEES");
        contentPanel.add(statisticsPanel, "STATISTICS");

        btnProducts.addActionListener(e -> {
            productPanel.refreshData();
            cardLayout.show(contentPanel, "PRODUCTS");
        });
        btnCatBrand.addActionListener(e -> {
            categoryBrandPanel.refreshData();
            cardLayout.show(contentPanel, "CAT_BRAND");
        });
        btnSales.addActionListener(e -> cardLayout.show(contentPanel, "SALES"));
        btnInvoices.addActionListener(e -> cardLayout.show(contentPanel, "INVOICES"));
        btnCustomers.addActionListener(e -> cardLayout.show(contentPanel, "CUSTOMERS"));
        btnPurchase.addActionListener(e -> {
            purchasePanel.reloadSuppliersAndProducts();
            cardLayout.show(contentPanel, "PURCHASE");
        });
        btnSuppliers.addActionListener(e -> {
            supplierPanel.loadData("");
            cardLayout.show(contentPanel, "SUPPLIERS");
        });
        btnEmployees.addActionListener(e -> {
            employeePanel.refreshData();
            cardLayout.show(contentPanel, "EMPLOYEES");
        });
        btnStatistics.addActionListener(e -> {
            statisticsPanel.loadAllData();
            cardLayout.show(contentPanel, "STATISTICS");
        });

        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    /**
     * Áp dụng phân quyền truy cập các module trên Sidebar dựa theo Chức vụ của người đang đăng nhập.
     */
    public final void applyPositionPermissions() {
        if (SessionManager.isAdmin()) {
            btnEmployees.setEnabled(true);
        }
        Employee emp = SessionManager.getCurrentEmployee();
        if (emp == null) {
            return;
        }
        PositionPermission perm = permissionDAO.findByPosition(emp.getPosition());
        if (perm == null) {
            return;
        }

        btnProducts.setEnabled(perm.isCanProducts());
        btnCatBrand.setEnabled(perm.isCanCatBrand());
        btnSales.setEnabled(perm.isCanSales());
        btnInvoices.setEnabled(perm.isCanInvoices());
        btnCustomers.setEnabled(perm.isCanCustomers());
        btnPurchase.setEnabled(perm.isCanPurchase());
        btnSuppliers.setEnabled(perm.isCanSuppliers());
        btnEmployees.setEnabled(SessionManager.isAdmin() || perm.isCanEmployees());
        btnStatistics.setEnabled(perm.isCanStatistics());
    }

    private JButton createMenuButton(String text, IconType iconType) {
        JButton btn = new JButton(text, IconUtils.createSidebarIcon(iconType, new Color(56, 189, 248)));
        btn.setIconTextGap(12);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(new Color(51, 65, 85));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
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
