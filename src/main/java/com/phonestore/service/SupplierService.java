package com.phonestore.service;

import com.phonestore.dao.SupplierDAO;
import com.phonestore.model.Supplier;
import com.phonestore.utils.ValidationUtils;
import java.util.List;

/**
 * Service xử lý nghiệp vụ Quản lý Nhà cung cấp.
 */
public class SupplierService {

    private final SupplierDAO supplierDAO;

    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
    }

    public SupplierService(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierDAO.findAll();
    }

    public List<Supplier> getActiveSuppliers() {
        return supplierDAO.findAllActive();
    }

    public Supplier getSupplierById(int id) {
        return supplierDAO.findById(id);
    }

    public List<Supplier> searchSuppliers(String keyword) {
        return supplierDAO.search(keyword);
    }

    /**
     * Thêm mới Nhà cung cấp.
     * @return null nếu thành công, hoặc thông báo lỗi nếu dữ liệu không hợp lệ.
     */
    public String addSupplier(Supplier s) {
        String validationError = validateSupplier(s, null);
        if (validationError != null) {
            return validationError;
        }

        boolean ok = supplierDAO.insert(s);
        return ok ? null : "Thêm nhà cung cấp thất bại! Vui lòng kiểm tra kết nối CSDL.";
    }

    /**
     * Cập nhật thông tin Nhà cung cấp.
     * @return null nếu thành công, hoặc thông báo lỗi.
     */
    public String updateSupplier(Supplier s) {
        if (s == null || s.getSupplierId() <= 0) {
            return "ID nhà cung cấp không hợp lệ!";
        }

        String validationError = validateSupplier(s, s.getSupplierId());
        if (validationError != null) {
            return validationError;
        }

        boolean ok = supplierDAO.update(s);
        return ok ? null : "Cập nhật thông tin nhà cung cấp thất bại!";
    }

    /**
     * Xóa Nhà cung cấp.
     * RÀNG BUỘC: Chỉ cho phép xóa nếu chưa có phiếu nhập liên quan.
     */
    public String deleteSupplier(int supplierId) {
        if (supplierId <= 0) {
            return "ID nhà cung cấp không hợp lệ!";
        }

        if (supplierDAO.hasPurchaseOrders(supplierId)) {
            return "Không thể xóa: Nhà cung cấp này đã có phiếu nhập trong hệ thống!\n(Gợi ý: Bạn có thể cập nhật trạng thái sang 'Ngừng hoạt động').";
        }

        boolean ok = supplierDAO.delete(supplierId);
        return ok ? null : "Xóa nhà cung cấp thất bại!";
    }

    private String validateSupplier(Supplier s, Integer excludeId) {
        if (s == null) {
            return "Dữ liệu nhà cung cấp không được để trống!";
        }
        if (ValidationUtils.isEmpty(s.getSupplierName())) {
            return "Tên nhà cung cấp không được để trống!";
        }
        if (ValidationUtils.isEmpty(s.getPhone())) {
            return "Số điện thoại nhà cung cấp không được để trống!";
        }
        if (!ValidationUtils.isValidPhone(s.getPhone())) {
            return "Số điện thoại không đúng định dạng (10-11 chữ số, bắt đầu bằng 0)!";
        }
        if (supplierDAO.isPhoneExists(s.getPhone(), excludeId)) {
            return "Số điện thoại này đã được sử dụng bởi nhà cung cấp khác!";
        }
        if (!ValidationUtils.isEmpty(s.getEmail()) && !ValidationUtils.isValidEmail(s.getEmail())) {
            return "Email không đúng định dạng!";
        }
        return null;
    }
}
