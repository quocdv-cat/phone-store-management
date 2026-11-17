package com.phonestore.view;

import com.phonestore.model.Brand;
import com.phonestore.model.Category;
import com.phonestore.service.BrandService;
import com.phonestore.service.CategoryService;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Hộp thoại quản lý đầy đủ CRUD cho Thương hiệu (Brand) và Danh mục (Category).
 */
public class CategoryBrandDialog extends JDialog {

    private final BrandService brandService = new BrandService();
    private final CategoryService categoryService = new CategoryService();
    private final Runnable onDataChanged;

    // Thành phần UI Tab Thương hiệu
    private JTextField txtSearchBrand;
    private JTable tblBrands;
    private DefaultTableModel modelBrands;
    private int selectedBrandId = 0;
    private JTextField txtBrandName;
    private JTextField txtBrandOrigin;
    private JTextField txtBrandDesc;
    private JCheckBox chkBrandActive;

    // Thành phần UI Tab Danh mục
    private JTextField txtSearchCategory;
    private JTable tblCategories;
    private DefaultTableModel modelCategories;
    private int selectedCategoryId = 0;
    private JTextField txtCategoryName;
    private JTextField txtCategoryDesc;
    private JCheckBox chkCategoryActive;

    public CategoryBrandDialog(Frame parent, Runnable onDataChanged) {
        super(parent, "Quản lý Thương hiệu & Danh mục Điện thoại", true);
        this.onDataChanged = onDataChanged;
        initComponents();
        loadBrands("");
        loadCategories("");
    }

