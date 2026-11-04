package com.phonestore.view;

import com.phonestore.service.AuthService;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập - Quản lý Cửa hàng Điện thoại Di động");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 340);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 15));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        rootPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("PHONE STORE MANAGEMENT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(25, 118, 210));
        rootPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Tài khoản:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtUsername = new JTextField("admin");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(220, 34));
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPassword = new JPasswordField("123456");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(220, 34));
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel lblHint = new JLabel("Mẫu thử: admin / 123456 hoặc nhanvien1 / 123456", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(Color.GRAY);
        formPanel.add(lblHint, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        btnLogin = new JButton("ĐĂNG NHẬP HỆ THỐNG");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(25, 118, 210));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(100, 42));
        btnLogin.addActionListener(e -> handleLogin());

        getRootPane().setDefaultButton(btnLogin);
        rootPanel.add(btnLogin, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        String error = authService.login(username, password);
        if (error != null) {
            MessageUtils.showError(this, error);
            return;
        }

        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        dispose();
    }
}
