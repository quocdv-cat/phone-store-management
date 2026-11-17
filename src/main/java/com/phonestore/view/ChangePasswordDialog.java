package com.phonestore.view;

import com.phonestore.service.AuthService;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Hộp thoại Đổi mật khẩu cá nhân (tích hợp kiểm tra mật khẩu mạnh & mã hóa SHA-256).
 */
public class ChangePasswordDialog extends JDialog {

    private final AuthService authService = new AuthService();
    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    public ChangePasswordDialog(Frame parent) {
        super(parent, "Đổi mật khẩu tài khoản", true);
        initComponents();
    }

    private void initComponents() {
        setSize(480, 340);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel("ĐỔI MẬT KHẨU ĐĂNG NHẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(25, 118, 210));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCurrentPassword = new JPasswordField(20);
        txtNewPassword = new JPasswordField(20);
        txtConfirmPassword = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        form.add(new JLabel("Mật khẩu hiện tại (*):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        form.add(txtCurrentPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Mật khẩu mới (*):"), gbc);
        gbc.gridx = 1;
        form.add(txtNewPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Xác nhận mật khẩu mới (*):"), gbc);
        gbc.gridx = 1;
        form.add(txtConfirmPassword, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        JCheckBox chkShow = new JCheckBox("Hiển thị mật khẩu");
        char defaultEcho = txtCurrentPassword.getEchoChar();
        chkShow.addActionListener(e -> {
            char echo = chkShow.isSelected() ? (char) 0 : defaultEcho;
            txtCurrentPassword.setEchoChar(echo);
            txtNewPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });
        form.add(chkShow, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JLabel lblRule = new JLabel("<html><i>* Yêu cầu: Tối thiểu 8 ký tự, gồm chữ HOA, chữ thường, số và ký tự đặc biệt (!, @, #, $...)</i></html>");
        lblRule.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRule.setForeground(new Color(100, 116, 139));
        form.add(lblRule, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnSave = new JButton("Lưu mật khẩu mới");
        btnSave.setBackground(new Color(22, 163, 74));
        btnSave.setForeground(Color.WHITE);
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> handleSave());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleSave() {
        String currentPwd = new String(txtCurrentPassword.getPassword());
        String newPwd = new String(txtNewPassword.getPassword());
        String confirmPwd = new String(txtConfirmPassword.getPassword());

        String err = authService.changePassword(currentPwd, newPwd, confirmPwd);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }
        MessageUtils.showInfo(this, "Đổi mật khẩu thành công!");
        dispose();
    }
}
