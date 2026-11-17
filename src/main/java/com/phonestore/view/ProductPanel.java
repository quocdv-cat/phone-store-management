package com.phonestore.view;

import com.phonestore.model.Brand;
import com.phonestore.model.Category;
import com.phonestore.model.Product;
import com.phonestore.service.BrandService;
import com.phonestore.service.CategoryService;
import com.phonestore.service.ProductService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý Điện thoại (Product Management) - Đầy đủ CRUD, Tìm kiếm, Lọc đa tiêu chí.
 * (Giá nhập do phân hệ Quản lý Nhập kho đảm nhiệm).
 */
public class ProductPanel extends JPanel {

    private final ProductService productService = new ProductService();
    private final BrandService brandService = new BrandService();
    private final CategoryService categoryService = new CategoryService();

    // Bộ lọc tìm kiếm
    private JTextField txtSearchKeyword;
    private JComboBox<Brand> cbxFilterBrand;
    private JComboBox<Category> cbxFilterCategory;
    private JComboBox<String> cbxFilterStatus;
    private JCheckBox chkOnlyInStock;

    // Bảng danh sách
    private JTable tblProducts;
    private DefaultTableModel tableModel;
    private List<Product> currentProducts;
    private int selectedProductId = 0;

    // Form nhập liệu CRUD (Không bao gồm Giá nhập vì Giá nhập do Quản lý Nhập kho quản lý)
    private JTextField txtProductCode;
    private JTextField txtProductName;
    private JComboBox<Brand> cbxFormBrand;
    private JComboBox<Category> cbxFormCategory;
    private JTextField txtModel;
    private JComboBox<String> cbxRam;
    private JComboBox<String> cbxRom;
    private JTextField txtColor;
    private JTextField txtSellingPrice;
    private JSpinner spnStockQuantity;
    private JSpinner spnWarrantyMonths;
    private JComboBox<String> cbxFormStatus;

    public ProductPanel() {
        initComponents();
        reloadBrandAndCategoryCombos();
        loadProducts();
    }

