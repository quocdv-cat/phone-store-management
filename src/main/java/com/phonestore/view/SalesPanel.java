package com.phonestore.view;

import com.phonestore.dao.ProductDAO;
import com.phonestore.model.Customer;
import com.phonestore.model.Employee;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import com.phonestore.model.Product;
import com.phonestore.service.CustomerService;
import com.phonestore.service.InvoiceService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.MessageUtils;
import com.phonestore.utils.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Giao dien Ban hang POS tai quay (SalesPanel) - Chuc nang nghiep vu trong tam cua he thong.
 * Phu trach boi Thanh vien 2 (Trung - Core Transaction Developer).
 * Bo cuc 2 cot:
 * - Cot trai: Danh sach san pham trong kho (chi hien thi status = 'ACTIVE' va stock_quantity > 0)
 * - Cot phai: Thong tin khach hang, Gio hang, Chiet khau, Tong tien & Nut Thanh toan (JDBC Transaction).
 */
public class SalesPanel extends JPanel {

    private final ProductDAO productDAO;
    private final CustomerService customerService;
    private final InvoiceService invoiceService;

    // LEFT: Product Catalog
    private DefaultTableModel productTableModel;
    private JTable productTable;
    private JTextField txtSearchProduct;
    private List<Product> availableProducts;

    // RIGHT: Customer Info
    private JTextField txtCustomerPhone;
    private JLabel lblCustomerName;
    private JLabel lblCustomerDetail;
    private Customer selectedCustomer;

    // RIGHT: Cart Table & Cart Items
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private final List<CartItem> cartItems = new ArrayList<>();

    // RIGHT: Payment calculations
    private JLabel lblTotalAmount;
    private JTextField txtDiscountPercent;
    private JLabel lblDiscountValue;
    private JLabel lblFinalAmount;
    private JTextField txtCashGiven;
    private JLabel lblChangeAmount;
    private JTextField txtNote;
    private JButton btnCheckout;

    public SalesPanel() {
        this.productDAO = new ProductDAO();
        this.customerService = new CustomerService();
        this.invoiceService = new InvoiceService();

        initComponents();
        loadAvailableProducts("");
        clearSelectedCustomer();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblHeader = new JLabel("QUẢN LÝ BÁN HÀNG TẠI QUẦY");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel("Th\u1ef1c hi\u1ec7n l\u1eadp h\u00f3a \u0111\u01a1n, tr\u1eeb t\u1ed3n kho an to\u00e0n b\u1eb1ng JDBC Transaction v\u00e0 in phi\u1ebfu b\u00e1n l\u1ebb");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        headerPanel.add(lblHeader, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(headerPanel, BorderLayout.NORTH);

        // Main 2-column split pane
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createProductCatalogPanel(), createCartAndCheckoutPanel());
        mainSplit.setResizeWeight(0.50);
        mainSplit.setDividerSize(6);
        add(mainSplit, BorderLayout.CENTER);
    }

    // =========================================================================
    // COT TRAI: DANH SACH SAN PHAM TRONG KHO (status = 'ACTIVE' & stock > 0)
    // =========================================================================
    private JPanel createProductCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "1. Danh S\u00e1ch \u0110i\u1ec7n Tho\u1ea1i Trong Kho (C\u00f2n H\u00e0ng)",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        txtSearchProduct = new JTextField(18);
        txtSearchProduct.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchProduct.putClientProperty("JTextField.placeholderText", "T\u00ecm theo t\u00ean, m\u00e3, h\u00e3ng...");

        JButton btnSearch = new JButton("T\u00ecm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(new Color(15, 118, 110));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);

        JButton btnRefresh = new JButton("L\u00e0m m\u1edbi");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setFocusPainted(false);

        JButton btnAddToCart = new JButton("Th\u00eam v\u00e0o gi\u1ecf");
        btnAddToCart.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddToCart.setBackground(new Color(37, 99, 235));
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFocusPainted(false);

        searchBar.add(new JLabel("T\u00ecm SP:"));
        searchBar.add(txtSearchProduct);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);
        searchBar.add(btnAddToCart);
        panel.add(searchBar, BorderLayout.NORTH);

