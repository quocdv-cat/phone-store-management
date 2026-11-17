package com.phonestore.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích mã hóa mật khẩu bằng thuật toán băm SHA-256 (không lưu mật khẩu dạng plain-text).
 */
public class PasswordUtils {

    private PasswordUtils() {
    }

    /**
     * Mã hóa mật khẩu thuần sang chuỗi Hex SHA-256 (64 ký tự).
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Lỗi thuật toán mã hóa SHA-256", ex);
        }
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập vào có khớp với chuỗi băm trong DB hay không.
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String hashedInput = hashPassword(rawPassword);
        return hashedInput.equalsIgnoreCase(storedHash.trim());
    }
}
