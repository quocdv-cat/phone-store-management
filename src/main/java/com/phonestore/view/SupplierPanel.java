package com.phonestore.view;

import com.phonestore.model.Supplier;
import com.phonestore.service.SupplierService;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý Nhà cung cấp (SupplierPanel).
 * Thuộc Module 6 - Phân hệ TV3.
 */
public class SupplierPanel extends JPanel {

    private final SupplierService supplierService = new SupplierService();

    private JTextField txtSupplierId;
    private JTextField txtSupplierName;
    private JTextField txtContactPerson;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JCheckBox chkActive;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnRefreshSearch;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private JTable tblSuppliers;
    private DefaultTableModel tableModel;

    public SupplierPanel() {
        initComponents();
        loadData("");
    }

    public void setEditAllowed(boolean allowed) {
        if (btnAdd != null) btnAdd.setEnabled(allowed);
        if (btnUpdate != null) btnUpdate.setEnabled(allowed);
        if (btnDelete != null) btnDelete.setEnabled(allowed);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. HEADER & THANH TÌM KIẾM (Tách 2 dòng để không đè chữ khi thu nhỏ cửa sổ)
        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        JLabel lblTitle = new JLabel("QUẢN LÝ NHÀ CUNG CẤP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBox.setBorder(BorderFactory.createTitledBorder("Tìm kiếm nhà cung cấp"));
        txtSearch = new JTextField(22);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc SĐT...");
        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(37, 99, 235));
        btnSearch.setForeground(Color.WHITE);
        btnRefreshSearch = new JButton("Làm mới");

        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnRefreshSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadData("");
        });

        searchBox.add(new JLabel("Tìm kiếm:"));
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnRefreshSearch);
        headerPanel.add(searchBox, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. THÂN GIAO DIỆN: CỘT TRÁI (FORM NHẬP LIỆU) - CỘT PHẢI (BẢNG DỮ LIỆU)
        JPanel bodyPanel = new JPanel(new BorderLayout(15, 0));

        // --- FORM BÊN TRÁI ---
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setPreferredSize(new Dimension(360, 0));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Thông tin Nhà cung cấp", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtSupplierId = new JTextField();
        txtSupplierId.setEditable(false);
        txtSupplierId.setEnabled(false);

        txtSupplierName = new JTextField();
        txtContactPerson = new JTextField();
        txtPhone = new JTextField();
        txtEmail = new JTextField();
        txtAddress = new JTextField();
        chkActive = new JCheckBox("Đang hoạt động", true);

        // Mã NCC (ID)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Mã NCC:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        fieldsPanel.add(txtSupplierId, gbc);

        // Tên NCC
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Tên NCC (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        fieldsPanel.add(txtSupplierName, gbc);

        // Người liên hệ
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Người liên hệ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        fieldsPanel.add(txtContactPerson, gbc);

        // Số điện thoại
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Điện thoại (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        fieldsPanel.add(txtPhone, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.7;
        fieldsPanel.add(txtEmail, gbc);

        // Địa chỉ
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.7;
        fieldsPanel.add(txtAddress, gbc);

        // Trạng thái
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        fieldsPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.7;
        fieldsPanel.add(chkActive, gbc);

        formPanel.add(fieldsPanel, BorderLayout.NORTH);

        // Các nút thao tác
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        btnAdd = new JButton("Thêm mới");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xóa NCC");
        btnClear = new JButton("Làm mới");

        btnAdd.setBackground(new Color(16, 185, 129));
        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(59, 130, 246));
        btnUpdate.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(239, 68, 68));
        btnDelete.setForeground(Color.WHITE);

        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());

        actionsPanel.add(btnAdd);
        actionsPanel.add(btnUpdate);
        actionsPanel.add(btnDelete);
        actionsPanel.add(btnClear);
        formPanel.add(actionsPanel, BorderLayout.SOUTH);

        bodyPanel.add(formPanel, BorderLayout.WEST);

        // --- BẢNG DANH SÁCH BÊN PHẢI ---
        String[] columns = {"ID", "Tên Nhà Cung Cấp", "Người Liên Hệ", "Số Điện Thoại", "Email", "Địa Chỉ", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblSuppliers = new JTable(tableModel);
        tblSuppliers.setRowHeight(28);
        tblSuppliers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSuppliers.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        tblSuppliers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblSuppliers.getSelectedRow();
                if (row >= 0) {
                    fillFormFromRow(row);
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblSuppliers);
        scrollTable.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Danh sách Nhà cung cấp trong hệ thống", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)));
        bodyPanel.add(scrollTable, BorderLayout.CENTER);

        add(bodyPanel, BorderLayout.CENTER);
    }

    public void loadData(String keyword) {
        tableModel.setRowCount(0);
        List<Supplier> list = supplierService.searchSuppliers(keyword);
        for (Supplier s : list) {
            tableModel.addRow(new Object[]{
                s.getSupplierId(),
                s.getSupplierName(),
                s.getContactPerson() != null ? s.getContactPerson() : "",
                s.getPhone(),
                s.getEmail() != null ? s.getEmail() : "",
                s.getAddress() != null ? s.getAddress() : "",
                s.isActive() ? "Đang hoạt động" : "Ngừng hoạt động"
            });
        }
    }

    private void fillFormFromRow(int row) {
        txtSupplierId.setText(tableModel.getValueAt(row, 0).toString());
        txtSupplierName.setText(tableModel.getValueAt(row, 1).toString());
        txtContactPerson.setText(tableModel.getValueAt(row, 2).toString());
        txtPhone.setText(tableModel.getValueAt(row, 3).toString());
        txtEmail.setText(tableModel.getValueAt(row, 4).toString());
        txtAddress.setText(tableModel.getValueAt(row, 5).toString());
        chkActive.setSelected("Đang hoạt động".equals(tableModel.getValueAt(row, 6).toString()));
    }

    private void clearForm() {
        txtSupplierId.setText("");
        txtSupplierName.setText("");
        txtContactPerson.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        chkActive.setSelected(true);
        tblSuppliers.clearSelection();
    }

    private void onAdd() {
        Supplier s = getSupplierFromForm();
        String err = supplierService.addSupplier(s);
        if (err != null) {
            MessageUtils.showError(this, err);
        } else {
            MessageUtils.showInfo(this, "Thêm nhà cung cấp mới thành công!");
            clearForm();
            loadData("");
        }
    }

    private void onUpdate() {
        String idText = txtSupplierId.getText().trim();
        if (idText.isEmpty()) {
            MessageUtils.showWarning(this, "Vui lòng chọn nhà cung cấp từ bảng để cập nhật!");
            return;
        }

        Supplier s = getSupplierFromForm();
        s.setSupplierId(Integer.parseInt(idText));

        String err = supplierService.updateSupplier(s);
        if (err != null) {
            MessageUtils.showError(this, err);
        } else {
            MessageUtils.showInfo(this, "Cập nhật thông tin nhà cung cấp thành công!");
            clearForm();
            loadData("");
        }
    }

    private void onDelete() {
        String idText = txtSupplierId.getText().trim();
        if (idText.isEmpty()) {
            MessageUtils.showWarning(this, "Vui lòng chọn nhà cung cấp cần xóa từ bảng!");
            return;
        }

        int id = Integer.parseInt(idText);
        String name = txtSupplierName.getText().trim();

        if (MessageUtils.showConfirm(this, "Bạn có chắc chắn muốn xóa nhà cung cấp \"" + name + "\"?\nLưu ý: Chỉ xóa được nếu chưa phát sinh phiếu nhập.")) {
            String err = supplierService.deleteSupplier(id);
            if (err != null) {
                MessageUtils.showError(this, err);
            } else {
                MessageUtils.showInfo(this, "Xóa nhà cung cấp thành công!");
                clearForm();
                loadData("");
            }
        }
    }

    private Supplier getSupplierFromForm() {
        Supplier s = new Supplier();
        s.setSupplierName(txtSupplierName.getText().trim());
        s.setContactPerson(txtContactPerson.getText().trim());
        s.setPhone(txtPhone.getText().trim());
        s.setEmail(txtEmail.getText().trim());
        s.setAddress(txtAddress.getText().trim());
        s.setActive(chkActive.isSelected());
        return s;
    }
}