    public void refreshData() {
        reloadBrandAndCategoryCombos();
        loadProducts();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. TOP FILTER BAR (Sử dụng GridBagLayout để khi thu nhỏ cửa sổ không bao giờ bị che nút Tìm / Lọc & Đặt lại)
        JPanel topContainer = new JPanel(new BorderLayout(0, 8));
        JLabel lblTitle = new JLabel("QUẢN LÝ ĐIỆN THOẠI & TỒN KHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        topContainer.add(lblTitle, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new GridBagLayout());
        filterBar.setBorder(BorderFactory.createTitledBorder("Tìm kiếm & Bộ lọc"));
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.insets = new Insets(4, 6, 4, 6);
        fgbc.fill = GridBagConstraints.HORIZONTAL;

        txtSearchKeyword = new JTextField(14);
        cbxFilterBrand = new JComboBox<>();
        cbxFilterCategory = new JComboBox<>();
        cbxFilterStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ALL"});
        chkOnlyInStock = new JCheckBox("Chỉ hiện máy còn hàng");

        JButton btnFilter = new JButton("Tìm / Lọc");
        btnFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFilter.setBackground(new Color(37, 99, 235));
        btnFilter.setForeground(Color.WHITE);

        JButton btnResetFilter = new JButton("Đặt lại");
        btnResetFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btnFilter.addActionListener(e -> loadProducts());
        txtSearchKeyword.addActionListener(e -> loadProducts());
        btnResetFilter.addActionListener(e -> {
            txtSearchKeyword.setText("");
            if (cbxFilterBrand.getItemCount() > 0) cbxFilterBrand.setSelectedIndex(0);
            if (cbxFilterCategory.getItemCount() > 0) cbxFilterCategory.setSelectedIndex(0);
            cbxFilterStatus.setSelectedIndex(0);
            chkOnlyInStock.setSelected(false);
            loadProducts();
        });

        // Dòng 1 của bộ lọc: Từ khóa + Hãng + Danh mục
        fgbc.gridx = 0; fgbc.gridy = 0; fgbc.weightx = 0;
        filterBar.add(new JLabel("Từ khóa:"), fgbc);
        fgbc.gridx = 1; fgbc.weightx = 0.4;
        filterBar.add(txtSearchKeyword, fgbc);

        fgbc.gridx = 2; fgbc.weightx = 0;
        filterBar.add(new JLabel("Hãng:"), fgbc);
        fgbc.gridx = 3; fgbc.weightx = 0.3;
        filterBar.add(cbxFilterBrand, fgbc);

        fgbc.gridx = 4; fgbc.weightx = 0;
        filterBar.add(new JLabel("Danh mục:"), fgbc);
        fgbc.gridx = 5; fgbc.weightx = 0.3;
        filterBar.add(cbxFilterCategory, fgbc);

        // Dòng 2 của bộ lọc: Trạng thái + Còn hàng + Nút Tìm / Lọc + Đặt lại (luôn hiển thị rõ khi cửa sổ thu nhỏ)
        fgbc.gridx = 0; fgbc.gridy = 1; fgbc.weightx = 0;
        filterBar.add(new JLabel("Trạng thái:"), fgbc);
        fgbc.gridx = 1; fgbc.weightx = 0.4;
        filterBar.add(cbxFilterStatus, fgbc);

        fgbc.gridx = 2; fgbc.gridwidth = 2; fgbc.weightx = 0.3;
        filterBar.add(chkOnlyInStock, fgbc);

        JPanel filterBtnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBtnGroup.setOpaque(false);
        filterBtnGroup.add(btnFilter);
        filterBtnGroup.add(btnResetFilter);

        fgbc.gridx = 4; fgbc.gridwidth = 2; fgbc.weightx = 0.3;
        filterBar.add(filterBtnGroup, fgbc);

        topContainer.add(filterBar, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // 2. CENTER TABLE (Đã lược bỏ cột Giá Nhập để chuyển sang cho Quản lý Nhập kho quản lý)
        String[] cols = {
            "ID", "Mã SP", "Tên Điện Thoại", "Hãng", "Danh Mục", "Model",
            "RAM", "ROM", "Màu Sắc", "Giá Bán", "Tồn Kho", "Bảo Hành", "Trạng Thái"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblProducts = new JTable(tableModel);
        tblProducts.setRowHeight(27);
        tblProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProducts.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblProducts.getSelectedRow() >= 0) {
                populateFormFromSelectedRow(tblProducts.getSelectedRow());
            }
        });
        add(new JScrollPane(tblProducts), BorderLayout.CENTER);

        // 3. BOTTOM FORM & CRUD BUTTONS
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết Điện thoại"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtProductCode = new JTextField(12);
        txtProductName = new JTextField(20);
        cbxFormBrand = new JComboBox<>();
        cbxFormCategory = new JComboBox<>();
        txtModel = new JTextField(12);
        cbxRam = new JComboBox<>(new String[]{"4GB", "6GB", "8GB", "12GB", "16GB", "24GB"});
        cbxRam.setEditable(true);
        cbxRom = new JComboBox<>(new String[]{"64GB", "128GB", "256GB", "512GB", "1TB"});
        cbxRom.setEditable(true);
        txtColor = new JTextField(12);
        txtSellingPrice = new JTextField("0", 12);
        spnStockQuantity = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
        spnWarrantyMonths = new JSpinner(new SpinnerNumberModel(12, 0, 60, 1));
        cbxFormStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        // Hàng 1
        addFormField(form, gbc, 0, 0, "Mã SP (*):", txtProductCode);
        addFormField(form, gbc, 2, 0, "Tên điện thoại (*):", txtProductName);
        addFormField(form, gbc, 4, 0, "Thương hiệu (*):", cbxFormBrand);
        addFormField(form, gbc, 6, 0, "Danh mục (*):", cbxFormCategory);

        // Hàng 2
        addFormField(form, gbc, 0, 1, "Model:", txtModel);
        addFormField(form, gbc, 2, 1, "RAM (*):", cbxRam);
        addFormField(form, gbc, 4, 1, "Bộ nhớ trong (*):", cbxRom);
        addFormField(form, gbc, 6, 1, "Màu sắc (*):", txtColor);

        // Hàng 3 (Không có Giá nhập - Giá nhập do Quản lý Nhập kho cập nhật)
        addFormField(form, gbc, 0, 2, "Giá bán (*):", txtSellingPrice);
        addFormField(form, gbc, 2, 2, "Số lượng tồn kho:", spnStockQuantity);
        addFormField(form, gbc, 4, 2, "Thời gian bảo hành:", spnWarrantyMonths);
        addFormField(form, gbc, 6, 2, "Trạng thái:", cbxFormStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        JButton btnAdd = new JButton("Thêm điện thoại");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setBackground(new Color(22, 163, 74));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(150, 36));

        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdate.setBackground(new Color(37, 99, 235));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setPreferredSize(new Dimension(130, 36));

        JButton btnDelete = new JButton("Xóa / Ngừng KD");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setBackground(new Color(220, 38, 38));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(150, 36));

