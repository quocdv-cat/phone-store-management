package com.phonestore.utils;

import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra tính hợp lệ của dữ liệu đầu vào.
 */
public class ValidationUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ValidationUtils() {
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        return !isEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return isEmpty(email) || EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
