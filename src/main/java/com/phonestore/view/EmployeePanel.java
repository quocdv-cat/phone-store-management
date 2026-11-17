package com.phonestore.view;

import com.phonestore.model.Employee;
import com.phonestore.model.User;
import com.phonestore.service.EmployeeService;
import com.phonestore.utils.MessageUtils;
import com.phonestore.utils.ValidationUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý Nhân viên, Cấp tài khoản đăng nhập (Mã hóa SHA-256)
 * và nút "Sửa quyền" bật cửa sổ cấu hình quyền theo từng Chức vụ.
 */
public class EmployeePanel extends JPanel {

    private final EmployeeService employeeService = new EmployeeService();
    private final Runnable onPermissionChanged;

    // Tìm kiếm & Bảng
    private JTextField txtSearch;
    private JTable tblEmployees;
    private DefaultTableModel tableModel;
    private List<Employee> currentEmployees;
    private int selectedEmployeeId = 0;

    // 1. Form Hồ sơ Nhân viên
    private JTextField txtEmpCode;
    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JComboBox<String> cbxPosition;
    private JComboBox<String> cbxEmpStatus;

    // 2. Form Tài khoản (Mã hóa SHA-256)
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbxRole;
    private JComboBox<String> cbxAccStatus;
    private JLabel lblPasswordFilterStatus;

    public EmployeePanel() {
        this(null);
    }

    public EmployeePanel(Runnable onPermissionChanged) {
        this.onPermissionChanged = onPermissionChanged;
        initComponents();
        loadEmployees("");
    }