        JButton btnClear = new JButton("Làm mới");
        btnClear.setPreferredSize(new Dimension(130, 36));

        btnAdd.addActionListener(e -> handleAddProduct());
        btnUpdate.addActionListener(e -> handleUpdateProduct());
        btnDelete.addActionListener(e -> handleDeleteProduct());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel form, GridBagConstraints gbc, int x, int y, String label, java.awt.Component comp) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 0.25;
        form.add(comp, gbc);
    }

    private void reloadBrandAndCategoryCombos() {
        List<Brand> brands = brandService.getActiveBrands();
        List<Category> categories = categoryService.getActiveCategories();

        cbxFilterBrand.removeAllItems();
        cbxFilterBrand.addItem(new Brand(0, "-- Tất cả hãng --", "", "", true));
        cbxFormBrand.removeAllItems();
        for (Brand b : brands) {
            cbxFilterBrand.addItem(b);
            cbxFormBrand.addItem(b);
        }

        cbxFilterCategory.removeAllItems();
        cbxFilterCategory.addItem(new Category(0, "-- Tất cả danh mục --", "", true));
        cbxFormCategory.removeAllItems();
        for (Category c : categories) {
            cbxFilterCategory.addItem(c);
            cbxFormCategory.addItem(c);
        }
    }

    private void loadProducts() {
        tableModel.setRowCount(0);
        String keyword = txtSearchKeyword.getText();
        Brand filterBrand = (Brand) cbxFilterBrand.getSelectedItem();
        Category filterCategory = (Category) cbxFilterCategory.getSelectedItem();
        Integer brandId = (filterBrand != null && filterBrand.getBrandId() > 0) ? filterBrand.getBrandId() : null;
        Integer categoryId = (filterCategory != null && filterCategory.getCategoryId() > 0) ? filterCategory.getCategoryId() : null;
        String statusFilter = (String) cbxFilterStatus.getSelectedItem();
        boolean onlyInStock = chkOnlyInStock.isSelected();

        currentProducts = productService.searchProducts(keyword, brandId, categoryId, onlyInStock, statusFilter);
        for (Product p : currentProducts) {
            tableModel.addRow(new Object[]{
                p.getProductId(),
                p.getProductCode(),
                p.getProductName(),
                p.getBrandName(),
                p.getCategoryName(),
                p.getModel(),
                p.getRam(),
                p.getRom(),
                p.getColor(),
                CurrencyUtils.format(p.getSellingPrice()),
                p.getStockQuantity(),
                p.getWarrantyMonths() + " tháng",
                p.getStatus()
            });
        }
    }

    private void populateFormFromSelectedRow(int row) {
        if (currentProducts == null || row < 0 || row >= currentProducts.size()) {
            return;
        }
        Product p = currentProducts.get(row);
        selectedProductId = p.getProductId();
        txtProductCode.setText(p.getProductCode());
        txtProductName.setText(p.getProductName());
        txtModel.setText(p.getModel());
        cbxRam.setSelectedItem(p.getRam());
        cbxRom.setSelectedItem(p.getRom());
        txtColor.setText(p.getColor());
        txtSellingPrice.setText(p.getSellingPrice() != null ? p.getSellingPrice().toPlainString() : "0");
        spnStockQuantity.setValue(p.getStockQuantity());
        spnWarrantyMonths.setValue(p.getWarrantyMonths());
        cbxFormStatus.setSelectedItem(p.getStatus());

        for (int i = 0; i < cbxFormBrand.getItemCount(); i++) {
            if (cbxFormBrand.getItemAt(i).getBrandId() == p.getBrandId()) {
                cbxFormBrand.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cbxFormCategory.getItemCount(); i++) {
            if (cbxFormCategory.getItemAt(i).getCategoryId() == p.getCategoryId()) {
                cbxFormCategory.setSelectedIndex(i);
                break;
            }
        }
    }

    private Product buildProductFromForm() {
        Product p = new Product();
        p.setProductId(selectedProductId);
        p.setProductCode(txtProductCode.getText());
        p.setProductName(txtProductName.getText());

        Brand b = (Brand) cbxFormBrand.getSelectedItem();
        Category c = (Category) cbxFormCategory.getSelectedItem();
        p.setBrandId(b != null ? b.getBrandId() : 0);
        p.setCategoryId(c != null ? c.getCategoryId() : 0);

        p.setModel(txtModel.getText());
        p.setRam(cbxRam.getSelectedItem() != null ? cbxRam.getSelectedItem().toString() : "");
        p.setRom(cbxRom.getSelectedItem() != null ? cbxRom.getSelectedItem().toString() : "");
        p.setColor(txtColor.getText());
        p.setImportPrice(BigDecimal.ZERO);

        try {
            String rawSelling = txtSellingPrice.getText().replace(".", "").replace(",", "").trim();
            p.setSellingPrice(new BigDecimal(rawSelling));
        } catch (NumberFormatException ex) {
            MessageUtils.showError(this, "Giá bán phải là số hợp lệ!");
            return null;
        }

        p.setStockQuantity((Integer) spnStockQuantity.getValue());
        p.setWarrantyMonths((Integer) spnWarrantyMonths.getValue());
        p.setStatus((String) cbxFormStatus.getSelectedItem());
        return p;
    }

    private void handleAddProduct() {
        Product p = buildProductFromForm();
        if (p == null) {
            return;
        }
        String err = productService.addProduct(p);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Thêm điện thoại mới thành công!");
        clearForm();
        loadProducts();
    }

    private void handleUpdateProduct() {
        if (selectedProductId <= 0) {
            MessageUtils.showWarning(this, "Vui lòng chọn một điện thoại trên bảng để cập nhật!");
            return;
        }
        Product p = buildProductFromForm();
        if (p == null) {
            return;
        }
        String err = productService.updateProduct(p);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Cập nhật thông tin điện thoại thành công!");
        clearForm();
        loadProducts();
    }

    private void handleDeleteProduct() {
        if (selectedProductId <= 0) {
            MessageUtils.showWarning(this, "Vui lòng chọn một điện thoại trên bảng để xóa!");
            return;
        }
        if (!MessageUtils.showConfirm(this, "Bạn có chắc chắn muốn xóa / ngừng kinh doanh điện thoại này?")) {
            return;
        }
        String result = productService.deleteProduct(selectedProductId);
        if ("SOFT_DELETED".equals(result)) {
            MessageUtils.showInfo(this, "Sản phẩm đã có lịch sử giao dịch nên được chuyển sang trạng thái 'INACTIVE' (Ngừng kinh doanh) để bảo toàn dữ liệu!");
        } else if (result != null) {
            MessageUtils.showError(this, result);
            return;
        } else {
            MessageUtils.showInfo(this, "Đã xóa điện thoại khỏi hệ thống thành công!");
        }
        clearForm();
        loadProducts();
    }

    private void clearForm() {
        selectedProductId = 0;
        txtProductCode.setText("");
        txtProductName.setText("");
        txtModel.setText("");
        cbxRam.setSelectedItem("8GB");
        cbxRom.setSelectedItem("256GB");
        txtColor.setText("");
        txtSellingPrice.setText("0");
        spnStockQuantity.setValue(0);
        spnWarrantyMonths.setValue(12);
        cbxFormStatus.setSelectedItem("ACTIVE");
        tblProducts.clearSelection();
    }
}
