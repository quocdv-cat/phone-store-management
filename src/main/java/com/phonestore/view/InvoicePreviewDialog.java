package com.phonestore.view;

import com.phonestore.model.Customer;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import com.phonestore.service.CustomerService;
import com.phonestore.service.InvoiceService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.DateUtils;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Hộp thoại hiển thị mẫu phiếu hóa đơn bán hàng chuyên nghiệp và xuất file (.html / .txt).
 * Phụ trách bởi Thành viên 2 (Trung - Core Transaction Developer).
 */
public class InvoicePreviewDialog extends JDialog {

    private final int invoiceId;
    private final InvoiceService invoiceService;
    private final CustomerService customerService;

    private Invoice invoice;
    private List<InvoiceDetail> details;
    private Customer customer;
    private JEditorPane previewPane;

    public InvoicePreviewDialog(Window parent, int invoiceId) {
        super(parent, "Xem Trước & Xuất Hóa Đơn Bán Hàng", ModalityType.APPLICATION_MODAL);
        this.invoiceId = invoiceId;
        this.invoiceService = new InvoiceService();
        this.customerService = new CustomerService();

        loadData();
        initComponents();
    }

    private void loadData() {
        this.invoice = invoiceService.getInvoiceById(invoiceId);
        if (invoice != null) {
            this.details = invoiceService.getInvoiceDetails(invoiceId);
            this.customer = customerService.getCustomerById(invoice.getCustomerId());
        }
    }