    private void initComponents() {
        setSize(880, 580);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("1. Quản lý Thương hiệu", createBrandTab());
        tabbedPane.addTab("2. Quản lý Danh mục", createCategoryTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBrandTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Thanh tìm kiếm
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        txtSearchBrand = new JTextField(22);
        JButton btnSearch = new JButton("Tìm kiếm");
        JButton btnRefresh = new JButton("Tất cả");
        btnSearch.addActionListener(e -> loadBrands(txtSearchBrand.getText()));
        btnRefresh.addActionListener(e -> {
            txtSearchBrand.setText("");
            loadBrands("");
        });
        topBar.add(new JLabel("Tìm thương hiệu:"));
        topBar.add(txtSearchBrand);
        topBar.add(btnSearch);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        // Bảng danh sách
        String[] cols = {"ID", "Tên Thương Hiệu", "Xuất Xứ", "Mô Tả", "Trạng Thái"};
        modelBrands = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBrands = new JTable(modelBrands);
        tblBrands.setRowHeight(26);
        tblBrands.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBrands.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblBrands.getSelectedRow() >= 0) {
                int row = tblBrands.getSelectedRow();
                selectedBrandId = (int) modelBrands.getValueAt(row, 0);
                txtBrandName.setText(String.valueOf(modelBrands.getValueAt(row, 1)));
                txtBrandOrigin.setText(String.valueOf(modelBrands.getValueAt(row, 2)));
                txtBrandDesc.setText(String.valueOf(modelBrands.getValueAt(row, 3)));
                chkBrandActive.setSelected("Hoạt động".equals(modelBrands.getValueAt(row, 4)));
            }
        });
        panel.add(new JScrollPane(tblBrands), BorderLayout.CENTER);

        // Form nhập + Nút CRUD
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Thương hiệu"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtBrandName = new JTextField(18);
        txtBrandOrigin = new JTextField(15);
        txtBrandDesc = new JTextField(25);
        chkBrandActive = new JCheckBox("Đang hoạt động", true);

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Tên thương hiệu (*):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        form.add(txtBrandName, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(new JLabel("Quốc gia xuất xứ:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        form.add(txtBrandOrigin, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        form.add(txtBrandDesc, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        form.add(chkBrandActive, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnAdd = new JButton("Thêm mới");
        btnAdd.setBackground(new Color(22, 163, 74));
        btnAdd.setForeground(Color.WHITE);

        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setBackground(new Color(37, 99, 235));
        btnUpdate.setForeground(Color.WHITE);

        JButton btnDelete = new JButton("Xóa");
        btnDelete.setBackground(new Color(220, 38, 38));
        btnDelete.setForeground(Color.WHITE);

        JButton btnClear = new JButton("Làm mới");

        btnAdd.addActionListener(e -> handleAddBrand());
        btnUpdate.addActionListener(e -> handleUpdateBrand());
        btnDelete.addActionListener(e -> handleDeleteBrand());
        btnClear.addActionListener(e -> clearBrandForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCategoryTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        txtSearchCategory = new JTextField(22);
        JButton btnSearch = new JButton("Tìm kiếm");
        JButton btnRefresh = new JButton("Tất cả");
        btnSearch.addActionListener(e -> loadCategories(txtSearchCategory.getText()));
        btnRefresh.addActionListener(e -> {
            txtSearchCategory.setText("");
            loadCategories("");
        });
        topBar.add(new JLabel("Tìm danh mục:"));
        topBar.add(txtSearchCategory);
        topBar.add(btnSearch);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Tên Danh Mục", "Mô Tả", "Trạng Thái"};
        modelCategories = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCategories = new JTable(modelCategories);
        tblCategories.setRowHeight(26);
        tblCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCategories.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblCategories.getSelectedRow() >= 0) {
                int row = tblCategories.getSelectedRow();
                selectedCategoryId = (int) modelCategories.getValueAt(row, 0);
                txtCategoryName.setText(String.valueOf(modelCategories.getValueAt(row, 1)));
                txtCategoryDesc.setText(String.valueOf(modelCategories.getValueAt(row, 2)));
                chkCategoryActive.setSelected("Hoạt động".equals(modelCategories.getValueAt(row, 3)));
            }
        });
        panel.add(new JScrollPane(tblCategories), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Danh mục"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCategoryName = new JTextField(20);
        txtCategoryDesc = new JTextField(25);
        chkCategoryActive = new JCheckBox("Đang hoạt động", true);

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Tên danh mục (*):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        form.add(txtCategoryName, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(chkCategoryActive, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        form.add(txtCategoryDesc, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnAdd = new JButton("Thêm mới");
        btnAdd.setBackground(new Color(22, 163, 74));
        btnAdd.setForeground(Color.WHITE);

        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setBackground(new Color(37, 99, 235));
        btnUpdate.setForeground(Color.WHITE);

        JButton btnDelete = new JButton("Xóa");
        btnDelete.setBackground(new Color(220, 38, 38));
        btnDelete.setForeground(Color.WHITE);

        JButton btnClear = new JButton("Làm mới");

        btnAdd.addActionListener(e -> handleAddCategory());
        btnUpdate.addActionListener(e -> handleUpdateCategory());
        btnDelete.addActionListener(e -> handleDeleteCategory());
        btnClear.addActionListener(e -> clearCategoryForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadBrands(String keyword) {
        modelBrands.setRowCount(0);
        List<Brand> list = brandService.searchBrands(keyword);
        for (Brand b : list) {
            modelBrands.addRow(new Object[]{
                b.getBrandId(),
                b.getBrandName(),
                b.getOriginCountry(),
                b.getDescription(),
                b.isActive() ? "Hoạt động" : "Ngừng"
            });
        }
    }

    private void handleAddBrand() {
        Brand b = new Brand(0, txtBrandName.getText(), txtBrandOrigin.getText(), txtBrandDesc.getText(), chkBrandActive.isSelected());
        String err = brandService.addBrand(b);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Thêm thương hiệu thành công!");
        clearBrandForm();
        loadBrands(txtSearchBrand.getText());
        notifyParentChanged();
    }

    private void handleUpdateBrand() {
        Brand b = new Brand(selectedBrandId, txtBrandName.getText(), txtBrandOrigin.getText(), txtBrandDesc.getText(), chkBrandActive.isSelected());
        String err = brandService.updateBrand(b);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Cập nhật thương hiệu thành công!");
        clearBrandForm();
        loadBrands(txtSearchBrand.getText());
        notifyParentChanged();
    }

    private void handleDeleteBrand() {
        if (selectedBrandId <= 0) {
            MessageUtils.showWarning(this, "Vui lòng chọn một thương hiệu từ bảng để xóa!");
            return;
        }
        if (!MessageUtils.showConfirm(this, "Bạn có chắc chắn muốn xóa thương hiệu này?")) {
            return;
        }
        String err = brandService.deleteBrand(selectedBrandId);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Đã xóa thương hiệu thành công!");
        clearBrandForm();
        loadBrands(txtSearchBrand.getText());
        notifyParentChanged();
    }

    private void clearBrandForm() {
        selectedBrandId = 0;
        txtBrandName.setText("");
        txtBrandOrigin.setText("");
        txtBrandDesc.setText("");
        chkBrandActive.setSelected(true);
        tblBrands.clearSelection();
    }

    private void loadCategories(String keyword) {
        modelCategories.setRowCount(0);
        List<Category> list = categoryService.searchCategories(keyword);
        for (Category c : list) {
            modelCategories.addRow(new Object[]{
                c.getCategoryId(),
                c.getCategoryName(),
                c.getDescription(),
                c.isActive() ? "Hoạt động" : "Ngừng"
            });
        }
    }

    private void handleAddCategory() {
        Category c = new Category(0, txtCategoryName.getText(), txtCategoryDesc.getText(), chkCategoryActive.isSelected());
        String err = categoryService.addCategory(c);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Thêm danh mục thành công!");
        clearCategoryForm();
        loadCategories(txtSearchCategory.getText());
        notifyParentChanged();
    }

    private void handleUpdateCategory() {
        Category c = new Category(selectedCategoryId, txtCategoryName.getText(), txtCategoryDesc.getText(), chkCategoryActive.isSelected());
        String err = categoryService.updateCategory(c);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Cập nhật danh mục thành công!");
        clearCategoryForm();
        loadCategories(txtSearchCategory.getText());
        notifyParentChanged();
    }

    private void handleDeleteCategory() {
        if (selectedCategoryId <= 0) {
            MessageUtils.showWarning(this, "Vui lòng chọn một danh mục từ bảng để xóa!");
            return;
        }
        if (!MessageUtils.showConfirm(this, "Bạn có chắc chắn muốn xóa danh mục này?")) {
            return;
        }
        String err = categoryService.deleteCategory(selectedCategoryId);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Đã xóa danh mục thành công!");
        clearCategoryForm();
        loadCategories(txtSearchCategory.getText());
        notifyParentChanged();
    }

    private void clearCategoryForm() {
        selectedCategoryId = 0;
        txtCategoryName.setText("");
        txtCategoryDesc.setText("");
        chkCategoryActive.setSelected(true);
        tblCategories.clearSelection();
    }

    private void notifyParentChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
