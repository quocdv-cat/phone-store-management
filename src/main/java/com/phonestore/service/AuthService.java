package com.phonestore.service;

import com.phonestore.dao.EmployeeDAO;
import com.phonestore.dao.UserDAO;
import com.phonestore.model.Employee;
import com.phonestore.model.User;
import com.phonestore.utils.PasswordUtils;
import com.phonestore.utils.SessionManager;
import com.phonestore.utils.ValidationUtils;

/**
 * Xử lý nghiệp vụ Đăng nhập (so khớp mật khẩu mã hóa SHA-256), Đăng xuất và Đổi mật khẩu.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public String login(String username, String password) {
        if (ValidationUtils.isEmpty(username) || ValidationUtils.isEmpty(password)) {
            return "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!";
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            return "Sai tên đăng nhập hoặc mật khẩu!";
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return "Tài khoản này đã bị khóa. Vui lòng liên hệ Quản trị viên!";
        }

        Employee emp = employeeDAO.findById(user.getEmployeeId());
        if (emp != null && !"ACTIVE".equalsIgnoreCase(emp.getStatus())) {
            return "Hồ sơ nhân viên của tài khoản này đã ngừng hoạt động!";
        }

        SessionManager.login(user, emp);
        return null; // null = Đăng nhập thành công
    }

    public String changePassword(String currentPassword, String newPassword, String confirmPassword) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            return "Phiên đăng nhập không hợp lệ!";
        }
        if (ValidationUtils.isEmpty(currentPassword)) {
            return "Vui lòng nhập mật khẩu hiện tại!";
        }
        if (!PasswordUtils.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
            return "Mật khẩu hiện tại không chính xác!";
        }

        String passwordRuleError = ValidationUtils.validateStrongPassword(newPassword);
        if (passwordRuleError != null) {
            return passwordRuleError;
        }

        if (!newPassword.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp với mật khẩu mới!";
        }
        if (currentPassword.equals(newPassword)) {
            return "Mật khẩu mới phải khác mật khẩu hiện tại!";
        }

        String newHash = PasswordUtils.hashPassword(newPassword);
        boolean updated = userDAO.updatePasswordHash(currentUser.getUserId(), newHash);
        if (!updated) {
            return "Lỗi cơ sở dữ liệu khi cập nhật mật khẩu!";
        }

        currentUser.setPasswordHash(newHash);
        return null;
    }

    public void logout() {
        SessionManager.logout();
    }
}
