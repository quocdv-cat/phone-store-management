package com.phonestore.view;

import com.phonestore.dao.ProductDAO;
import com.phonestore.model.Employee;
import com.phonestore.model.Product;
import com.phonestore.model.PurchaseDetail;
import com.phonestore.model.PurchaseOrder;
import com.phonestore.model.Supplier;
import com.phonestore.service.PurchaseService;
import com.phonestore.service.SupplierService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.MessageUtils;
import com.phonestore.utils.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý Nhập hàng & Phiếu nhập (PurchasePanel).
 * Tích hợp 2 Tab qua JTabbedPane:
 * - Tab 1: Tạo phiếu nhập kho mới.
 * - Tab 2: Lịch sử phiếu nhập & Chi tiết (PurchaseHistoryPanel).
 * Thuộc Module 7 - Phân hệ TV3.
 */
public class PurchasePanel extends JPanel {

    private final PurchaseService purchaseService = new PurchaseService();
    private final SupplierService supplierService = new SupplierService();
    private final ProductDAO productDAO = new ProductDAO();

    private JTabbedPane tabbedPane;
    private PurchaseHistoryPanel historyPanel;

    // Thành phần Tab 1 - Thông tin chung phiếu nhập
    private JTextField txtPurchaseCode;
    private JTextField txtPurchaseDate;
    private JTextField txtEmployeeName;
    private JComboBox<Supplier> cboSuppliers;
    private JTextField txtNote;

    // Chọn sản phẩm nhập
    private JComboBox<Product> cboProducts;
    private JLabel lblCurrentStock;
    private JLabel lblCurrentImportPrice;
    private JSpinner spnQuantity;
    private JTextField txtImportPrice;
    private JButton btnAddToCart;

    // Bảng giỏ hàng phiếu nhập
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private final List<PurchaseDetail> cartItems = new ArrayList<>();

    private JLabel lblTotalAmount;
    private JButton btnRemoveItem;
    private JButton btnClearCart;
    private JButton btnSaveOrder;

