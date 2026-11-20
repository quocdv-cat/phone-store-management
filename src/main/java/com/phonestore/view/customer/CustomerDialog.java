package com.phonestore.view.customer;

import com.phonestore.model.Customer;
import com.phonestore.service.CustomerService;
import com.phonestore.utils.DateUtils;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Hộp thoại Thêm mới hoặc Chỉnh sửa thông tin khách hàng.
 */
public class CustomerDialog extends JDialog {

    private final CustomerService customerService;
    private final Customer customer;
    private final boolean isEditMode;
    private boolean saved = false;

    private JTextField txtId;
    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JTextField txtCreatedAt;
    private JTextField txtOrders;

    public CustomerDialog(Window parent, Customer customerToEdit) {
        super(parent, customerToEdit == null ? "Thêm Mới Khách Hàng" : "Chỉnh Sửa Thông Tin Khách Hàng", ModalityType.APPLICATION_MODAL);
        this.customerService = new CustomerService();
        this.customer = customerToEdit;
        this.isEditMode = (customerToEdit != null);

        initComponents();
        populateData();
    }

    public boolean isSaved() {
        return saved;
    }

    private void initComponents() {
        setSize(520, isEditMode ? 460 : 380);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel(isEditMode ? "CHỈNH SỬA KHÁCH HÀNG" : "THÊM MỚI KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(isEditMode
                ? "Cập nhật thông tin chi tiết của khách hàng #" + customer.getCustomerId()
                : "Điền đầy đủ các trường thông tin bắt buộc (*)");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(148, 163, 184));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Mã KH (chỉ hiển thị khi chỉnh sửa)
        if (isEditMode) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.3;
            JLabel lblId = new JLabel("Mã khách hàng:");
            lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
            formPanel.add(lblId, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            txtId = new JTextField();
            txtId.setEditable(false);
            txtId.setEnabled(false);
            txtId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(txtId, gbc);
            row++;
        }

        // Họ và tên (*)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblName = new JLabel("Họ và tên (*):");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblName, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtFullName = new JTextField();
        txtFullName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtFullName, gbc);
        row++;

        // Số điện thoại (*)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblPhone = new JLabel("Số điện thoại (*):");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblPhone, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtPhone, gbc);
        row++;

        // Email
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(lblEmail, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtEmail, gbc);
        row++;

        // Địa chỉ
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblAddress = new JLabel("Địa chỉ:");
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(lblAddress, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtAddress = new JTextField();
        txtAddress.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtAddress, gbc);
        row++;

        // Ngày tạo & Số đơn hàng (chỉ hiển thị khi chỉnh sửa)
        if (isEditMode) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.3;
            JLabel lblCreated = new JLabel("Ngày tạo:");
            lblCreated.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(lblCreated, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            txtCreatedAt = new JTextField();
            txtCreatedAt.setEditable(false);
            txtCreatedAt.setEnabled(false);
            txtCreatedAt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(txtCreatedAt, gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.3;
            JLabel lblOrderCount = new JLabel("Tổng đơn đã mua:");
            lblOrderCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(lblOrderCount, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            txtOrders = new JTextField();
            txtOrders.setEditable(false);
            txtOrders.setEnabled(false);
            txtOrders.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(txtOrders, gbc);
            row++;
        }

        add(formPanel, BorderLayout.CENTER);

        // Footer Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnSave = new JButton(isEditMode ? "Lưu Thay Đổi" : "Thêm Khách Hàng");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        JButton btnCancel = new JButton("Đóng");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setFocusPainted(false);

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);
        add(footerPanel, BorderLayout.SOUTH);

        // Actions
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());

        // Keyboard ESC to close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void populateData() {
        if (isEditMode && customer != null) {
            txtId.setText(String.valueOf(customer.getCustomerId()));
            txtFullName.setText(customer.getFullName());
            txtPhone.setText(customer.getPhone());
            txtEmail.setText(customer.getEmail() != null ? customer.getEmail() : "");
            txtAddress.setText(customer.getAddress() != null ? customer.getAddress() : "");
            txtCreatedAt.setText(DateUtils.formatDateTime(customer.getCreatedAt()));
            txtOrders.setText(customer.getTotalOrders() + " hóa đơn");
        }
    }

    private void save() {
        String name = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        try {
            if (isEditMode) {
                customer.setFullName(name);
                customer.setPhone(phone);
                customer.setEmail(email);
                customer.setAddress(address);
                customerService.updateCustomer(customer);
                MessageUtils.showInfo(this, "Cập nhật thông tin khách hàng thành công!");
            } else {
                Customer newCustomer = new Customer(name, phone, email, address);
                customerService.addCustomer(newCustomer);
                MessageUtils.showInfo(this, "Thêm mới khách hàng thành công!");
            }
            this.saved = true;
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            MessageUtils.showWarning(this, ex.getMessage());
        } catch (Exception ex) {
            MessageUtils.showError(this, "Lỗi: " + ex.getMessage());
        }
    }
}
