package com.phonestore.service;

import com.phonestore.dao.EmployeeDAO;
import com.phonestore.dao.PositionPermissionDAO;
import com.phonestore.dao.UserDAO;
import com.phonestore.model.Employee;
import com.phonestore.model.PositionPermission;
import com.phonestore.model.User;
import com.phonestore.utils.PasswordUtils;
import com.phonestore.utils.SessionManager;
import com.phonestore.utils.ValidationUtils;
import java.util.List;

/**
 * Xử lý nghiệp vụ CRUD Nhân viên, Quản lý Tài khoản đăng nhập
 * và Phân quyền theo từng Chức vụ.
 */
public class EmployeeService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PositionPermissionDAO permissionDAO = new PositionPermissionDAO();

    public List<Employee> searchEmployees(String keyword) {
        return employeeDAO.search(keyword);
    }

    public User getAccountByEmployeeId(int employeeId) {
        return userDAO.findByEmployeeId(employeeId);
    }

    public PositionPermission getPermissionByPosition(String positionName) {
        PositionPermission perm = permissionDAO.findByPosition(positionName);
        if (perm == null) {
            return new PositionPermission(positionName, true, false, true, true, true, false, false, false, false);
        }
        return perm;
    }

    public String savePositionPermission(PositionPermission perm) {
        if (!SessionManager.isAdmin()) {
            return "Chỉ Quản trị viên (ADMIN) mới có quyền sửa quyền theo Chức vụ!";
        }
        if (perm == null || ValidationUtils.isEmpty(perm.getPositionName())) {
            return "Vui lòng chọn Chức vụ cần sửa quyền!";
        }
        return permissionDAO.saveOrUpdate(perm) ? null : "Không thể lưu phân quyền cho chức vụ này!";
    }

    public String addEmployee(Employee emp) {
        String error = validateEmployee(emp, 0);
        if (error != null) {
            return error;
        }
        int newId = employeeDAO.insert(emp);
        return newId > 0 ? null : "Không thể thêm nhân viên mới vào cơ sở dữ liệu!";
    }

    public String updateEmployee(Employee emp) {
        if (emp.getEmployeeId() <= 0) {
            return "Vui lòng chọn một nhân viên từ bảng để cập nhật!";
        }
        String error = validateEmployee(emp, emp.getEmployeeId());
        if (error != null) {
            return error;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getEmployeeId() == emp.getEmployeeId()
                && !"ACTIVE".equalsIgnoreCase(emp.getStatus())) {
            return "Bạn không thể tự chuyển trạng thái ngừng làm việc cho chính mình!";
        }

        boolean updated = employeeDAO.update(emp);
        if (updated && !"ACTIVE".equalsIgnoreCase(emp.getStatus())) {
            userDAO.updateStatusByEmployeeId(emp.getEmployeeId(), "INACTIVE");
        }
        return updated ? null : "Không thể cập nhật thông tin nhân viên!";
    }

    public String deleteEmployee(int employeeId) {
        if (employeeId <= 0) {
            return "Vui lòng chọn nhân viên cần xóa!";
        }
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getEmployeeId() == employeeId) {
            return "Không thể xóa hồ sơ nhân viên đang đăng nhập hiện tại!";
        }

        if (employeeDAO.hasInvoicesOrPurchases(employeeId)) {
            employeeDAO.softDelete(employeeId);
            userDAO.updateStatusByEmployeeId(employeeId, "INACTIVE");
            return "SOFT_DELETED";
        } else {
            userDAO.deleteByEmployeeId(employeeId);
            return employeeDAO.deletePhysical(employeeId)
                    ? null
                    : "Không thể xóa nhân viên khỏi cơ sở dữ liệu!";
        }
    }

    /**
     * Cấp tài khoản mới hoặc cập nhật tài khoản cho Nhân viên.
     * Bắt buộc kiểm tra độ mạnh mật khẩu (độ dài >= 8, chữ HOA, chữ thường, số, ký tự đặc biệt)
     * và mã hóa SHA-256 trước khi lưu xuống DB.
     */
    public String saveOrUpdateAccount(int employeeId, String username, String rawPassword, String role, String status) {
        if (!SessionManager.isAdmin()) {
            return "Chỉ Quản trị viên (ADMIN) mới có quyền cấp hoặc chỉnh sửa tài khoản!";
        }
        if (employeeId <= 0) {
            return "Vui lòng chọn một nhân viên từ danh sách trước khi cấp tài khoản!";
        }
        if (!ValidationUtils.isValidUsername(username)) {
            return "Tên đăng nhập phải từ 4-30 ký tự (chỉ gồm chữ cái, số, dấu chấm, gạch dưới hoặc gạch ngang)!";
        }

        User existing = userDAO.findByEmployeeId(employeeId);
        int excludeUserId = existing == null ? 0 : existing.getUserId();
        if (userDAO.existsByUsername(username, excludeUserId)) {
            return "Tên đăng nhập '" + username.trim() + "' đã được sử dụng!";
        }

        User currentUser = SessionManager.getCurrentUser();
        if (existing != null && currentUser != null && existing.getUserId() == currentUser.getUserId()) {
            if (!"ACTIVE".equalsIgnoreCase(status)) {
                return "Bạn không thể tự khóa tài khoản Admin đang đăng nhập của chính mình!";
            }
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return "Bạn không thể tự hạ quyền Admin của chính mình!";
            }
        }

        if (existing == null) {
            // Tạo tài khoản mới -> Bắt buộc nhập mật khẩu đạt chuẩn bộ lọc
            String pwdError = ValidationUtils.validateStrongPassword(rawPassword);
            if (pwdError != null) {
                return pwdError;
            }
            User newUser = new User();
            newUser.setEmployeeId(employeeId);
            newUser.setUsername(username.trim());
            newUser.setPasswordHash(PasswordUtils.hashPassword(rawPassword));
            newUser.setRole(role);
            newUser.setStatus(status);
            return userDAO.insert(newUser) ? null : "Không thể tạo tài khoản đăng nhập!";
        } else {
            // Cập nhật tài khoản đã có
            boolean ok = userDAO.updateRoleAndStatus(existing.getUserId(), username.trim(), role, status);
            if (!ok) {
                return "Không thể cập nhật thông tin tài khoản!";
            }
            // Nếu Admin có nhập mật khẩu mới để cấp lại/reset -> Kiểm tra bộ lọc mật khẩu mạnh & mã hóa
            if (!ValidationUtils.isEmpty(rawPassword)) {
                String pwdError = ValidationUtils.validateStrongPassword(rawPassword);
                if (pwdError != null) {
                    return pwdError;
                }
                userDAO.updatePasswordHash(existing.getUserId(), PasswordUtils.hashPassword(rawPassword));
            }
            return null;
        }
    }

    private String validateEmployee(Employee emp, int excludeId) {
        if (ValidationUtils.isEmpty(emp.getEmployeeCode())) {
            return "Mã nhân viên không được để trống (VD: NV004)!";
        }
        if (employeeDAO.existsByCode(emp.getEmployeeCode(), excludeId)) {
            return "Mã nhân viên '" + emp.getEmployeeCode().trim() + "' đã tồn tại!";
        }
        if (ValidationUtils.isEmpty(emp.getFullName())) {
            return "Họ và tên nhân viên không được để trống!";
        }
        if (!ValidationUtils.isValidPhone(emp.getPhone())) {
            return "Số điện thoại không hợp lệ (phải bắt đầu bằng số 0 và gồm 10-11 chữ số)!";
        }
        if (employeeDAO.existsByPhone(emp.getPhone(), excludeId)) {
            return "Số điện thoại '" + emp.getPhone().trim() + "' đã được đăng ký cho nhân viên khác!";
        }
        if (!ValidationUtils.isValidEmail(emp.getEmail())) {
            return "Địa chỉ Email không đúng định dạng!";
        }
        if (ValidationUtils.isEmpty(emp.getPosition())) {
            return "Chức vụ không được để trống!";
        }
        return null;
    }
}
