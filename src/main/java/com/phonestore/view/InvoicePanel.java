package com.phonestore.view;

import com.phonestore.model.Invoice;
import com.phonestore.model.InvoiceDetail;
import com.phonestore.service.InvoiceService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.DateUtils;
import com.phonestore.utils.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý Lịch sử Hóa đơn và Chi tiết Hóa đơn (InvoicePanel).
 * Phụ trách bởi Thành viên 2 (Trung - Core Transaction Developer).
 * Cấu trúc: Bảng trên là Danh sách Hóa đơn; Bảng dưới là Chi tiết các máy trong Hóa đơn được chọn.
 */
public class InvoicePanel extends JPanel {

    private final InvoiceService invoiceService;
    private static final DateTimeFormatter DATE_INPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Filter controls
    private JTextField txtSearch;
    private JComboBox<String> cbQuickRange;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private JComboBox<String> cbStatus;
    private JButton btnSearch;
    private JButton btnReset;

    // Action buttons
    private JButton btnPreview;

    // Invoices Table
    private DefaultTableModel invoiceTableModel;
    private JTable invoiceTable;
    private List<Invoice> currentInvoiceList;

    // Details Table
    private DefaultTableModel detailTableModel;
    private JTable detailTable;
    private JLabel lblDetailTitle;

    // Status Summary
    private JLabel lblSummary;

