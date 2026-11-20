package com.phonestore.view;

import com.phonestore.model.Customer;
import com.phonestore.service.CustomerService;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
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
 * Hộp thoại Popup thêm nhanh khách hàng theo SĐT từ màn hình bán hàng POS.
 * Phụ trách bởi Thành viên 2 (Trung - Core Transaction Developer).
 */
public class QuickCustomerDialog extends JDialog {

    private final CustomerService customerService;
    private Customer createdCustomer = null;

    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;

    public QuickCustomerDialog(Window parent, String initialPhone) {
        super(parent, "Tạo Nhanh Khách Hàng - Mobile Store POS", ModalityType.APPLICATION_MODAL);
        this.customerService = new CustomerService();
        initComponents(initialPhone);
    }

    /**
     * Phương thức tĩnh mở popup và trả về đối tượng Customer vừa tạo thành công.
     * @param parent Cửa sổ cha (SalesPanel, MainFrame,...)
     * @param initialPhone Số điện thoại nhập từ ô tìm kiếm khách hàng
     * @return Customer đã được lưu vào CSDL với ID tự sinh, hoặc null nếu hủy
     */
    public static Customer showDialog(Window parent, String initialPhone) {
        QuickCustomerDialog dialog = new QuickCustomerDialog(parent, initialPhone);
        dialog.setVisible(true);
        return dialog.getCreatedCustomer();
    }

    public Customer getCreatedCustomer() {
        return createdCustomer;
    }

    private void initComponents(String initialPhone) {
        setSize(460, 360);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("TẠO NHANH KHÁCH HÀNG MỚI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Thêm nhanh để áp dụng ngay vào hóa đơn bán hàng POS");
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

        // 1. Số điện thoại (*)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblPhone = new JLabel("Số điện thoại (*):");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblPhone, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (initialPhone != null && !initialPhone.trim().isEmpty()) {
            txtPhone.setText(initialPhone.trim());
        }
        formPanel.add(txtPhone, gbc);

        // 2. Họ và tên (*)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblName = new JLabel("Họ và tên (*):");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblName, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtFullName = new JTextField();
        txtFullName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtFullName, gbc);

        // 3. Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(lblEmail, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtEmail, gbc);

        // 4. Địa chỉ
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel lblAddress = new JLabel("Địa chỉ:");
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(lblAddress, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtAddress = new JTextField();
        txtAddress.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtAddress, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Footer Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnSave = new JButton("Tạo & Chọn Ngay");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(150, 35));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(90, 35));

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);
        add(footerPanel, BorderLayout.SOUTH);

        // Events
        btnSave.addActionListener(e -> saveCustomer());
        btnCancel.addActionListener(e -> dispose());

        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveCustomer();
                }
            }
        };
        txtFullName.addKeyListener(enterSubmit);
        txtPhone.addKeyListener(enterSubmit);
        txtEmail.addKeyListener(enterSubmit);
        txtAddress.addKeyListener(enterSubmit);

        // Esc to close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Auto-focus
        if (txtPhone.getText().isEmpty()) {
            txtPhone.requestFocusInWindow();
        } else {
            txtFullName.requestFocusInWindow();
        }
    }

    private void saveCustomer() {
        String name = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        Customer customer = new Customer(name, phone, email, address);

        try {
            Customer saved = customerService.addCustomer(customer);
            this.createdCustomer = saved;
            MessageUtils.showInfo(this, "Tạo khách hàng '" + saved.getFullName() + "' thành công!");
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            MessageUtils.showWarning(this, ex.getMessage());
        } catch (Exception ex) {
            MessageUtils.showError(this, "Lỗi khi lưu thông tin: " + ex.getMessage());
        }
    }
}