        // Products Table
        String[] columns = {"M\u00e3 SP", "T\u00ean \u0110i\u1ec7n Tho\u1ea1i", "Th\u01b0\u01a1ng Hi\u1ec7u", "M\u00e0u S\u1eafc", "\u0110\u01a1n Gi\u00e1 B\u00e1n", "T\u1ed3n Kho"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setRowHeight(28);
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        productTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Events
        btnSearch.addActionListener(e -> loadAvailableProducts(txtSearchProduct.getText()));
        btnRefresh.addActionListener(e -> {
            txtSearchProduct.setText("");
            loadAvailableProducts("");
        });
        txtSearchProduct.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadAvailableProducts(txtSearchProduct.getText());
                }
            }
        });

        // Double-click to add to cart
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addProductToCart();
                }
            }
        });
        btnAddToCart.addActionListener(e -> addProductToCart());

        return panel;
    }

    // =========================================================================
    // COT PHAI: THONG TIN KHACH HANG, GIO HANG & THANH TOAN (POS)
    // =========================================================================
    private JPanel createCartAndCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));

        // 1. Khung thong tin khach hang
        JPanel customerBox = new JPanel(new BorderLayout(5, 5));
        customerBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "2. Th\u00f4ng Tin Kh\u00e1ch H\u00e0ng",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        JPanel custSearchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        txtCustomerPhone = new JTextField(12);
        txtCustomerPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCustomerPhone.putClientProperty("JTextField.placeholderText", "Nh\u1eadp S\u0110T kh\u00e1ch...");

        JButton btnFindCust = new JButton("T\u00ecm S\u0110T");
        btnFindCust.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnQuickCust = new JButton("T\u1ea1o Nhanh");
        btnQuickCust.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickCust.setBackground(new Color(16, 185, 129));
        btnQuickCust.setForeground(Color.WHITE);
        btnQuickCust.setFocusPainted(false);

        JButton btnClearCust = new JButton("B\u1ecf ch\u1ecdn");
        btnClearCust.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        custSearchRow.add(new JLabel("S\u0110T:"));
        custSearchRow.add(txtCustomerPhone);
        custSearchRow.add(btnFindCust);
        custSearchRow.add(btnQuickCust);
        custSearchRow.add(btnClearCust);
        customerBox.add(custSearchRow, BorderLayout.NORTH);

        JPanel custDisplay = new JPanel(new GridLayout(2, 1, 0, 2));
        custDisplay.setBorder(BorderFactory.createEmptyBorder(2, 8, 4, 8));
        lblCustomerName = new JLabel("Kh\u00e1ch h\u00e0ng: Ch\u01b0a ch\u1ecdn kh\u00e1ch h\u00e0ng");
        lblCustomerName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCustomerName.setForeground(new Color(220, 38, 38));

        lblCustomerDetail = new JLabel("Vui l\u00f2ng nh\u1eadp S\u0110T \u0111\u1ec3 t\u00ecm ho\u1eb7c b\u1ea5m T\u1ea1o Nhanh kh\u00e1ch h\u00e0ng m\u1edbi");
        lblCustomerDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCustomerDetail.setForeground(new Color(100, 116, 139));

        custDisplay.add(lblCustomerName);
        custDisplay.add(lblCustomerDetail);
        customerBox.add(custDisplay, BorderLayout.CENTER);

        // 2. Khung Gio hang (Cart Table) voi nut +/- inline
        JPanel cartBox = new JPanel(new BorderLayout(0, 6));
        cartBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "3. Gi\u1ecf H\u00e0ng H\u00f3a \u0110\u01a1n",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        String[] cartCols = {"M\u00e3 SP", "T\u00ean S\u1ea3n Ph\u1ea9m", "M\u00e0u S\u1eafc", "\u0110\u01a1n Gi\u00e1", "S\u1ed1 L\u01b0\u1ee3ng", "Th\u00e0nh Ti\u1ec1n"};
        cartTableModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 4; // Chi cho phep tuong tac o cot "So Luong" (index 4)
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(28);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(SwingConstants.RIGHT);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRender);
        cartTable.getColumnModel().getColumn(5).setCellRenderer(rightRender);

        // Dat custom renderer va editor cho cot SL (index 4) - hien thi nut [-] [so luong] [+]
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(4).setCellRenderer(new QuantityButtonRenderer());
        cartTable.getColumnModel().getColumn(4).setCellEditor(new QuantityButtonEditor());
        cartTable.getColumnModel().getColumn(4).setMinWidth(75);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(100);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(95);

        cartBox.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Cart Action Buttons - chi giu "Lam Trong Gio"
        JPanel cartBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton btnClearCart = new JButton("L\u00e0m Tr\u1ed1ng Gi\u1ecf");
        btnClearCart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cartBtns.add(btnClearCart);
        cartBox.add(cartBtns, BorderLayout.SOUTH);

        // 3. Khung Tinh toan & Nut Thanh Toan
        JPanel paymentBox = new JPanel(new GridBagLayout());
        paymentBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "4. Thanh To\u00e1n & Ho\u00e0n T\u1ea5t",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Tong tien hang
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel lblTotalTitle = new JLabel("T\u1ed5ng ti\u1ec1n h\u00e0ng:");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        paymentBox.add(lblTotalTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        lblTotalAmount = new JLabel("0 \u0111", SwingConstants.RIGHT);
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        paymentBox.add(lblTotalAmount, gbc);

        // Row 1: Giam gia (%)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        JLabel lblDiscountTitle = new JLabel("Gi\u1ea3m gi\u00e1 (%):");
        lblDiscountTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentBox.add(lblDiscountTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        JPanel discountRow = new JPanel(new BorderLayout(4, 0));
        txtDiscountPercent = new JTextField("0");
        txtDiscountPercent.setHorizontalAlignment(JTextField.RIGHT);
        txtDiscountPercent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtDiscountPercent.setPreferredSize(new Dimension(60, 28));
        JLabel lblPercent = new JLabel("%");
        lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        discountRow.add(txtDiscountPercent, BorderLayout.CENTER);
        discountRow.add(lblPercent, BorderLayout.EAST);
        paymentBox.add(discountRow, gbc);

        // Row 2: Gia tri giam gia (hien thi so tien tuong ung)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        JLabel lblDiscountValTitle = new JLabel("S\u1ed1 ti\u1ec1n gi\u1ea3m:");
        lblDiscountValTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiscountValTitle.setForeground(new Color(100, 116, 139));
        paymentBox.add(lblDiscountValTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        lblDiscountValue = new JLabel("-0 \u0111", SwingConstants.RIGHT);
        lblDiscountValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiscountValue.setForeground(new Color(100, 116, 139));
        paymentBox.add(lblDiscountValue, gbc);

        // Row 3: Khach phai tra (Final Amount)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        JLabel lblFinalTitle = new JLabel("KH\u00c1CH PH\u1ea2I TR\u1ea2:");
        lblFinalTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFinalTitle.setForeground(new Color(220, 38, 38));
        paymentBox.add(lblFinalTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        lblFinalAmount = new JLabel("0 \u0111", SwingConstants.RIGHT);
        lblFinalAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFinalAmount.setForeground(new Color(220, 38, 38));
        paymentBox.add(lblFinalAmount, gbc);

        // Row 4: Tien khach dua
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        JLabel lblCashTitle = new JLabel("Ti\u1ec1n kh\u00e1ch \u0111\u01b0a:");
        lblCashTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentBox.add(lblCashTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtCashGiven = new JTextField();
        txtCashGiven.setHorizontalAlignment(JTextField.RIGHT);
        txtCashGiven.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentBox.add(txtCashGiven, gbc);

        // Row 5: Tien thua tra khach
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.35;
        JLabel lblChangeTitle = new JLabel("Ti\u1ec1n th\u1eeba tr\u1ea3 kh\u00e1ch:");
        lblChangeTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentBox.add(lblChangeTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        lblChangeAmount = new JLabel("0 \u0111", SwingConstants.RIGHT);
        lblChangeAmount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChangeAmount.setForeground(new Color(16, 185, 129));
        paymentBox.add(lblChangeAmount, gbc);

        // Row 6: Ghi chu
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.35;
        JLabel lblNoteTitle = new JLabel("Ghi ch\u00fa \u0111\u01a1n:");
        lblNoteTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentBox.add(lblNoteTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtNote = new JTextField();
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        paymentBox.add(txtNote, gbc);

        // Row 7: Nut Thanh toan to, noi bat
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 8, 8, 8);
        btnCheckout = new JButton("THANH TO\u00c1N & IN H\u00d3A \u0110\u01a0N");
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCheckout.setBackground(new Color(16, 185, 129));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setPreferredSize(new Dimension(0, 45));
        btnCheckout.setFocusPainted(false);
        paymentBox.add(btnCheckout, gbc);

        // Ghep cac phan cot phai
        JPanel rightTop = new JPanel(new BorderLayout(0, 6));
        rightTop.add(customerBox, BorderLayout.NORTH);
        rightTop.add(cartBox, BorderLayout.CENTER);

        panel.add(rightTop, BorderLayout.CENTER);
        panel.add(paymentBox, BorderLayout.SOUTH);

        // Events
        btnFindCust.addActionListener(e -> findCustomerByPhone());
        txtCustomerPhone.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    findCustomerByPhone();
                }
            }
        });
        btnQuickCust.addActionListener(e -> quickCreateCustomer());
        btnClearCust.addActionListener(e -> clearSelectedCustomer());

        btnClearCart.addActionListener(e -> clearCart());

        txtDiscountPercent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                recalculateTotals();
            }
        });

        txtCashGiven.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateChange();
            }
        });

        btnCheckout.addActionListener(e -> processCheckout());

        return panel;
    }

    // =========================================================================
    // XU LY DU LIEU & NGHIEP VU POS
    // =========================================================================

    public void loadAvailableProducts(String keyword) {
        productTableModel.setRowCount(0);
        availableProducts = productDAO.searchProducts(keyword, null, null, true);

        for (Product p : availableProducts) {
            productTableModel.addRow(new Object[]{
                p.getProductCode(),
                p.getProductName(),
                p.getBrandName(),
                p.getColor(),
                CurrencyUtils.format(p.getSellingPrice()),
                p.getStockQuantity()
            });
        }
    }

    private void addProductToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0 || availableProducts == null || row >= availableProducts.size()) {
            MessageUtils.showWarning(this, "Vui l\u00f2ng ch\u1ecdn m\u1ed9t s\u1ea3n ph\u1ea9m t\u1eeb danh s\u00e1ch b\u00ean tr\u00e1i \u0111\u1ec3 th\u00eam v\u00e0o gi\u1ecf!");
            return;
        }

        Product selected = availableProducts.get(row);

        CartItem existing = null;
        for (CartItem item : cartItems) {
            if (item.product.getProductId() == selected.getProductId()) {
                existing = item;
                break;
            }
        }

        if (existing != null) {
            if (existing.quantity + 1 > selected.getStockQuantity()) {
                MessageUtils.showWarning(this, "Kh\u00f4ng th\u1ec3 th\u00eam! S\u1ed1 l\u01b0\u1ee3ng trong gi\u1ecf (" + (existing.quantity + 1)
                        + ") s\u1ebd v\u01b0\u1ee3t qu\u00e1 s\u1ed1 l\u01b0\u1ee3ng t\u1ed3n kho hi\u1ec7n c\u00f3 (" + selected.getStockQuantity() + ")!");
                return;
            }
            existing.quantity++;
        } else {
            if (selected.getStockQuantity() < 1) {
                MessageUtils.showWarning(this, "S\u1ea3n ph\u1ea9m n\u00e0y hi\u1ec7n \u0111\u00e3 h\u1ebft h\u00e0ng trong kho!");
                return;
            }
            cartItems.add(new CartItem(selected, 1, selected.getSellingPrice()));
        }

        renderCartTable();
    }

    /**
     * Tang hoac giam so luong cua mot dong trong gio hang.
     * delta = +1 de tang, -1 de giam.
     * Neu SL = 1 va giam -> xoa khoi gio.
     */
    void adjustCartQuantity(int cartRowIndex, int delta) {
        if (cartRowIndex < 0 || cartRowIndex >= cartItems.size()) return;

        CartItem item = cartItems.get(cartRowIndex);
        int newQty = item.quantity + delta;

        if (newQty <= 0) {
            // Khi SL = 1 va nhan "-" (hien la "x") -> xoa truc tiep khoi gio, khong hoi
            cartItems.remove(cartRowIndex);
        } else {
            if (newQty > item.product.getStockQuantity()) {
                MessageUtils.showWarning(this, "KH\u00d4NG \u0110\u1ee6 T\u1ed2N KHO!\nS\u1ea3n ph\u1ea9m '" + item.product.getProductName()
                        + "' ch\u1ec9 c\u00f2n t\u1ed3n kho " + item.product.getStockQuantity() + " m\u00e1y.");
                return;
            }
            item.quantity = newQty;
        }

        renderCartTable();
    }

    private void clearCart() {
        if (cartItems.isEmpty()) return;
        if (MessageUtils.showConfirm(this, "B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n l\u00e0m tr\u1ed1ng to\u00e0n b\u1ed9 gi\u1ecf h\u00e0ng?")) {
            cartItems.clear();
            renderCartTable();
        }
    }

    private void renderCartTable() {
        // Dung editor truoc khi thay doi model de tranh loi
        if (cartTable.isEditing()) {
            cartTable.getCellEditor().cancelCellEditing();
        }
        cartTableModel.setRowCount(0);
        for (CartItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                item.product.getProductCode(),
                item.product.getProductName(),
                item.product.getColor() != null ? item.product.getColor() : "-",
                CurrencyUtils.format(item.unitPrice),
                item.quantity,
                CurrencyUtils.format(item.getSubtotal())
            });
        }
        recalculateTotals();
    }

    /**
     * Tinh tong tien, ap dung giam gia theo %, cap nhat hien thi.
     */
    private void recalculateTotals() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }

        lblTotalAmount.setText(CurrencyUtils.format(total));

        // Tinh giam gia theo %
        BigDecimal discountPercent = BigDecimal.ZERO;
        try {
            String percentStr = txtDiscountPercent.getText().trim().replaceAll("[^0-9.]", "");
            if (!percentStr.isEmpty()) {
                discountPercent = new BigDecimal(percentStr);
            }
        } catch (Exception ignored) {
        }

        // Gioi han 0-100%
        if (discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            discountPercent = BigDecimal.valueOf(100);
            txtDiscountPercent.setText("100");
        }
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0) {
            discountPercent = BigDecimal.ZERO;
            txtDiscountPercent.setText("0");
        }

        BigDecimal discountAmount = total.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        lblDiscountValue.setText("-" + CurrencyUtils.format(discountAmount));

        BigDecimal finalAmount = total.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }
        lblFinalAmount.setText(CurrencyUtils.format(finalAmount));

        calculateChange();
    }

    /**
     * Lay so tien giam gia da tinh (dung cho checkout).
     */
    private BigDecimal getDiscountAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }

        BigDecimal discountPercent = BigDecimal.ZERO;
        try {
            String percentStr = txtDiscountPercent.getText().trim().replaceAll("[^0-9.]", "");
            if (!percentStr.isEmpty()) {
                discountPercent = new BigDecimal(percentStr);
            }
        } catch (Exception ignored) {
        }

        if (discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            discountPercent = BigDecimal.valueOf(100);
        }
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0) {
            discountPercent = BigDecimal.ZERO;
        }

        return total.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private void calculateChange() {
        try {
            String cashStr = txtCashGiven.getText().trim().replaceAll("[^0-9]", "");
            if (cashStr.isEmpty()) {
                lblChangeAmount.setText("0 \u0111");
                return;
            }
            BigDecimal cashGiven = new BigDecimal(cashStr);

            BigDecimal total = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                total = total.add(item.getSubtotal());
            }
            BigDecimal discount = getDiscountAmount();
            BigDecimal finalAmount = total.subtract(discount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            if (cashGiven.compareTo(finalAmount) >= 0) {
                BigDecimal change = cashGiven.subtract(finalAmount);
                lblChangeAmount.setText(CurrencyUtils.format(change));
            } else {
                BigDecimal shortage = finalAmount.subtract(cashGiven);
                lblChangeAmount.setText("C\u00f2n thi\u1ebfu: " + CurrencyUtils.format(shortage));
            }
        } catch (Exception ex) {
            lblChangeAmount.setText("0 \u0111");
        }
    }

    private void findCustomerByPhone() {
        String phone = txtCustomerPhone.getText().trim();
        if (phone.isEmpty()) {
            MessageUtils.showWarning(this, "Vui l\u00f2ng nh\u1eadp s\u1ed1 \u0111i\u1ec7n tho\u1ea1i c\u1ea7n tra c\u1ee9u!");
            return;
        }

        Customer c = customerService.getCustomerByPhone(phone);
        if (c != null) {
            selectCustomer(c);
            MessageUtils.showInfo(this, "T\u00ecm th\u1ea5y kh\u00e1ch h\u00e0ng: " + c.getFullName());
        } else {
            boolean create = MessageUtils.showConfirm(this,
                    "Kh\u00f4ng t\u00ecm th\u1ea5y kh\u00e1ch h\u00e0ng v\u1edbi S\u0110T '" + phone + "'.\nB\u1ea1n c\u00f3 mu\u1ed1n t\u1ea1o nhanh kh\u00e1ch h\u00e0ng n\u00e0y ngay b\u00e2y gi\u1edd?");
            if (create) {
                quickCreateCustomer();
            }
        }
    }

    private void quickCreateCustomer() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Customer created = QuickCustomerDialog.showDialog(parent, txtCustomerPhone.getText().trim());
        if (created != null) {
            selectCustomer(created);
        }
    }

    private void clearSelectedCustomer() {
        this.selectedCustomer = null;
        txtCustomerPhone.setText("");
        updateCustomerDisplay();
    }

    private void selectCustomer(Customer customer) {
        this.selectedCustomer = customer;
        txtCustomerPhone.setText(customer.getPhone());
        updateCustomerDisplay();
    }

    private void updateCustomerDisplay() {
        if (selectedCustomer != null) {
            lblCustomerName.setText("Kh\u00e1ch h\u00e0ng: " + selectedCustomer.getFullName() + " [#" + selectedCustomer.getCustomerId() + "]");
            lblCustomerName.setForeground(new Color(30, 41, 59));
            lblCustomerDetail.setText("S\u0110T: " + selectedCustomer.getPhone()
                    + " | \u0110\u1ecba ch\u1ec9: " + (selectedCustomer.getAddress() != null && !selectedCustomer.getAddress().isEmpty() ? selectedCustomer.getAddress() : "Ch\u01b0a c\u1eadp nh\u1eadt"));
        } else {
            lblCustomerName.setText("Kh\u00e1ch h\u00e0ng: Ch\u01b0a ch\u1ecdn kh\u00e1ch h\u00e0ng");
            lblCustomerName.setForeground(new Color(220, 38, 38));
            lblCustomerDetail.setText("Vui l\u00f2ng nh\u1eadp S\u0110T \u0111\u1ec3 t\u00ecm ho\u1eb7c b\u1ea5m T\u1ea1o Nhanh kh\u00e1ch h\u00e0ng m\u1edbi");
        }
    }

    // =========================================================================
    // THANH TOAN (JDBC TRANSACTION)
    // =========================================================================
    private void processCheckout() {
        // 1. Kiem tra gio hang
        if (cartItems.isEmpty()) {
            MessageUtils.showWarning(this, "Gi\u1ecf h\u00e0ng \u0111ang tr\u1ed1ng! Vui l\u00f2ng th\u00eam \u00edt nh\u1ea5t m\u1ed9t s\u1ea3n ph\u1ea9m.");
            return;
        }

        // 2. Kiem tra khach hang (bat buoc phai chon hoac nhap khach hang)
        if (selectedCustomer == null) {
            MessageUtils.showWarning(this, "Vui l\u00f2ng ch\u1ecdn ho\u1eb7c t\u1ea1o m\u1edbi th\u00f4ng tin kh\u00e1ch h\u00e0ng tr\u01b0\u1edbc khi thanh to\u00e1n!");
            txtCustomerPhone.requestFocus();
            return;
        }

        // 3. Lay nhan vien dang dang nhap
        Employee currentEmployee = SessionManager.getCurrentEmployee();
        int employeeId = currentEmployee != null ? currentEmployee.getEmployeeId() : 1;

        // 4. Chuan bi doi tuong Invoice & Details
        Invoice invoice = new Invoice();
        invoice.setCustomerId(selectedCustomer.getCustomerId());
        invoice.setEmployeeId(employeeId);
        invoice.setNote(txtNote.getText().trim());

        BigDecimal discount = getDiscountAmount();
        invoice.setDiscountAmount(discount);

        List<InvoiceDetail> details = new ArrayList<>();
        for (CartItem item : cartItems) {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setProductId(item.product.getProductId());
            detail.setProductCode(item.product.getProductCode());
            detail.setProductName(item.product.getProductName());
            detail.setQuantity(item.quantity);
            detail.setUnitPrice(item.unitPrice);
            detail.setSubtotal(item.getSubtotal());
            details.add(detail);
        }

        // 5. Xac nhan thanh toan
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem it : cartItems) total = total.add(it.getSubtotal());
        BigDecimal finalAmt = total.subtract(discount);

        String discountInfo = "";
        try {
            String pStr = txtDiscountPercent.getText().trim().replaceAll("[^0-9.]", "");
            if (!pStr.isEmpty() && new BigDecimal(pStr).compareTo(BigDecimal.ZERO) > 0) {
                discountInfo = pStr + "% = ";
            }
        } catch (Exception ignored) {
        }

        boolean confirm = MessageUtils.showConfirm(this,
                "X\u00c1C NH\u1eacN THANH TO\u00c1N H\u00d3A \u0110\u01a0N:\n\n"
                + "Kh\u00e1ch h\u00e0ng: " + selectedCustomer.getFullName() + "\n"
                + "S\u1ed1 l\u01b0\u1ee3ng m\u00f3n: " + cartItems.size() + " lo\u1ea1i s\u1ea3n ph\u1ea9m\n"
                + "T\u1ed5ng ti\u1ec1n h\u00e0ng: " + CurrencyUtils.format(total) + "\n"
                + "Gi\u1ea3m gi\u00e1: -" + discountInfo + CurrencyUtils.format(discount) + "\n"
                + "--------------------------------------------------\n"
                + "TH\u1ef0C THU: " + CurrencyUtils.format(finalAmt) + "\n\n"
                + "Ti\u1ebfn h\u00e0nh tr\u1eeb t\u1ed3n kho v\u00e0 ghi nh\u1eadn h\u00f3a \u0111\u01a1n?");

        if (!confirm) {
            return;
        }

        // 6. Thuc thi JDBC Transaction qua InvoiceService
        try {
            Invoice savedInvoice = invoiceService.createInvoice(invoice, details);

            MessageUtils.showInfo(this, "THANH TO\u00c1N TH\u00c0NH C\u00d4NG!\n"
                    + "M\u00e3 H\u00f3a \u0110\u01a1n: " + savedInvoice.getInvoiceCode() + "\n"
                    + "T\u1ed5ng ti\u1ec1n: " + CurrencyUtils.format(savedInvoice.getFinalAmount()));

            // Mo hop thoai xem truoc & in hoa don
            Window parent = SwingUtilities.getWindowAncestor(this);
            InvoicePreviewDialog previewDialog = new InvoicePreviewDialog(parent, savedInvoice.getInvoiceId());
            previewDialog.setVisible(true);

            // Reset form sau khi thanh toan thanh cong
            cartItems.clear();
            renderCartTable();
            txtDiscountPercent.setText("0");
            txtCashGiven.setText("");
            lblChangeAmount.setText("0 \u0111");
            txtNote.setText("");
            clearSelectedCustomer();

            // Cap nhat lai ton kho trong danh sach san pham ben trai
            loadAvailableProducts(txtSearchProduct.getText());

        } catch (Exception ex) {
            MessageUtils.showError(this, "L\u1ed6I THANH TO\u00c1N GIAO D\u1ecaCH!\nH\u1ec7 th\u1ed1ng \u0111\u00e3 t\u1ef1 \u0111\u1ed9ng ROLLBACK \u0111\u1ec3 b\u1ea3o to\u00e0n d\u1eef li\u1ec7u.\nChi ti\u1ebft l\u1ed7i: " + ex.getMessage());
        }
    }

    // =========================================================================
    // INNER CLASS: CartItem
    // =========================================================================
    private static class CartItem {
        Product product;
        int quantity;
        BigDecimal unitPrice;

        public CartItem(Product product, int quantity, BigDecimal unitPrice) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public BigDecimal getSubtotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // =========================================================================
    // INNER CLASS: Custom Renderer cho cot So Luong - hien thi [- / x] [SL] [+]
    // =========================================================================
    private class QuantityButtonRenderer extends JPanel implements TableCellRenderer {
        private final JLabel btnMinus;
        private final JLabel lblQty;
        private final JLabel btnPlus;

        public QuantityButtonRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(true);

            btnMinus = new JLabel("", SwingConstants.CENTER);
            btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnMinus.setPreferredSize(new Dimension(20, 20));
            btnMinus.setMinimumSize(new Dimension(20, 20));
            btnMinus.setMaximumSize(new Dimension(20, 20));
            btnMinus.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            btnMinus.setOpaque(true);
            btnMinus.setBackground(Color.WHITE);

            lblQty = new JLabel("", SwingConstants.CENTER);
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblQty.setPreferredSize(new Dimension(24, 20));
            lblQty.setMinimumSize(new Dimension(24, 20));
            lblQty.setMaximumSize(new Dimension(24, 20));
            lblQty.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(203, 213, 225)));
            lblQty.setOpaque(true);
            lblQty.setBackground(Color.WHITE);

            btnPlus = new JLabel("+", SwingConstants.CENTER);
            btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPlus.setPreferredSize(new Dimension(20, 20));
            btnPlus.setMinimumSize(new Dimension(20, 20));
            btnPlus.setMaximumSize(new Dimension(20, 20));
            btnPlus.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            btnPlus.setOpaque(true);
            btnPlus.setBackground(Color.WHITE);

            add(Box.createHorizontalGlue());
            add(btnMinus);
            add(lblQty);
            add(btnPlus);
            add(Box.createHorizontalGlue());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            int qty = 0;
            if (value instanceof Integer) {
                qty = (Integer) value;
            }

            lblQty.setText(String.valueOf(qty));

            // Khi SL = 1: hien "x" (xoa); khi SL > 1: hien "-"
            if (qty <= 1) {
                btnMinus.setText("x");
                btnMinus.setForeground(new Color(220, 38, 38));
            } else {
                btnMinus.setText("\u2212"); // dau tru (minus sign)
                btnMinus.setForeground(new Color(30, 41, 59));
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }

    // =========================================================================
    // INNER CLASS: Custom Editor cho cot So Luong - xu ly click vao [- / x] va [+]
    // =========================================================================
    private class QuantityButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JLabel btnMinus;
        private final JLabel lblQty;
        private final JLabel btnPlus;
        private int currentQty;
        private int editingRow;

        public QuantityButtonEditor() {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);

            btnMinus = new JLabel("", SwingConstants.CENTER);
            btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnMinus.setPreferredSize(new Dimension(20, 20));
            btnMinus.setMinimumSize(new Dimension(20, 20));
            btnMinus.setMaximumSize(new Dimension(20, 20));
            btnMinus.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            btnMinus.setOpaque(true);
            btnMinus.setBackground(new Color(248, 250, 252));
            btnMinus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            lblQty = new JLabel("", SwingConstants.CENTER);
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblQty.setPreferredSize(new Dimension(24, 20));
            lblQty.setMinimumSize(new Dimension(24, 20));
            lblQty.setMaximumSize(new Dimension(24, 20));
            lblQty.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(203, 213, 225)));
            lblQty.setOpaque(true);
            lblQty.setBackground(Color.WHITE);

            btnPlus = new JLabel("+", SwingConstants.CENTER);
            btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPlus.setPreferredSize(new Dimension(20, 20));
            btnPlus.setMinimumSize(new Dimension(20, 20));
            btnPlus.setMaximumSize(new Dimension(20, 20));
            btnPlus.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            btnPlus.setOpaque(true);
            btnPlus.setBackground(new Color(248, 250, 252));
            btnPlus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            panel.add(Box.createHorizontalGlue());
            panel.add(btnMinus);
            panel.add(lblQty);
            panel.add(btnPlus);
            panel.add(Box.createHorizontalGlue());

            // Click vao nut "-" hoac "x"
            btnMinus.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    fireEditingStopped();
                    SwingUtilities.invokeLater(() -> adjustCartQuantity(editingRow, -1));
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    btnMinus.setBackground(new Color(230, 230, 230));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnMinus.setBackground(new Color(245, 245, 245));
                }
            });

            // Click vao nut "+"
            btnPlus.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    fireEditingStopped();
                    SwingUtilities.invokeLater(() -> adjustCartQuantity(editingRow, +1));
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    btnPlus.setBackground(new Color(230, 230, 230));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnPlus.setBackground(new Color(245, 245, 245));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            editingRow = row;
            currentQty = 0;
            if (value instanceof Integer) {
                currentQty = (Integer) value;
            }

            lblQty.setText(String.valueOf(currentQty));

            if (currentQty <= 1) {
                btnMinus.setText("x");
                btnMinus.setForeground(new Color(220, 38, 38));
            } else {
                btnMinus.setText("\u2212");
                btnMinus.setForeground(new Color(30, 41, 59));
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentQty;
        }
    }
}
