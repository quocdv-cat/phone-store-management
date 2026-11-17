package com.phonestore.utils;

import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra tính hợp lệ của dữ liệu đầu vào và bộ lọc độ mạnh mật khẩu.
 */
public class ValidationUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{4,30}$");

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*");

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

    public static boolean isValidUsername(String username) {
        return !isEmpty(username) && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Bộ lọc mật khẩu mạnh khi Admin cấp tài khoản cho Staff hoặc khi đổi mật khẩu:
     * - Độ dài tối thiểu 8 ký tự
     * - Ít nhất 1 chữ cái IN HOA (A-Z)
     * - Ít nhất 1 chữ cái thường (a-z)
     * - Ít nhất 1 chữ số (0-9)
     * - Ít nhất 1 ký tự đặc biệt (!, @, #, $, %, ^, &, *...)
     *
     * @return null nếu hợp lệ, hoặc chuỗi thông báo lỗi chi tiết nếu chưa đạt.
     */
    public static String validateStrongPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống!";
        }
        if (password.length() < 8) {
            return "Mật khẩu phải có độ dài tối thiểu từ 8 ký tự trở lên!";
        }
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            return "Mật khẩu phải chứa ít nhất 1 chữ cái IN HOA (A-Z)!";
        }
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            return "Mật khẩu phải chứa ít nhất 1 chữ cái viết thường (a-z)!";
        }
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            return "Mật khẩu phải chứa ít nhất 1 chữ số (0-9)!";
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            return "Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (ví dụ: !, @, #, $, %, ^, &, *...)!";
        }
        return null;
    }
}