    public PurchasePanel() {
        initComponents();
        initNewOrder();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // TAB 1: Tạo phiếu nhập mới
        JPanel newOrderPanel = createNewOrderPanel();
        tabbedPane.addTab("1. Lập Phiếu Nhập Hàng Mới", newOrderPanel);

        // TAB 2: Lịch sử phiếu nhập
        historyPanel = new PurchaseHistoryPanel();
        tabbedPane.addTab("2. Lịch Sử Phiếu Nhập & Chi Tiết", historyPanel);

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                historyPanel.loadSuppliersFilter();
                historyPanel.loadOrders();
            } else if (tabbedPane.getSelectedIndex() == 0) {
                reloadSuppliersAndProducts();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createNewOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. TOP: THÔNG TIN PHIẾU NHẬP
        JPanel masterInfoPanel = new JPanel(new GridBagLayout());
        masterInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Thông tin chung Phiếu Nhập", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPurchaseCode = new JTextField(15);
        txtPurchaseCode.setEditable(false);

        txtPurchaseDate = new JTextField(15);
        txtPurchaseDate.setEditable(false);

        txtEmployeeName = new JTextField(15);
        txtEmployeeName.setEditable(false);

        cboSuppliers = new JComboBox<>();
        cboSuppliers.setPreferredSize(new Dimension(250, 28));

        txtNote = new JTextField(30);

        // Hàng 0: Mã, Ngày, Nhân viên
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        masterInfoPanel.add(new JLabel("Mã phiếu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3;
        masterInfoPanel.add(txtPurchaseCode, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        masterInfoPanel.add(new JLabel("Ngày nhập:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.3;
        masterInfoPanel.add(txtPurchaseDate, gbc);

        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0;
        masterInfoPanel.add(new JLabel("Nhân viên:"), gbc);
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0.4;
        masterInfoPanel.add(txtEmployeeName, gbc);

        // Hàng 1: NCC, Ghi chú
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        masterInfoPanel.add(new JLabel("Nhà cung cấp (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.4;
        masterInfoPanel.add(cboSuppliers, gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        masterInfoPanel.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 4; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.6;
        masterInfoPanel.add(txtNote, gbc);

        panel.add(masterInfoPanel, BorderLayout.NORTH);

        // 2. CENTER: CHỌN SẢN PHẨM & BẢNG GIỎ HÀNG NHẬP
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));

        // Thanh chọn sản phẩm (Chia 2 hàng rõ ràng, không bị tràn viền)
        JPanel productSelectPanel = new JPanel(new GridBagLayout());
        productSelectPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Thêm sản phẩm vào danh sách chờ nhập", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        GridBagConstraints gbcProd = new GridBagConstraints();
        gbcProd.insets = new Insets(4, 6, 4, 6);
        gbcProd.fill = GridBagConstraints.HORIZONTAL;

        cboProducts = new JComboBox<>();
        cboProducts.setPreferredSize(new Dimension(300, 28));

        lblCurrentStock = new JLabel("Tồn hiện tại: 0");
        lblCurrentStock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCurrentStock.setForeground(new Color(2, 132, 199));

        lblCurrentImportPrice = new JLabel("Giá cũ: 0 đ");
        lblCurrentImportPrice.setForeground(new Color(100, 116, 139));

        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spnQuantity.setPreferredSize(new Dimension(80, 28));

        txtImportPrice = new JTextField(12);
        txtImportPrice.putClientProperty("JTextField.placeholderText", "Đơn giá nhập");

        btnAddToCart = new JButton("+ Thêm vào phiếu");
        btnAddToCart.setBackground(new Color(16, 185, 129));
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddToCart.setPreferredSize(new Dimension(160, 30));

        cboProducts.addActionListener(e -> onProductSelected());
        btnAddToCart.addActionListener(e -> onAddToCart());

        // Hàng 0: Chọn điện thoại & thông tin tồn, giá
        gbcProd.gridx = 0; gbcProd.gridy = 0; gbcProd.weightx = 0;
        productSelectPanel.add(new JLabel("Chọn điện thoại:"), gbcProd);
        gbcProd.gridx = 1; gbcProd.gridy = 0; gbcProd.weightx = 0.5;
        productSelectPanel.add(cboProducts, gbcProd);
        gbcProd.gridx = 2; gbcProd.gridy = 0; gbcProd.weightx = 0.25;
        productSelectPanel.add(lblCurrentStock, gbcProd);
        gbcProd.gridx = 3; gbcProd.gridy = 0; gbcProd.weightx = 0.25;
        productSelectPanel.add(lblCurrentImportPrice, gbcProd);

        // Hàng 1: Số lượng, Đơn giá nhập, Nút Thêm
        gbcProd.gridx = 0; gbcProd.gridy = 1; gbcProd.weightx = 0;
        productSelectPanel.add(new JLabel("Số lượng nhập:"), gbcProd);
        gbcProd.gridx = 1; gbcProd.gridy = 1; gbcProd.weightx = 0.2;
        productSelectPanel.add(spnQuantity, gbcProd);
        gbcProd.gridx = 2; gbcProd.gridy = 1; gbcProd.weightx = 0.5;
        JPanel priceBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        priceBox.add(new JLabel("Đơn giá nhập thực tế (VNĐ):"));
        priceBox.add(txtImportPrice);
        productSelectPanel.add(priceBox, gbcProd);
        gbcProd.gridx = 3; gbcProd.gridy = 1; gbcProd.weightx = 0.3;
        productSelectPanel.add(btnAddToCart, gbcProd);

        centerPanel.add(productSelectPanel, BorderLayout.NORTH);

        // Bảng giỏ hàng
        String[] columns = {"STT", "Mã SP", "Tên Điện Thoại", "Đơn Giá Nhập", "Số Lượng", "Thành Tiền"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblCart = new JTable(cartTableModel);
        tblCart.setRowHeight(28);
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Danh sách các mặt hàng chuẩn bị nhập kho", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)));
        centerPanel.add(scrollCart, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        // 3. BOTTOM: TỔNG TIỀN VÀ NÚT LƯU PHIẾU
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRemoveItem = new JButton("Xóa sản phẩm chọn");
        btnClearCart = new JButton("Xóa trắng giỏ");

        btnRemoveItem.addActionListener(e -> onRemoveItem());
        btnClearCart.addActionListener(e -> onClearCart());
        leftActions.add(btnRemoveItem);
        leftActions.add(btnClearCart);
        bottomPanel.add(leftActions, BorderLayout.WEST);

        JPanel rightSummary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        JLabel lblTotalText = new JLabel("TỔNG TIỀN PHIẾU NHẬP:");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblTotalAmount = new JLabel("0 đ");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalAmount.setForeground(new Color(220, 38, 38));

        btnSaveOrder = new JButton("LƯU PHIẾU NHẬP (Nhập kho)");
        btnSaveOrder.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSaveOrder.setBackground(new Color(16, 185, 129));
        btnSaveOrder.setForeground(Color.WHITE);
        btnSaveOrder.setPreferredSize(new Dimension(240, 42));
        btnSaveOrder.addActionListener(e -> onSaveOrder());

        rightSummary.add(lblTotalText);
        rightSummary.add(lblTotalAmount);
        rightSummary.add(btnSaveOrder);
        bottomPanel.add(rightSummary, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void initNewOrder() {
        txtPurchaseCode.setText(purchaseService.generatePurchaseCode());
        txtPurchaseDate.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        Employee emp = SessionManager.getCurrentEmployee();
        txtEmployeeName.setText(emp != null ? emp.getFullName() + " (" + emp.getEmployeeCode() + ")" : "Nhân viên kho (NV003)");

        txtNote.setText("");
        cartItems.clear();
        updateCartTable();
        reloadSuppliersAndProducts();
    }

    public void reloadSuppliersAndProducts() {
        // Load danh sách NCC
        cboSuppliers.removeAllItems();
        List<Supplier> suppliers = supplierService.getActiveSuppliers();
        for (Supplier s : suppliers) {
            cboSuppliers.addItem(s);
        }

        // Load danh sách SP từ ProductDAO (TV1)
        cboProducts.removeAllItems();
        List<Product> products = productDAO.findAllActive();
        for (Product p : products) {
            cboProducts.addItem(p);
        }
        onProductSelected();
    }

    private void onProductSelected() {
        Product p = (Product) cboProducts.getSelectedItem();
        if (p != null) {
            lblCurrentStock.setText("Tồn: " + p.getStockQuantity());
            lblCurrentImportPrice.setText("Giá cũ: " + CurrencyUtils.format(p.getImportPrice()));
            txtImportPrice.setText(p.getImportPrice().toPlainString());
        } else {
            lblCurrentStock.setText("Tồn: 0");
            lblCurrentImportPrice.setText("Giá cũ: 0 đ");
            txtImportPrice.setText("");
        }
    }

    private void onAddToCart() {
        Product p = (Product) cboProducts.getSelectedItem();
        if (p == null) {
            MessageUtils.showWarning(this, "Vui lòng chọn sản phẩm cần nhập!");
            return;
        }

        int qty = (int) spnQuantity.getValue();
        if (qty <= 0) {
            MessageUtils.showWarning(this, "Số lượng nhập phải lớn hơn 0!");
            return;
        }

        String priceStr = txtImportPrice.getText().trim().replace(",", "").replace(".", "");
        BigDecimal importPrice;
        try {
            importPrice = new BigDecimal(priceStr);
            if (importPrice.compareTo(BigDecimal.ZERO) <= 0) {
                MessageUtils.showWarning(this, "Đơn giá nhập phải lớn hơn 0!");
                return;
            }
        } catch (Exception ex) {
            MessageUtils.showWarning(this, "Đơn giá nhập không hợp lệ! Vui lòng nhập số hợp lệ.");
            return;
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        PurchaseDetail existing = null;
        for (PurchaseDetail d : cartItems) {
            if (d.getProductId() == p.getProductId()) {
                existing = d;
                break;
            }
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
            existing.setImportPrice(importPrice);
            existing.setSubtotal(importPrice.multiply(BigDecimal.valueOf(existing.getQuantity())));
        } else {
            PurchaseDetail detail = new PurchaseDetail();
            detail.setProductId(p.getProductId());
            detail.setProductCode(p.getProductCode());
            detail.setProductName(p.getProductName());
            detail.setQuantity(qty);
            detail.setImportPrice(importPrice);
            detail.setSubtotal(importPrice.multiply(BigDecimal.valueOf(qty)));
            cartItems.add(detail);
        }

        spnQuantity.setValue(1);
        updateCartTable();
    }

    private void onRemoveItem() {
        int row = tblCart.getSelectedRow();
        if (row < 0) {
            MessageUtils.showWarning(this, "Vui lòng chọn dòng sản phẩm cần xóa khỏi danh sách!");
            return;
        }
        cartItems.remove(row);
        updateCartTable();
    }

    private void onClearCart() {
        if (!cartItems.isEmpty() && MessageUtils.showConfirm(this, "Bạn có muốn xóa toàn bộ danh sách hàng chờ nhập?")) {
            cartItems.clear();
            updateCartTable();
        }
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        int stt = 1;
        for (PurchaseDetail d : cartItems) {
            cartTableModel.addRow(new Object[]{
                stt++,
                d.getProductCode(),
                d.getProductName(),
                CurrencyUtils.format(d.getImportPrice()),
                d.getQuantity(),
                CurrencyUtils.format(d.getSubtotal())
            });
            total = total.add(d.getSubtotal());
        }
        lblTotalAmount.setText(CurrencyUtils.format(total));
    }

    private void onSaveOrder() {
        Supplier supplier = (Supplier) cboSuppliers.getSelectedItem();
        if (supplier == null) {
            MessageUtils.showWarning(this, "Vui lòng chọn Nhà cung cấp!");
            return;
        }

        if (cartItems.isEmpty()) {
            MessageUtils.showWarning(this, "Danh sách hàng chờ nhập đang trống! Vui lòng thêm ít nhất một sản phẩm.");
            return;
        }

        Employee emp = SessionManager.getCurrentEmployee();
        int empId = emp != null ? emp.getEmployeeId() : 3; // Fallback NV kho NV003 nếu chưa login

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseCode(txtPurchaseCode.getText().trim());
        order.setSupplierId(supplier.getSupplierId());
        order.setEmployeeId(empId);
        order.setPurchaseDate(LocalDateTime.now());
        order.setNote(txtNote.getText().trim());
        order.setStatus("COMPLETED");

        boolean confirm = MessageUtils.showConfirm(this,
                "Xác nhận LẬP PHIẾU NHẬP VÀ CẬP NHẬT KHO?\n- Mã: " + order.getPurchaseCode()
                + "\n- Nhà cung cấp: " + supplier.getSupplierName()
                + "\n- Số lượng mặt hàng: " + cartItems.size()
                + "\n- Tổng tiền: " + lblTotalAmount.getText()
                + "\n(Tồn kho và Giá nhập của các sản phẩm sẽ được tự động cộng và cập nhật bằng JDBC Transaction)");
        if (!confirm) {
            return;
        }

        String error = purchaseService.createPurchaseOrder(order, cartItems);
        if (error != null) {
            MessageUtils.showError(this, error);
        } else {
            MessageUtils.showInfo(this, "Nhập kho thành công!\nPhiếu nhập " + order.getPurchaseCode() + " đã được lưu và cập nhật tồn kho.");
            initNewOrder();
            historyPanel.loadOrders();
        }
    }
}