    public InvoicePanel() {
        this.invoiceService = new InvoiceService();
        initComponents();
        loadInvoices();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. TOP PANEL: Title & Filters
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));

        JPanel headerRow = new JPanel(new BorderLayout());
        JLabel lblHeader = new JLabel("QUẢN LÝ LỊCH SỬ HÓA ĐƠN BÁN HÀNG (INVOICES MANAGEMENT)");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(new Color(30, 41, 59));

        JLabel lblSubHeader = new JLabel("Tra cứu toàn bộ hóa đơn đã bán, lọc theo khoảng ngày, xem chi tiết máy và in / xuất hóa đơn");
        lblSubHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubHeader.setForeground(new Color(100, 116, 139));

        headerRow.add(lblHeader, BorderLayout.NORTH);
        headerRow.add(lblSubHeader, BorderLayout.SOUTH);
        topContainer.add(headerRow, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.putClientProperty("JTextField.placeholderText", "Mã HĐ, Khách hàng, SĐT...");

        cbQuickRange = new JComboBox<>(new String[]{"Tất cả thời gian", "Hôm nay", "7 ngày qua", "30 ngày qua", "Tùy chọn ngày"});
        cbQuickRange.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtFromDate = new JTextField(8);
        txtFromDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFromDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");

        txtToDate = new JTextField(8);
        txtToDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtToDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        txtToDate.setText(LocalDate.now().format(DATE_INPUT_FORMAT));

        cbStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "COMPLETED", "CANCELLED"});
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(new Color(15, 118, 110));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);

        btnReset = new JButton("Làm mới");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setFocusPainted(false);

        btnPreview = new JButton("Xem Trước & In Hóa Đơn");
        btnPreview.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPreview.setBackground(new Color(37, 99, 235));
        btnPreview.setForeground(Color.WHITE);
        btnPreview.setFocusPainted(false);
        btnPreview.setEnabled(false);

        filterBar.add(new JLabel("Tìm kiếm:"));
        filterBar.add(txtSearch);
        filterBar.add(new JLabel("Mốc:"));
        filterBar.add(cbQuickRange);
        filterBar.add(new JLabel("Từ ngày:"));
        filterBar.add(txtFromDate);
        filterBar.add(new JLabel("Đến ngày:"));
        filterBar.add(txtToDate);
        filterBar.add(new JLabel("Trạng thái:"));
        filterBar.add(cbStatus);
        filterBar.add(btnSearch);
        filterBar.add(btnReset);
        filterBar.add(btnPreview);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 2. CENTER PANEL: SplitPane (Bảng Hóa đơn phía trên, Bảng Chi tiết phía dưới)
        // 2.1 Bảng trên: Danh sách hóa đơn
        JPanel invoiceTablePanel = new JPanel(new BorderLayout(0, 5));
        invoiceTablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Danh Sách Hóa Đơn Bán Hàng",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        String[] invCols = {
            "ID", "Mã Hóa Đơn", "Ngày Lập", "Khách Hàng", "SĐT Khách", "Nhân Viên Lập",
            "Tổng Tiền Hàng", "Giảm Giá", "Thực Thu (VNĐ)", "Trạng Thái", "Ghi Chú"
        };
        invoiceTableModel = new DefaultTableModel(invCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        invoiceTable = new JTable(invoiceTableModel);
        invoiceTable.setRowHeight(28);
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        invoiceTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        invoiceTable.getColumnModel().getColumn(1).setCellRenderer(centerRender);
        invoiceTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        invoiceTable.getColumnModel().getColumn(4).setCellRenderer(centerRender);
        invoiceTable.getColumnModel().getColumn(9).setCellRenderer(centerRender);

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(SwingConstants.RIGHT);
        invoiceTable.getColumnModel().getColumn(6).setCellRenderer(rightRender);
        invoiceTable.getColumnModel().getColumn(7).setCellRenderer(rightRender);
        invoiceTable.getColumnModel().getColumn(8).setCellRenderer(rightRender);

        invoiceTablePanel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        // 2.2 Bảng dưới: Chi tiết mặt hàng
        JPanel detailTablePanel = new JPanel(new BorderLayout(0, 5));
        detailTablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Chi Tiết Các Mặt Hàng Trong Hóa Đơn Được Chọn",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        lblDetailTitle = new JLabel("Nhấp chọn 1 hóa đơn ở bảng trên để xem chi tiết danh sách sản phẩm hoặc nhấp đúp để mở phiếu in.");
        lblDetailTitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDetailTitle.setForeground(new Color(71, 85, 105));
        lblDetailTitle.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        detailTablePanel.add(lblDetailTitle, BorderLayout.NORTH);

        String[] detailCols = {"STT", "Mã Sản Phẩm", "Tên Điện Thoại / Sản Phẩm", "Số Lượng", "Đơn Giá (VNĐ)", "Thành Tiền (VNĐ)"};
        detailTableModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        detailTable = new JTable(detailTableModel);
        detailTable.setRowHeight(26);
        detailTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        detailTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        detailTable.getColumnModel().getColumn(1).setCellRenderer(centerRender);
        detailTable.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        detailTable.getColumnModel().getColumn(4).setCellRenderer(rightRender);
        detailTable.getColumnModel().getColumn(5).setCellRenderer(rightRender);

        detailTablePanel.add(new JScrollPane(detailTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, invoiceTablePanel, detailTablePanel);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerSize(6);
        add(splitPane, BorderLayout.CENTER);

        // 3. BOTTOM PANEL: Summary Metrics
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));

        lblSummary = new JLabel("Tổng số hóa đơn: 0 | Tổng doanh thu: 0 đ");
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSummary.setForeground(new Color(15, 23, 42));
        statusPanel.add(lblSummary, BorderLayout.WEST);

        add(statusPanel, BorderLayout.SOUTH);

        // Events
        btnSearch.addActionListener(e -> loadInvoices());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbQuickRange.setSelectedIndex(0);
            txtFromDate.setText("");
            txtToDate.setText(LocalDate.now().format(DATE_INPUT_FORMAT));
            cbStatus.setSelectedIndex(0);
            loadInvoices();
        });

        cbQuickRange.addActionListener(e -> {
            int idx = cbQuickRange.getSelectedIndex();
            if (idx == 0) { // Tat ca
                txtFromDate.setText("");
                txtToDate.setText(LocalDate.now().format(DATE_INPUT_FORMAT));
            } else if (idx == 1) { // Hom nay
                String today = LocalDate.now().format(DATE_INPUT_FORMAT);
                txtFromDate.setText(today);
                txtToDate.setText(today);
            } else if (idx == 2) { // 7 ngay qua
                txtFromDate.setText(LocalDate.now().minusDays(7).format(DATE_INPUT_FORMAT));
                txtToDate.setText(LocalDate.now().format(DATE_INPUT_FORMAT));
            } else if (idx == 3) { // 30 ngay qua
                txtFromDate.setText(LocalDate.now().minusDays(30).format(DATE_INPUT_FORMAT));
                txtToDate.setText(LocalDate.now().format(DATE_INPUT_FORMAT));
            }
            loadInvoices();
        });

        cbStatus.addActionListener(e -> loadInvoices());

        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadInvoices();
                }
            }
        };
        txtSearch.addKeyListener(enterSubmit);
        txtFromDate.addKeyListener(enterSubmit);
        txtToDate.addKeyListener(enterSubmit);

        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = invoiceTable.getSelectedRow();
                boolean selected = (row >= 0 && currentInvoiceList != null && row < currentInvoiceList.size());
                btnPreview.setEnabled(selected);
                if (selected) {
                    Invoice inv = currentInvoiceList.get(row);
                    loadInvoiceDetails(inv);
                } else {
                    detailTableModel.setRowCount(0);
                    lblDetailTitle.setText("Nhấp chọn 1 hóa đơn ở bảng trên để xem chi tiết danh sách sản phẩm.");
                }
            }
        });

        invoiceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && invoiceTable.getSelectedRow() >= 0) {
                    openPreviewDialog();
                }
            }
        });

        btnPreview.addActionListener(e -> openPreviewDialog());
    }

    public void loadInvoices() {
        invoiceTableModel.setRowCount(0);
        detailTableModel.setRowCount(0);
        btnPreview.setEnabled(false);

        String keyword = txtSearch.getText().trim();
        LocalDate fromDate = null;
        LocalDate toDate = null;

        String fromStr = txtFromDate.getText().trim();
        if (!fromStr.isEmpty()) {
            try {
                fromDate = LocalDate.parse(fromStr, DATE_INPUT_FORMAT);
            } catch (Exception ignored) {
            }
        }

        String toStr = txtToDate.getText().trim();
        if (!toStr.isEmpty()) {
            try {
                toDate = LocalDate.parse(toStr, DATE_INPUT_FORMAT);
            } catch (Exception ignored) {
            }
        }

        String status = (String) cbStatus.getSelectedItem();
        if ("Tất cả trạng thái".equals(status)) {
            status = null;
        }

        currentInvoiceList = invoiceService.searchInvoices(keyword, null, fromDate, toDate, status);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Invoice inv : currentInvoiceList) {
            if ("COMPLETED".equalsIgnoreCase(inv.getStatus())) {
                totalRevenue = totalRevenue.add(inv.getFinalAmount());
            }

            invoiceTableModel.addRow(new Object[]{
                inv.getInvoiceId(),
                inv.getInvoiceCode(),
                DateUtils.formatDateTime(inv.getInvoiceDate()),
                inv.getCustomerName(),
                inv.getCustomerPhone() != null ? inv.getCustomerPhone() : "—",
                inv.getEmployeeName(),
                CurrencyUtils.format(inv.getTotalAmount()),
                CurrencyUtils.format(inv.getDiscountAmount()),
                CurrencyUtils.format(inv.getFinalAmount()),
                "COMPLETED".equalsIgnoreCase(inv.getStatus()) ? "Hoàn thành" : "Đã hủy",
                inv.getNote() != null ? inv.getNote() : ""
            });
        }

        lblSummary.setText("Tổng số hóa đơn: " + currentInvoiceList.size()
                + " | Tổng doanh thu thực thu: " + CurrencyUtils.format(totalRevenue));

        if (!currentInvoiceList.isEmpty()) {
            invoiceTable.setRowSelectionInterval(0, 0);
        }
    }

    private void loadInvoiceDetails(Invoice invoice) {
        detailTableModel.setRowCount(0);
        lblDetailTitle.setText("Hóa đơn: " + invoice.getInvoiceCode() + " | Khách hàng: "
                + invoice.getCustomerName() + " | Ngày lập: " + DateUtils.formatDateTime(invoice.getInvoiceDate()));

        List<InvoiceDetail> details = invoiceService.getInvoiceDetails(invoice.getInvoiceId());
        int stt = 1;
        for (InvoiceDetail d : details) {
            detailTableModel.addRow(new Object[]{
                stt++,
                d.getProductCode(),
                d.getProductName(),
                d.getQuantity(),
                CurrencyUtils.format(d.getUnitPrice()),
                CurrencyUtils.format(d.getSubtotal())
            });
        }
    }

    private void openPreviewDialog() {
        int row = invoiceTable.getSelectedRow();
        if (row >= 0 && currentInvoiceList != null && row < currentInvoiceList.size()) {
            Invoice inv = currentInvoiceList.get(row);
            Window parent = SwingUtilities.getWindowAncestor(this);
            InvoicePreviewDialog dialog = new InvoicePreviewDialog(parent, inv.getInvoiceId());
            dialog.setVisible(true);
        } else {
            MessageUtils.showWarning(this, "Vui lòng chọn một hóa đơn để xem trước và in!");
        }
    }
}
