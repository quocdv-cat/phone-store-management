package com.phonestore.utils;

import com.phonestore.model.Employee;
import com.phonestore.model.User;

/**
 * Lưu trữ thông tin phiên đăng nhập hiện tại của người dùng.
 */
public class SessionManager {

    private static User currentUser;
    private static Employee currentEmployee;

    private SessionManager() {
    }

    public static void login(User user, Employee employee) {
        currentUser = user;
        currentEmployee = employee;
    }

    public static void logout() {
        currentUser = null;
        currentEmployee = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