    public void refreshData() {
        loadEmployees(txtSearch.getText());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. TOP BAR (Tách Tiêu đề và Thanh tìm kiếm thành 2 dòng riêng biệt để không bao giờ đè chữ khi thu nhỏ cửa sổ)
        JPanel topBar = new JPanel(new BorderLayout(0, 8));
        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN & CẤP TÀI KHOẢN ĐĂNG NHẬP (ADMIN)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        topBar.add(lblTitle, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm nhân viên"));
        txtSearch = new JTextField(24);
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setBackground(new Color(37, 99, 235));
        btnSearch.setForeground(Color.WHITE);
        JButton btnReset = new JButton("Làm mới");

        btnSearch.addActionListener(e -> loadEmployees(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadEmployees(txtSearch.getText()));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadEmployees("");
        });
        searchPanel.add(new JLabel("Tìm nhân viên:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);
        topBar.add(searchPanel, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // 2. CENTER TABLE
        String[] cols = {
            "ID", "Mã NV", "Họ và Tên", "Số Điện Thoại", "Email",
            "Chức Vụ", "Trạng Thái NV", "Tài Khoản", "Quyền", "Trạng Thái TK"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblEmployees = new JTable(tableModel);
        tblEmployees.setRowHeight(27);
        tblEmployees.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblEmployees.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblEmployees.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblEmployees.getSelectedRow() >= 0) {
                populateFromSelectedRow(tblEmployees.getSelectedRow());
            }
        });
        add(new JScrollPane(tblEmployees), BorderLayout.CENTER);

        // 3. BOTTOM 2-COLUMN SPLIT:
        // Khung 1: Hồ sơ Nhân viên | Khung 2: Cấp / Quản lý Tài khoản Đăng nhập (Mã hóa SHA-256)
        JPanel bottomSplit = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomSplit.add(createEmployeeCrudPanel());
        bottomSplit.add(createAccountProvisionPanel());

        add(bottomSplit, BorderLayout.SOUTH);
    }

    private JPanel createEmployeeCrudPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("1. Hồ sơ Nhân viên"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtEmpCode = new JTextField(12);
        txtFullName = new JTextField(18);
        txtPhone = new JTextField(14);
        txtEmail = new JTextField(16);
        txtAddress = new JTextField(20);
        cbxPosition = new JComboBox<>(new String[]{
            "Nhân viên bán hàng", "Nhân viên kho", "Cửa hàng trưởng", "Kế toán", "Kỹ thuật viên"
        });
        cbxPosition.setEditable(true);
        cbxEmpStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Mã NV (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        form.add(txtEmpCode, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(new JLabel("Họ và tên (*):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        form.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Số ĐT (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        form.add(txtPhone, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        form.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("Chức vụ (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        form.add(cbxPosition, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        form.add(cbxEmpStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        form.add(txtAddress, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("Thêm NV");
        btnAdd.setBackground(new Color(22, 163, 74));
        btnAdd.setForeground(Color.WHITE);

        JButton btnUpdate = new JButton("Sửa NV");
        btnUpdate.setBackground(new Color(37, 99, 235));
        btnUpdate.setForeground(Color.WHITE);

        JButton btnDelete = new JButton("Xóa NV");
        btnDelete.setBackground(new Color(220, 38, 38));
        btnDelete.setForeground(Color.WHITE);

        JButton btnClear = new JButton("Làm mới");

        btnAdd.addActionListener(e -> handleAddEmployee());
        btnUpdate.addActionListener(e -> handleUpdateEmployee());
        btnDelete.addActionListener(e -> handleDeleteEmployee());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAccountProvisionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("2. Cấp / Quản lý Tài khoản Đăng nhập"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        cbxRole = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        cbxAccStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Tài khoản (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        form.add(txtUsername, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(new JLabel("Nhóm quyền:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        form.add(cbxRole, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Mật khẩu (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        form.add(txtPassword, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(new JLabel("Trạng thái TK:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        form.add(cbxAccStatus, gbc);

        JCheckBox chkShowPwd = new JCheckBox("Hiện mật khẩu");
        char defaultEcho = txtPassword.getEchoChar();
        chkShowPwd.addActionListener(e -> txtPassword.setEchoChar(chkShowPwd.isSelected() ? (char) 0 : defaultEcho));

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        form.add(chkShowPwd, gbc);

        lblPasswordFilterStatus = new JLabel("<html><b>Bộ lọc mật khẩu:</b> Tối thiểu 8 ký tự, có chữ HOA, chữ thường, số và ký tự đặc biệt (!, @, #, $...)</html>");
        lblPasswordFilterStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPasswordFilterStatus.setForeground(new Color(100, 116, 139));

        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePasswordChecklist();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePasswordChecklist();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePasswordChecklist();
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        form.add(lblPasswordFilterStatus, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));

        JButton btnEditRolePerm = new JButton("Sửa quyền");
        btnEditRolePerm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEditRolePerm.setBackground(new Color(234, 88, 12));
        btnEditRolePerm.setForeground(Color.WHITE);
        btnEditRolePerm.setPreferredSize(new Dimension(130, 34));
        btnEditRolePerm.addActionListener(e -> openPermissionDialog());

        JButton btnSaveAccount = new JButton("Cấp / Cập nhật Tài khoản");
        btnSaveAccount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSaveAccount.setBackground(new Color(124, 58, 237));
        btnSaveAccount.setForeground(Color.WHITE);
        btnSaveAccount.setPreferredSize(new Dimension(210, 34));
        btnSaveAccount.addActionListener(e -> handleSaveAccount());

        btnPanel.add(btnEditRolePerm);
        btnPanel.add(btnSaveAccount);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void openPermissionDialog() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        String initialPosition = cbxPosition.getSelectedItem() != null
                ? cbxPosition.getSelectedItem().toString()
                : "Nhân viên bán hàng";
        PositionPermissionDialog dialog = new PositionPermissionDialog(parentFrame, initialPosition, onPermissionChanged);
        dialog.setVisible(true);
    }

    private void updatePasswordChecklist() {
        String pwd = new String(txtPassword.getPassword());
        if (pwd.isEmpty()) {
            lblPasswordFilterStatus.setText("<html><b>Bộ lọc mật khẩu:</b> Tối thiểu 8 ký tự, có chữ HOA, chữ thường, số và ký tự đặc biệt (!, @, #, $...)</html>");
            lblPasswordFilterStatus.setForeground(new Color(100, 116, 139));
            return;
        }
        String err = ValidationUtils.validateStrongPassword(pwd);
        if (err == null) {
            lblPasswordFilterStatus.setText("<html><b>Bộ lọc mật khẩu:</b> ✓ Đạt chuẩn bảo mật (Đủ độ dài, chữ HOA, thường, số, ký tự đặc biệt)</html>");
            lblPasswordFilterStatus.setForeground(new Color(22, 163, 74));
        } else {
            lblPasswordFilterStatus.setText("<html><b>Chưa đạt:</b> " + err + "</html>");
            lblPasswordFilterStatus.setForeground(new Color(220, 38, 38));
        }
    }

    private void loadEmployees(String keyword) {
        tableModel.setRowCount(0);
        currentEmployees = employeeService.searchEmployees(keyword);
        for (Employee emp : currentEmployees) {
            User acc = employeeService.getAccountByEmployeeId(emp.getEmployeeId());
            tableModel.addRow(new Object[]{
                emp.getEmployeeId(),
                emp.getEmployeeCode(),
                emp.getFullName(),
                emp.getPhone(),
                emp.getEmail(),
                emp.getPosition(),
                emp.getStatus(),
                acc != null ? acc.getUsername() : "Chưa cấp TK",
                acc != null ? acc.getRole() : "-",
                acc != null ? acc.getStatus() : "-"
            });
        }
    }

    private void populateFromSelectedRow(int row) {
        if (currentEmployees == null || row < 0 || row >= currentEmployees.size()) {
            return;
        }
        Employee emp = currentEmployees.get(row);
        selectedEmployeeId = emp.getEmployeeId();
        txtEmpCode.setText(emp.getEmployeeCode());
        txtFullName.setText(emp.getFullName());
        txtPhone.setText(emp.getPhone());
        txtEmail.setText(emp.getEmail());
        txtAddress.setText(emp.getAddress());
        cbxPosition.setSelectedItem(emp.getPosition());
        cbxEmpStatus.setSelectedItem(emp.getStatus());

        User acc = employeeService.getAccountByEmployeeId(emp.getEmployeeId());
        if (acc != null) {
            txtUsername.setText(acc.getUsername());
            txtPassword.setText("");
            cbxRole.setSelectedItem(acc.getRole());
            cbxAccStatus.setSelectedItem(acc.getStatus());
        } else {
            txtUsername.setText("");
            txtPassword.setText("");
            cbxRole.setSelectedItem("STAFF");
            cbxAccStatus.setSelectedItem("ACTIVE");
        }
    }

    private Employee buildEmployeeFromForm() {
        Employee emp = new Employee();
        emp.setEmployeeId(selectedEmployeeId);
        emp.setEmployeeCode(txtEmpCode.getText());
        emp.setFullName(txtFullName.getText());
        emp.setPhone(txtPhone.getText());
        emp.setEmail(txtEmail.getText());
        emp.setAddress(txtAddress.getText());
        emp.setPosition(cbxPosition.getSelectedItem() != null ? cbxPosition.getSelectedItem().toString() : "");
        emp.setStatus((String) cbxEmpStatus.getSelectedItem());
        return emp;
    }

    private void handleAddEmployee() {
        Employee emp = buildEmployeeFromForm();
        String err = employeeService.addEmployee(emp);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Thêm hồ sơ nhân viên thành công! Bạn có thể chọn nhân viên này để cấp tài khoản ở khung bên phải.");
        clearForm();
        loadEmployees(txtSearch.getText());
    }

    private void handleUpdateEmployee() {
        if (selectedEmployeeId <= 0) {
            MessageUtils.showWarning(this, "Vui lòng chọn một nhân viên từ bảng để cập nhật!");
            return;
        }
        Employee emp = buildEmployeeFromForm();
        String err = employeeService.updateEmployee(emp);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Cập nhật hồ sơ nhân viên thành công!");
        clearForm();
        loadEmployees(txtSearch.getText());
    }

    private void handleDeleteEmployee() {
        if (selectedEmployeeId <= 0) {
            MessageUtils.showWarning(this, "Vui lòng chọn một nhân viên từ bảng để xóa!");
            return;
        }
        if (!MessageUtils.showConfirm(this, "Bạn có chắc chắn muốn xóa / ngừng làm việc nhân viên này?")) {
            return;
        }
        String result = employeeService.deleteEmployee(selectedEmployeeId);
        if ("SOFT_DELETED".equals(result)) {
            MessageUtils.showInfo(this, "Nhân viên đã có lịch sử lập Hóa đơn/Phiếu nhập nên được chuyển sang 'INACTIVE' và khóa tài khoản!");
        } else if (result != null) {
            MessageUtils.showError(this, result);
            return;
        } else {
            MessageUtils.showInfo(this, "Đã xóa nhân viên và tài khoản liên kết thành công!");
        }
        clearForm();
        loadEmployees(txtSearch.getText());
    }

    private void handleSaveAccount() {
        if (selectedEmployeeId <= 0) {
            MessageUtils.showWarning(this, "Vui lòng click chọn một nhân viên trên bảng trước khi cấp hoặc cập nhật tài khoản!");
            return;
        }
        String username = txtUsername.getText();
        String rawPassword = new String(txtPassword.getPassword());
        String role = (String) cbxRole.getSelectedItem();
        String status = (String) cbxAccStatus.getSelectedItem();

        String err = employeeService.saveOrUpdateAccount(selectedEmployeeId, username, rawPassword, role, status);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Đã lưu tài khoản đăng nhập thành công!");
        txtPassword.setText("");
        loadEmployees(txtSearch.getText());
    }

    private void clearForm() {
        selectedEmployeeId = 0;
        txtEmpCode.setText("");
        txtFullName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        cbxPosition.setSelectedIndex(0);
        cbxEmpStatus.setSelectedItem("ACTIVE");

        txtUsername.setText("");
        txtPassword.setText("");
        cbxRole.setSelectedItem("STAFF");
        cbxAccStatus.setSelectedItem("ACTIVE");
        tblEmployees.clearSelection();
    }
}