    private void initComponents() {
        setSize(780, 750);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header Top
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("XEM TRƯỚC HÓA ĐƠN BÁN HÀNG: " + (invoice != null ? invoice.getInvoiceCode() : ""));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Mẫu hóa đơn chuẩn định dạng, hỗ trợ xem trước, in ấn và xuất file ra đĩa");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(148, 163, 184));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Preview Pane (HTML)
        previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.setText(buildInvoiceHtml());
        previewPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(previewPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnPrint = new JButton("In Hóa Đơn");
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrint.setBackground(new Color(37, 99, 235));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);

        JButton btnExportHtml = new JButton("Xuất File .HTML");
        btnExportHtml.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportHtml.setBackground(new Color(16, 185, 129));
        btnExportHtml.setForeground(Color.WHITE);
        btnExportHtml.setFocusPainted(false);

        JButton btnExportTxt = new JButton("Xuất File .TXT");
        btnExportTxt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExportTxt.setFocusPainted(false);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.setFocusPainted(false);

        footerPanel.add(btnPrint);
        footerPanel.add(btnExportHtml);
        footerPanel.add(btnExportTxt);
        footerPanel.add(btnClose);
        add(footerPanel, BorderLayout.SOUTH);

        // Events
        btnPrint.addActionListener(e -> printInvoice());
        btnExportHtml.addActionListener(e -> exportHtml());
        btnExportTxt.addActionListener(e -> exportTxt());
        btnClose.addActionListener(e -> dispose());

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private String buildInvoiceHtml() {
        if (invoice == null) {
            return "<html><body><h2 style='color:red;'>Không tìm thấy thông tin hóa đơn!</h2></body></html>";
        }

        String custName = customer != null ? customer.getFullName() : (invoice.getCustomerName() != null ? invoice.getCustomerName() : "");
        String custPhone = customer != null ? customer.getPhone() : "";
        String custAddress = (customer != null && customer.getAddress() != null) ? customer.getAddress() : "";
        String empName = invoice.getEmployeeName() != null ? invoice.getEmployeeName() : "Nhân viên bán hàng";
        String dateStr = DateUtils.formatDateTime(invoice.getInvoiceDate());

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; margin: 20px; color: #1e293b; background: #ffffff; }");
        sb.append(".header { text-align: center; border-bottom: 2px dashed #94a3b8; padding-bottom: 12px; }");
        sb.append(".store-name { font-size: 20px; font-weight: bold; color: #0f172a; }");
        sb.append(".store-info { font-size: 12px; color: #64748b; margin-top: 4px; }");
        sb.append(".title { font-size: 18px; font-weight: bold; margin: 15px 0 10px 0; color: #1e40af; text-align: center; }");
        sb.append(".meta-table { width: 100%; font-size: 13px; margin-bottom: 15px; }");
        sb.append(".meta-table td { padding: 3px 0; }");
        sb.append(".items-table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px; }");
        sb.append(".items-table th { background-color: #f1f5f9; border-top: 1px solid #cbd5e1; border-bottom: 1px solid #cbd5e1; padding: 8px 6px; text-align: left; }");
        sb.append(".items-table td { border-bottom: 1px solid #e2e8f0; padding: 8px 6px; }");
        sb.append(".text-right { text-align: right; }");
        sb.append(".text-center { text-align: center; }");
        sb.append(".summary-table { width: 100%; margin-top: 15px; font-size: 13px; }");
        sb.append(".summary-table td { padding: 4px 6px; }");
        sb.append(".total-row { font-size: 16px; font-weight: bold; color: #dc2626; border-top: 2px dashed #94a3b8; padding-top: 8px; }");
        sb.append(".footer { text-align: center; margin-top: 25px; border-top: 1px dashed #cbd5e1; padding-top: 12px; font-size: 12px; color: #64748b; }");
        sb.append("</style></head><body>");

        // Header
        sb.append("<div class='header'>");
        sb.append("<div class='store-name'>MOBILE STORE - CỬA HÀNG ĐIỆN THOẠI CHÍNH HÃNG</div>");
        sb.append("<div class='store-info'>Địa chỉ: 123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh | Hotline: 1900 6868</div>");
        sb.append("<div class='store-info'>Website: www.phonestore.vn - Email: support@phonestore.vn</div>");
        sb.append("</div>");

        sb.append("<div class='title'>HÓA ĐƠN BÁN HÀNG</div>");

        // Meta info
        sb.append("<table class='meta-table'>");
        sb.append("<tr>");
        sb.append("<td><b>Mã HĐ:</b> ").append(invoice.getInvoiceCode()).append("</td>");
        sb.append("<td class='text-right'><b>Ngày lập:</b> ").append(dateStr).append("</td>");
        sb.append("</tr><tr>");
        sb.append("<td><b>Khách hàng:</b> ").append(custName).append("</td>");
        sb.append("<td class='text-right'><b>Điện thoại:</b> ").append(custPhone).append("</td>");
        sb.append("</tr><tr>");
        sb.append("<td colspan='2'><b>Địa chỉ:</b> ").append(custAddress).append("</td>");
        sb.append("</tr><tr>");
        sb.append("<td colspan='2'><b>Nhân viên bán hàng:</b> ").append(empName).append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        // Items Table
        sb.append("<table class='items-table'>");
        sb.append("<thead><tr>");
        sb.append("<th class='text-center' style='width: 35px;'>STT</th>");
        sb.append("<th>Mã SP</th>");
        sb.append("<th>Tên sản phẩm</th>");
        sb.append("<th class='text-center' style='width: 50px;'>SL</th>");
        sb.append("<th class='text-right' style='width: 110px;'>Đơn giá</th>");
        sb.append("<th class='text-right' style='width: 120px;'>Thành tiền</th>");
        sb.append("</tr></thead><tbody>");

        if (details != null && !details.isEmpty()) {
            int stt = 1;
            for (InvoiceDetail d : details) {
                sb.append("<tr>");
                sb.append("<td class='text-center'>").append(stt++).append("</td>");
                sb.append("<td>").append(d.getProductCode() != null ? d.getProductCode() : "").append("</td>");
                sb.append("<td><b>").append(d.getProductName() != null ? d.getProductName() : "").append("</b></td>");
                sb.append("<td class='text-center'>").append(d.getQuantity()).append("</td>");
                sb.append("<td class='text-right'>").append(CurrencyUtils.format(d.getUnitPrice())).append("</td>");
                sb.append("<td class='text-right'><b>").append(CurrencyUtils.format(d.getSubtotal())).append("</b></td>");
                sb.append("</tr>");
            }
        } else {
            sb.append("<tr><td colspan='6' class='text-center'>Không có chi tiết sản phẩm</td></tr>");
        }
        sb.append("</tbody></table>");

        // Summary
        sb.append("<table class='summary-table'>");
        sb.append("<tr>");
        sb.append("<td style='width: 60%;'><b>Ghi chú:</b> ").append(invoice.getNote() != null && !invoice.getNote().isEmpty() ? invoice.getNote() : "Không").append("</td>");
        sb.append("<td class='text-right' style='width: 20%;'><b>Tổng tiền hàng:</b></td>");
        sb.append("<td class='text-right' style='width: 20%;'>").append(CurrencyUtils.format(invoice.getTotalAmount())).append("</td>");
        sb.append("</tr><tr>");
        sb.append("<td><b>Trạng thái:</b> ").append("COMPLETED".equalsIgnoreCase(invoice.getStatus()) ? "<span style='color:green;'>Đã thanh toán</span>" : invoice.getStatus()).append("</td>");
        sb.append("<td class='text-right'><b>Giảm giá:</b></td>");
        sb.append("<td class='text-right'>-").append(CurrencyUtils.format(invoice.getDiscountAmount())).append("</td>");
        sb.append("</tr><tr class='total-row'>");
        sb.append("<td colspan='2' class='text-right'><b>TỔNG TIỀN THANH TOÁN:</b></td>");
        sb.append("<td class='text-right'><b>").append(CurrencyUtils.format(invoice.getFinalAmount())).append("</b></td>");
        sb.append("</tr>");
        sb.append("</table>");

        // Footer
        sb.append("<div class='footer'>");
        sb.append("<p><i>Cảm ơn quý khách đã tin tưởng mua sắm tại Mobile Store!</i><br>");
        sb.append("Quý khách vui lòng giữ lại hóa đơn này để được hưởng chính sách bảo hành và đổi trả chính hãng.</p>");
        sb.append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String buildInvoiceTxt() {
        if (invoice == null) {
            return "Khong tim thay thong tin hoa don!";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("               MOBILE STORE - CUA HANG DIEN THOAI                \n");
        sb.append("          123 Nguyen Hue, Q.1, TP.HCM - Hotline: 1900 6868       \n");
        sb.append("=================================================================\n");
        sb.append("                       HOA DON BAN HANG                          \n");
        sb.append("Ma hoa don : ").append(invoice.getInvoiceCode()).append("\n");
        sb.append("Ngay lap   : ").append(DateUtils.formatDateTime(invoice.getInvoiceDate())).append("\n");
        sb.append("Nhan vien  : ").append(invoice.getEmployeeName()).append("\n");
        sb.append("Khach hang : ").append(customer != null ? customer.getFullName() : invoice.getCustomerName()).append("\n");
        sb.append("Dien thoai : ").append(customer != null ? customer.getPhone() : "").append("\n");
        sb.append("Dia chi    : ").append(customer != null && customer.getAddress() != null ? customer.getAddress() : "").append("\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append(String.format("%-4s %-12s %-24s %-4s %-12s\n", "STT", "Ma SP", "Ten San Pham", "SL", "Thanh Tien"));
        sb.append("-----------------------------------------------------------------\n");

        if (details != null) {
            int stt = 1;
            for (InvoiceDetail d : details) {
                String pName = d.getProductName() != null ? d.getProductName() : "";
                if (pName.length() > 22) {
                    pName = pName.substring(0, 21) + ".";
                }
                sb.append(String.format("%-4d %-12s %-24s %-4d %12s\n",
                        stt++,
                        d.getProductCode(),
                        pName,
                        d.getQuantity(),
                        CurrencyUtils.format(d.getSubtotal())));
            }
        }
        sb.append("-----------------------------------------------------------------\n");
        sb.append(String.format("Tong tien hang : %46s\n", CurrencyUtils.format(invoice.getTotalAmount())));
        sb.append(String.format("Giam gia       : -%45s\n", CurrencyUtils.format(invoice.getDiscountAmount())));
        sb.append(String.format("THANH TOAN     : %46s\n", CurrencyUtils.format(invoice.getFinalAmount())));
        sb.append("-----------------------------------------------------------------\n");
        sb.append("Ghi chu: ").append(invoice.getNote() != null ? invoice.getNote() : "").append("\n\n");
        sb.append("        Cam on quy khach! Hen gap lai quy khach!\n");
        sb.append("=================================================================\n");
        return sb.toString();
    }

    private void printInvoice() {
        try {
            boolean printed = previewPane.print();
            if (printed) {
                MessageUtils.showInfo(this, "Lệnh in hóa đơn đã được gửi thành công!");
            }
        } catch (Exception ex) {
            MessageUtils.showError(this, "Lỗi khi in hóa đơn: " + ex.getMessage());
        }
    }

    private void exportHtml() {
        if (invoice == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Hóa đơn HTML");
        chooser.setSelectedFile(new File(invoice.getInvoiceCode() + ".html"));
        chooser.setFileFilter(new FileNameExtensionFilter("HTML Files (*.html)", "html"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".html")) {
                target = new File(target.getAbsolutePath() + ".html");
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
                writer.write(buildInvoiceHtml());
                MessageUtils.showInfo(this, "Xuất file HTML thành công:\n" + target.getAbsolutePath());
            } catch (Exception ex) {
                MessageUtils.showError(this, "Lỗi khi xuất file: " + ex.getMessage());
            }
        }
    }

    private void exportTxt() {
        if (invoice == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Hóa đơn Text");
        chooser.setSelectedFile(new File(invoice.getInvoiceCode() + ".txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".txt")) {
                target = new File(target.getAbsolutePath() + ".txt");
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
                writer.write(buildInvoiceTxt());
                MessageUtils.showInfo(this, "Xuất file TXT thành công:\n" + target.getAbsolutePath());
            } catch (Exception ex) {
                MessageUtils.showError(this, "Lỗi khi xuất file: " + ex.getMessage());
            }
        }
    }
}
