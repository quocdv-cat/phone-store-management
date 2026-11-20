package com.phonestore.service;

import com.phonestore.dao.CustomerDAO;
import com.phonestore.dao.InvoiceDAO;
import com.phonestore.model.Customer;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import com.phonestore.utils.ValidationUtils;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service xử lý nghiệp vụ quản lý khách hàng.
 * Thực hiện kiểm tra ràng buộc nghiệp vụ (validation, tính duy nhất SĐT, kiểm tra lịch sử mua hàng trước khi xóa).
 */
public class CustomerService {

    private final CustomerDAO customerDAO;
    private final InvoiceDAO invoiceDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    public CustomerService(CustomerDAO customerDAO, InvoiceDAO invoiceDAO) {
        this.customerDAO = customerDAO;
        this.invoiceDAO = invoiceDAO;
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public List<Customer> searchCustomers(String keyword) {
        return customerDAO.search(keyword);
    }

    public Customer getCustomerById(int customerId) {
        return customerDAO.findById(customerId);
    }

    public Customer getCustomerByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        return customerDAO.findByPhone(phone.trim());
    }

    /**
     * Thêm mới khách hàng với đầy đủ kiểm tra dữ liệu đầu vào.
     * @return Đối tượng Customer đã được lưu thành công kèm theo customerId vừa sinh.
     */
    public Customer addCustomer(Customer customer) {
        validateCustomerData(customer, null);

        boolean success = customerDAO.insert(customer);
        if (!success) {
            throw new RuntimeException("Lỗi hệ thống khi lưu thông tin khách hàng vào cơ sở dữ liệu!");
        }
        return customer;
    }

    /**
     * Cập nhật thông tin khách hàng.
     */
    public void updateCustomer(Customer customer) {
        if (customer == null || customer.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Khách hàng không hợp lệ!");
        }

        validateCustomerData(customer, customer.getCustomerId());

        boolean success = customerDAO.update(customer);
        if (!success) {
            throw new RuntimeException("Cập nhật thông tin khách hàng thất bại!");
        }
    }

    /**
     * Xóa khách hàng với điều kiện: Chỉ xóa được khách hàng chưa từng mua hàng.
     */
    public void deleteCustomer(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ!");
        }

        // 1. Kiểm tra xem khách hàng đã từng phát sinh hóa đơn hay chưa
        int invoiceCount = invoiceDAO.countInvoicesByCustomerId(customerId);
        if (invoiceCount > 0) {
            throw new IllegalStateException("Không thể xóa! Khách hàng này đã có " + invoiceCount
                    + " hóa đơn mua hàng trong hệ thống (dữ liệu giao dịch được bảo vệ).");
        }

        // 2. Thực hiện xóa
        boolean success = customerDAO.delete(customerId);
        if (!success) {
            throw new RuntimeException("Xóa khách hàng thất bại hoặc khách hàng không còn tồn tại!");
        }
    }

    /**
     * Kiểm tra xem khách hàng đã từng mua hàng hay chưa.
     */
    public boolean hasCustomerPurchased(int customerId) {
        return invoiceDAO.countInvoicesByCustomerId(customerId) > 0;
    }

    /**
     * Lấy danh sách lịch sử các hóa đơn mà khách hàng đã mua.
     */
    public List<Invoice> getCustomerInvoices(int customerId) {
        return invoiceDAO.findByCustomerId(customerId);
    }

    /**
     * Lấy danh sách sản phẩm chi tiết của một hóa đơn.
     */
    public List<InvoiceDetail> getInvoiceDetails(int invoiceId) {
        return invoiceDAO.findDetailsByInvoiceId(invoiceId);
    }

    /**
     * Lấy tổng chi tiêu tích lũy của khách hàng.
     */
    public BigDecimal getTotalSpent(int customerId) {
        return invoiceDAO.getTotalSpentByCustomerId(customerId);
    }

    /**
     * Hàm kiểm tra dữ liệu đầu vào chuẩn hóa theo nghiệp vụ.
     */
    private void validateCustomerData(Customer customer, Integer currentCustomerId) {
        if (customer == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không được để trống!");
        }

        // Họ tên
        if (ValidationUtils.isEmpty(customer.getFullName())) {
            throw new IllegalArgumentException("Họ và tên khách hàng không được để trống!");
        }
        String name = customer.getFullName().trim();
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Họ và tên khách hàng phải từ 2 đến 100 ký tự!");
        }

        // Số điện thoại (bắt buộc, đúng 10 chữ số bắt đầu bằng 0)
        String phone = customer.getPhone();
        if (ValidationUtils.isEmpty(phone)) {
            throw new IllegalArgumentException("Số điện thoại không được để trống!");
        }
        phone = phone.trim();
        if (!ValidationUtils.isValidPhone10Digits(phone)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ! Vui lòng nhập đúng 10 chữ số (bắt đầu bằng 0, ví dụ: 0912345678).");
        }

        // Số điện thoại phải là duy nhất
        if (customerDAO.isPhoneExists(phone, currentCustomerId)) {
            throw new IllegalArgumentException("Số điện thoại '" + phone + "' đã tồn tại trên hệ thống cho khách hàng khác!");
        }

        // Email (nếu nhập thì phải đúng định dạng)
        String email = customer.getEmail();
        if (!ValidationUtils.isEmpty(email)) {
            email = email.trim();
            if (!ValidationUtils.isValidEmail(email)) {
                throw new IllegalArgumentException("Địa chỉ email không đúng định dạng (ví dụ: email@domain.com)!");
            }
            if (email.length() > 100) {
                throw new IllegalArgumentException("Địa chỉ email không được vượt quá 100 ký tự!");
            }
        }

        // Địa chỉ
        String address = customer.getAddress();
        if (!ValidationUtils.isEmpty(address) && address.trim().length() > 255) {
            throw new IllegalArgumentException("Địa chỉ không được vượt quá 255 ký tự!");
        }
    }
}
