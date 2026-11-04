package com.phonestore.service;

import com.phonestore.dao.EmployeeDAO;
import com.phonestore.dao.UserDAO;
import com.phonestore.model.Employee;
import com.phonestore.model.User;
import com.phonestore.utils.SessionManager;
import com.phonestore.utils.ValidationUtils;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public String login(String username, String password) {
        if (ValidationUtils.isEmpty(username) || ValidationUtils.isEmpty(password)) {
            return "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!";
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null || !user.getPasswordHash().equals(password)) {
            return "Sai tên đăng nhập hoặc mật khẩu!";
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return "Tài khoản đã bị khóa. Vui lòng liên hệ Quản trị viên!";
        }

        Employee emp = employeeDAO.findById(user.getEmployeeId());
        SessionManager.login(user, emp);
        return null; // null = Đăng nhập thành công
    }

    public void logout() {
        SessionManager.logout();
    }
}
