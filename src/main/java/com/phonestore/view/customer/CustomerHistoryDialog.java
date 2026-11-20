package com.phonestore.view.customer;

import com.phonestore.model.Customer;
import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import com.phonestore.service.CustomerService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.DateUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Hộp thoại hiển thị lịch sử giao dịch / hóa đơn mà khách hàng đã mua,
 * kèm theo chi tiết các sản phẩm của từng hóa đơn được chọn.
 */
public class CustomerHistoryDialog extends JDialog {

    private final Customer customer;
    private final CustomerService customerService;

    private DefaultTableModel invoiceTableModel;
    private DefaultTableModel detailTableModel;
    private JTable invoiceTable;
    private JTable detailTable;
    private JLabel lblSelectedInvoice;
    private List<Invoice> invoiceList;

    public CustomerHistoryDialog(Window parent, Customer customer) {
        super(parent, "Lịch Sử Mua Hàng - " + customer.getFullName(), ModalityType.APPLICATION_MODAL);
        this.customer = customer;
        this.customerService = new CustomerService();

        initComponents();
        loadInvoiceHistory();
    }

    private void initComponents() {
        setSize(950, 680);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // 1. Top Panel: Thông tin khách hàng & Tổng chi tiêu
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setBackground(new Color(30, 41, 59));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        infoPanel.setOpaque(false);

        JLabel lblCustName = new JLabel("LỊCH SỬ GIAO DỊCH: " + customer.getFullName().toUpperCase());
        lblCustName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblCustName.setForeground(Color.WHITE);

        String contactInfo = "SĐT: " + customer.getPhone()
                + " | Email: " + (customer.getEmail() != null && !customer.getEmail().isEmpty() ? customer.getEmail() : "Chưa cập nhật")
                + " | Địa chỉ: " + (customer.getAddress() != null && !customer.getAddress().isEmpty() ? customer.getAddress() : "Chưa cập nhật");
        JLabel lblCustMeta = new JLabel(contactInfo);
        lblCustMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCustMeta.setForeground(new Color(148, 163, 184));

        infoPanel.add(lblCustName);
        infoPanel.add(lblCustMeta);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Stats Box
        BigDecimal totalSpent = customerService.getTotalSpent(customer.getCustomerId());
        JPanel statsBox = new JPanel(new GridLayout(2, 1, 0, 2));
        statsBox.setOpaque(false);

        JLabel lblSpentTitle = new JLabel("Tổng chi tiêu:", SwingConstants.RIGHT);
        lblSpentTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSpentTitle.setForeground(new Color(203, 213, 225));

        JLabel lblSpentValue = new JLabel(CurrencyUtils.format(totalSpent), SwingConstants.RIGHT);
        lblSpentValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSpentValue.setForeground(new Color(52, 211, 153)); // Xanh lá ngọc đẹp mắt

        statsBox.add(lblSpentTitle);
        statsBox.add(lblSpentValue);
        topPanel.add(statsBox, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 2. Center Panel: SplitPane (Danh sách hóa đơn bên trên, Chi tiết sản phẩm bên dưới)
        JPanel invoicePanel = new JPanel(new BorderLayout(5, 5));
        invoicePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Danh Sách Hóa Đơn Đã Mua",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        String[] invCols = {"Mã Hóa Đơn", "Ngày Lập", "Nhân Viên Lập", "Tổng Tiền Hàng", "Giảm Giá", "Thành Tiền", "Trạng Thái", "Ghi Chú"};
        invoiceTableModel = new DefaultTableModel(invCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        invoiceTable = new JTable(invoiceTableModel);
        invoiceTable.setRowHeight(28);
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        invoiceTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        invoiceTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        invoiceTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        invoicePanel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        // Detail Panel
        JPanel detailPanel = new JPanel(new BorderLayout(5, 5));
        lblSelectedInvoice = new JLabel("Chi Tiết Mặt Hàng Của Hóa Đơn (Chọn 1 hóa đơn ở trên để xem chi tiết)");
        lblSelectedInvoice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelectedInvoice.setForeground(new Color(30, 41, 59));
        lblSelectedInvoice.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        detailPanel.add(lblSelectedInvoice, BorderLayout.NORTH);

        String[] detailCols = {"Mã SP", "Tên Điện Thoại / Sản Phẩm", "Số Lượng", "Đơn Giá", "Thành Tiền"};
        detailTableModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        detailTable = new JTable(detailTableModel);
        detailTable.setRowHeight(26);
        detailTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        detailTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        detailTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        detailTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        detailPanel.add(new JScrollPane(detailTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, invoicePanel, detailPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(splitPane, BorderLayout.CENTER);

        // 3. Footer Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.setPreferredSize(new Dimension(100, 34));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);
        add(footerPanel, BorderLayout.SOUTH);

        // Table selection event
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = invoiceTable.getSelectedRow();
                if (selectedRow >= 0 && invoiceList != null && selectedRow < invoiceList.size()) {
                    Invoice inv = invoiceList.get(selectedRow);
                    loadInvoiceDetails(inv);
                }
            }
        });

        // Esc to close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void loadInvoiceHistory() {
        invoiceTableModel.setRowCount(0);
        detailTableModel.setRowCount(0);
        invoiceList = customerService.getCustomerInvoices(customer.getCustomerId());

        if (invoiceList.isEmpty()) {
            lblSelectedInvoice.setText("Khách hàng này chưa có đơn hàng nào.");
            return;
        }

        for (Invoice inv : invoiceList) {
            invoiceTableModel.addRow(new Object[]{
                inv.getInvoiceCode(),
                DateUtils.formatDateTime(inv.getInvoiceDate()),
                inv.getEmployeeName(),
                CurrencyUtils.format(inv.getTotalAmount()),
                CurrencyUtils.format(inv.getDiscountAmount()),
                CurrencyUtils.format(inv.getFinalAmount()),
                "COMPLETED".equalsIgnoreCase(inv.getStatus()) ? "Hoàn thành" : "Đã hủy",
                inv.getNote() != null ? inv.getNote() : ""
            });
        }

        // Tự động chọn dòng đầu tiên
        if (invoiceTable.getRowCount() > 0) {
            invoiceTable.setRowSelectionInterval(0, 0);
        }
    }

    private void loadInvoiceDetails(Invoice invoice) {
        detailTableModel.setRowCount(0);
        lblSelectedInvoice.setText("Chi tiết hóa đơn [" + invoice.getInvoiceCode() + "] - Lập ngày: "
                + DateUtils.formatDateTime(invoice.getInvoiceDate()));

        List<InvoiceDetail> details = customerService.getInvoiceDetails(invoice.getInvoiceId());
        for (InvoiceDetail d : details) {
            detailTableModel.addRow(new Object[]{
                d.getProductCode(),
                d.getProductName(),
                d.getQuantity(),
                CurrencyUtils.format(d.getUnitPrice()),
                CurrencyUtils.format(d.getSubtotal())
            });
        }
    }
}
