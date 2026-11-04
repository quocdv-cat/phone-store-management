package com.phonestore;

import com.formdev.flatlaf.FlatLightLaf;
import com.phonestore.view.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Điểm khởi chạy chính của ứng dụng Quản lý Cửa hàng Điện thoại Di động.
 */
public class PhoneStoreManagement {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo giao diện FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
