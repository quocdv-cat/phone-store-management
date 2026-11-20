package com.phonestore.service;

import com.phonestore.model.Customer;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomerServiceTest {

    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        customerService = new CustomerService();
    }

    @Test
    public void testAddCustomerSuccessAndSearchAndDelete() {
        String testPhone = "0999888777";
        // Dọn dẹp nếu đã tồn tại từ lần chạy trước
        Customer existing = customerService.getCustomerByPhone(testPhone);
        if (existing != null) {
            customerService.deleteCustomer(existing.getCustomerId());
        }

        // 1. Thêm mới thành công
        Customer customer = new Customer("Nguyễn Văn Thử Nghiệm", testPhone, "test.user@gmail.com", "123 Đường Test, Hà Nội");
        Customer saved = customerService.addCustomer(customer);
        Assertions.assertTrue(saved.getCustomerId() > 0, "Customer ID phải được tự động sinh > 0");

        // 2. Tìm kiếm nhanh theo Số điện thoại
        List<Customer> searchByPhone = customerService.searchCustomers(testPhone);
        Assertions.assertFalse(searchByPhone.isEmpty());
        Assertions.assertEquals(saved.getCustomerId(), searchByPhone.get(0).getCustomerId());

        // 3. Tìm kiếm nhanh theo Họ tên
        List<Customer> searchByName = customerService.searchCustomers("Thử Nghiệm");
        Assertions.assertFalse(searchByName.isEmpty());

        // 4. Cập nhật thông tin
        saved.setFullName("Nguyễn Văn Đã Cập Nhật");
        saved.setAddress("456 Đường Mới");
        customerService.updateCustomer(saved);

        Customer updated = customerService.getCustomerById(saved.getCustomerId());
        Assertions.assertEquals("Nguyễn Văn Đã Cập Nhật", updated.getFullName());
        Assertions.assertEquals("456 Đường Mới", updated.getAddress());

        // 5. Xóa khách hàng chưa từng mua hàng -> Thành công
        Assertions.assertFalse(customerService.hasCustomerPurchased(saved.getCustomerId()));
        customerService.deleteCustomer(saved.getCustomerId());

        Customer afterDelete = customerService.getCustomerById(saved.getCustomerId());
        Assertions.assertNull(afterDelete, "Khách hàng phải được xóa thành công");
    }

    @Test
    public void testAddCustomerInvalidPhoneThrowsException() {
        // Điện thoại không đủ 10 số
        Customer c1 = new Customer("Test Phone", "091234567", "email@test.com", "Dia chi");
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.addCustomer(c1));

        // Điện thoại 11 số
        Customer c2 = new Customer("Test Phone", "09123456789", "email@test.com", "Dia chi");
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.addCustomer(c2));

        // Điện thoại không bắt đầu bằng số 0
        Customer c3 = new Customer("Test Phone", "1912345678", "email@test.com", "Dia chi");
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.addCustomer(c3));

        // Điện thoại chứa chữ
        Customer c4 = new Customer("Test Phone", "091234567a", "email@test.com", "Dia chi");
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.addCustomer(c4));
    }

    @Test
    public void testAddCustomerDuplicatePhoneThrowsException() {
        // Thử thêm với số điện thoại đã có của khách số 2 (0912345678)
        Customer c = new Customer("Trùng SĐT", "0912345678", "trung@test.com", "Dia chi");
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.addCustomer(c));
        Assertions.assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    public void testAddCustomerInvalidEmailThrowsException() {
        Customer c = new Customer("Test Email", "0911223344", "dinh-dang-sai-email", "Dia chi");
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.addCustomer(c));
    }

    @Test
    public void testDeleteCustomerWithPurchasesShouldFail() {
        // Khách hàng Phạm Minh Tuấn (0912345678) đã có hóa đơn trong init_db.sql
        Customer cust = customerService.getCustomerByPhone("0912345678");
        if (cust != null) {
            int custId = cust.getCustomerId();
            Assertions.assertTrue(customerService.hasCustomerPurchased(custId), "Khách hàng phải có hóa đơn");
            IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(custId));
            Assertions.assertTrue(ex.getMessage().contains("Không thể xóa!"), "Phải chặn xóa khách hàng đã có hóa đơn");
        }
    }

    @Test
    public void testDeleteInvalidCustomerIdShouldFail() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.deleteCustomer(0));
        Assertions.assertTrue(ex.getMessage().contains("không hợp lệ"));
    }

    @Test
    public void testGetCustomerPurchaseHistoryAndDetails() {
        // Khách hàng Phạm Minh Tuấn (0912345678)
        Customer cust = customerService.getCustomerByPhone("0912345678");
        Assertions.assertNotNull(cust, "Phải tìm thấy khách hàng 0912345678");
        int custId = cust.getCustomerId();

        List<Invoice> invoices = customerService.getCustomerInvoices(custId);
        Assertions.assertFalse(invoices.isEmpty(), "Khách hàng phải có ít nhất 1 hóa đơn");

        Invoice firstInvoice = invoices.get(0);
        Assertions.assertNotNull(firstInvoice.getInvoiceCode());
        Assertions.assertTrue(firstInvoice.getFinalAmount().compareTo(BigDecimal.ZERO) > 0);

        List<InvoiceDetail> details = customerService.getInvoiceDetails(firstInvoice.getInvoiceId());
        Assertions.assertFalse(details.isEmpty(), "Hóa đơn phải có chi tiết mặt hàng");
        Assertions.assertNotNull(details.get(0).getProductName());

        BigDecimal totalSpent = customerService.getTotalSpent(custId);
        Assertions.assertTrue(totalSpent.compareTo(BigDecimal.ZERO) > 0, "Tổng chi tiêu phải > 0");
    }
}
