package com.phonestore.service;

import com.phonestore.config.DatabaseConnection;
import com.phonestore.dao.ProductDAO;
import com.phonestore.model.Customer;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import com.phonestore.model.Product;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Bộ kiểm thử toàn diện các tiêu chí nghiệm thu cho phân hệ của Trung (TV2):
 * - Bán hàng POS & Transaction an toàn
 * - Trừ kho chính xác
 * - Kiểm tra lỗi tồn kho
 * - Rollback khi xảy ra lỗi ở dòng chi tiết thứ 2
 * - Sinh mã hóa đơn tự động HDyyyyMMdd-XXX
 */
public class InvoiceServiceTest {

    private InvoiceService invoiceService;
    private ProductDAO productDAO;
    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        invoiceService = new InvoiceService();
        productDAO = new ProductDAO();
        customerService = new CustomerService();
    }

    @Test
    public void testGenerateInvoiceCodeFormat() {
        String code = invoiceService.generateNextInvoiceCode();
        Assertions.assertNotNull(code);
        Assertions.assertTrue(code.startsWith("HD20"), "Mã hóa đơn phải bắt đầu bằng HDyyyy...: " + code);
        Assertions.assertTrue(code.contains("-"), "Mã hóa đơn phải chứa dấu gạch ngang phân tách số thứ tự: " + code);
    }

    @Test
    public void testCheckoutSuccessAndStockDeduction() throws SQLException {
        // 1. Lấy sản phẩm SP001 và SP002 từ database
        List<Product> products = productDAO.findAllActive();
        Assertions.assertTrue(products.size() >= 2, "Cần ít nhất 2 sản phẩm để test");

        Product p1 = products.get(0);
        Product p2 = products.get(1);

        int initialStock1 = p1.getStockQuantity();
        int initialStock2 = p2.getStockQuantity();
        Assertions.assertTrue(initialStock1 >= 2, "Sản phẩm 1 cần tồn kho >= 2");
        Assertions.assertTrue(initialStock2 >= 2, "Sản phẩm 2 cần tồn kho >= 2");

        // 2. Chuẩn bị hóa đơn với 2 sản phẩm và giảm giá 100.000 VNĐ
        Invoice invoice = new Invoice();
        invoice.setCustomerId(2); // Phạm Minh Tuấn
        invoice.setEmployeeId(1); // NV001
        invoice.setNote("Hóa đơn test tự động TV2");
        invoice.setDiscountAmount(new BigDecimal("100000"));

        int qty1 = 1;
        int qty2 = 2;

        List<InvoiceDetail> details = new ArrayList<>();
        InvoiceDetail d1 = new InvoiceDetail();
        d1.setProductId(p1.getProductId());
        d1.setProductCode(p1.getProductCode());
        d1.setProductName(p1.getProductName());
        d1.setQuantity(qty1);
        d1.setUnitPrice(p1.getSellingPrice());
        details.add(d1);

        InvoiceDetail d2 = new InvoiceDetail();
        d2.setProductId(p2.getProductId());
        d2.setProductCode(p2.getProductCode());
        d2.setProductName(p2.getProductName());
        d2.setQuantity(qty2);
        d2.setUnitPrice(p2.getSellingPrice());
        details.add(d2);

        // Tiêu chí 1: Tính toán tiền đúng tuyệt đối
        BigDecimal expectedTotal = p1.getSellingPrice().multiply(BigDecimal.valueOf(qty1))
                .add(p2.getSellingPrice().multiply(BigDecimal.valueOf(qty2)));
        BigDecimal expectedFinal = expectedTotal.subtract(new BigDecimal("100000"));

        // 3. Thực hiện giao dịch tạo hóa đơn (JDBC Transaction)
        Invoice created = invoiceService.createInvoice(invoice, details);

        Assertions.assertTrue(created.getInvoiceId() > 0, "Invoice ID phải được tạo > 0");
        Assertions.assertEquals(expectedTotal, created.getTotalAmount(), "Tổng tiền hàng tính đúng");
        Assertions.assertEquals(expectedFinal, created.getFinalAmount(), "Thành tiền tính đúng sau giảm giá");

        // Tiêu chí 3: Tồn kho giảm chính xác bằng số lượng đã bán
        int updatedStock1 = getProductStock(p1.getProductId());
        int updatedStock2 = getProductStock(p2.getProductId());

        Assertions.assertEquals(initialStock1 - qty1, updatedStock1, "Tồn kho sản phẩm 1 phải giảm đúng bằng 1");
        Assertions.assertEquals(initialStock2 - qty2, updatedStock2, "Tồn kho sản phẩm 2 phải giảm đúng bằng 2");

        // Kiểm tra chi tiết hóa đơn trong DB
        List<InvoiceDetail> savedDetails = invoiceService.getInvoiceDetails(created.getInvoiceId());
        Assertions.assertEquals(2, savedDetails.size(), "Phải có đúng 2 dòng chi tiết sản phẩm");
    }

    @Test
    public void testCheckoutExceedingStockThrowsException() {
        // Cố tình mua số lượng lớn hơn tồn kho
        List<Product> products = productDAO.findAllActive();
        Assertions.assertFalse(products.isEmpty());
        Product p = products.get(0);

        Invoice invoice = new Invoice();
        invoice.setCustomerId(2);
        invoice.setEmployeeId(1);

        List<InvoiceDetail> details = new ArrayList<>();
        InvoiceDetail d = new InvoiceDetail();
        d.setProductId(p.getProductId());
        d.setProductCode(p.getProductCode());
        d.setProductName(p.getProductName());
        d.setQuantity(p.getStockQuantity() + 9999); // Vượt tồn kho
        d.setUnitPrice(p.getSellingPrice());
        details.add(d);

        Assertions.assertThrows(Exception.class, () -> {
            invoiceService.createInvoice(invoice, details);
        }, "Phải chặn lại khi số lượng mua vượt tồn kho");
    }

    @Test
    public void testTransactionRollbackWhenSecondItemFails() throws SQLException {
        // Tiêu chí 4: Nếu xảy ra lỗi ở dòng chi tiết thứ 2, toàn bộ hóa đơn và tồn kho dòng 1 phải được rollback
        List<Product> products = productDAO.findAllActive();
        Assertions.assertTrue(products.size() >= 2);
        Product p1 = products.get(0);
        Product p2 = products.get(1);

        int initialStock1 = getProductStock(p1.getProductId());
        int initialStock2 = getProductStock(p2.getProductId());

        Invoice invoice = new Invoice();
        invoice.setCustomerId(2);
        invoice.setEmployeeId(1);
        invoice.setNote("Test Rollback");

        List<InvoiceDetail> details = new ArrayList<>();
        // Dòng 1: Hợp lệ
        InvoiceDetail d1 = new InvoiceDetail();
        d1.setProductId(p1.getProductId());
        d1.setProductCode(p1.getProductCode());
        d1.setProductName(p1.getProductName());
        d1.setQuantity(1);
        d1.setUnitPrice(p1.getSellingPrice());
        details.add(d1);

        // Dòng 2: Cố tình mua vượt số lượng tồn kho để gây ra lỗi ở dòng thứ 2
        InvoiceDetail d2 = new InvoiceDetail();
        d2.setProductId(p2.getProductId());
        d2.setProductCode(p2.getProductCode());
        d2.setProductName(p2.getProductName());
        d2.setQuantity(initialStock2 + 5000); // Lỗi hết hàng
        d2.setUnitPrice(p2.getSellingPrice());
        details.add(d2);

        // Bắt ngoại lệ
        Assertions.assertThrows(Exception.class, () -> {
            invoiceService.createInvoice(invoice, details);
        });

        // Kiểm tra ROLLBACK: Tồn kho của sản phẩm 1 KHÔNG ĐƯỢC THAY ĐỔI
        int currentStock1 = getProductStock(p1.getProductId());
        Assertions.assertEquals(initialStock1, currentStock1,
                "Tồn kho sản phẩm 1 phải được khôi phục nguyên vẹn sau khi rollback");

        int currentStock2 = getProductStock(p2.getProductId());
        Assertions.assertEquals(initialStock2, currentStock2,
                "Tồn kho sản phẩm 2 phải không thay đổi sau khi rollback");
    }

    @Test
    public void testSearchInvoices() {
        List<Invoice> list = invoiceService.searchInvoices(null, null, null, null, null);
        Assertions.assertFalse(list.isEmpty(), "Danh sách hóa đơn không được rỗng");

        Invoice first = list.get(0);
        List<Invoice> byCode = invoiceService.searchInvoices(first.getInvoiceCode(), null, null, null, null);
        Assertions.assertFalse(byCode.isEmpty());
        Assertions.assertEquals(first.getInvoiceId(), byCode.get(0).getInvoiceId());
    }

    private int getProductStock(int productId) throws SQLException {
        String sql = "SELECT stock_quantity FROM products WHERE product_id = " + productId;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
}
