package com.phonestore.view;

import com.phonestore.model.PurchaseDetail;
import com.phonestore.model.PurchaseOrder;
import com.phonestore.model.Supplier;
import com.phonestore.service.PurchaseService;
import com.phonestore.service.SupplierService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.DateUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện tra cứu Lịch sử phiếu nhập & Chi tiết từng phiếu nhập (PurchaseHistoryPanel).
 * Thuộc Module 7 - Phân hệ TV3.
 */
public class PurchaseHistoryPanel extends JPanel {

    private final PurchaseService purchaseService = new PurchaseService();
    private final SupplierService supplierService = new SupplierService();

    private JTextField txtSearchCode;
    private JComboBox<SupplierItem> cboSuppliers;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private JButton btnFilter;
    private JButton btnResetFilter;

    private JTable tblOrders;
    private DefaultTableModel orderTableModel;

    private JTable tblDetails;
    private DefaultTableModel detailTableModel;
    private JLabel lblDetailHeader;

    public PurchaseHistoryPanel() {
        initComponents();
        loadSuppliersFilter();
        loadOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. BỘ LỌC TÌM KIẾM
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Bộ lọc tìm kiếm lịch sử nhập kho", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        txtSearchCode = new JTextField(10);
        txtSearchCode.putClientProperty("JTextField.placeholderText", "Mã PN...");

        cboSuppliers = new JComboBox<>();
        cboSuppliers.setPreferredSize(new Dimension(200, 30));

        txtFromDate = new JTextField(9);
        txtFromDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");

        txtToDate = new JTextField(9);
        txtToDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");

        btnFilter = new JButton("Lọc dữ liệu");
        btnFilter.setBackground(new Color(59, 130, 246));
        btnFilter.setForeground(Color.WHITE);

        btnResetFilter = new JButton("Làm mới");

        btnFilter.addActionListener(e -> loadOrders());
        txtSearchCode.addActionListener(e -> loadOrders());
        btnResetFilter.addActionListener(e -> {
            txtSearchCode.setText("");
            cboSuppliers.setSelectedIndex(0);
            txtFromDate.setText("");
            txtToDate.setText("");
            loadOrders();
        });

        filterPanel.add(new JLabel("Mã phiếu:"));
        filterPanel.add(txtSearchCode);
        filterPanel.add(new JLabel("Nhà cung cấp:"));
        filterPanel.add(cboSuppliers);
        filterPanel.add(new JLabel("Từ ngày:"));
        filterPanel.add(txtFromDate);
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(txtToDate);
        filterPanel.add(btnFilter);
        filterPanel.add(btnResetFilter);

        add(filterPanel, BorderLayout.NORTH);

        // 2. PHẦN BẢNG DỮ LIỆU: MASTER - DETAILS BẰNG JSPLITPANE
        String[] orderCols = {"ID", "Mã Phiếu", "Nhà Cung Cấp", "Nhân Viên Nhập", "Ngày Nhập", "Tổng Tiền", "Ghi Chú", "Trạng Thái"};
        orderTableModel = new DefaultTableModel(orderCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblOrders = new JTable(orderTableModel);
        tblOrders.setRowHeight(28);
        tblOrders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblOrders.getSelectedRow();
                if (row >= 0) {
                    int purchaseId = (int) tblOrders.getValueAt(row, 0);
                    String code = tblOrders.getValueAt(row, 1).toString();
                    loadDetails(purchaseId, code);
                } else {
                    detailTableModel.setRowCount(0);
                    lblDetailHeader.setText("Chi tiết các sản phẩm trong phiếu nhập");
                }
            }
        });

        JScrollPane scrollOrders = new JScrollPane(tblOrders);
        scrollOrders.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Danh sách phiếu nhập hàng", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(51, 65, 85)));

        // Bảng Details
        JPanel detailContainer = new JPanel(new BorderLayout(5, 5));
        lblDetailHeader = new JLabel("Chi tiết các sản phẩm trong phiếu nhập (Chọn 1 phiếu để xem)");
        lblDetailHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetailHeader.setForeground(new Color(30, 41, 59));
        detailContainer.add(lblDetailHeader, BorderLayout.NORTH);

        String[] detailCols = {"Mã SP", "Tên Điện Thoại", "Số Lượng Nhập", "Đơn Giá Nhập", "Thành Tiền"};
        detailTableModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblDetails = new JTable(detailTableModel);
        tblDetails.setRowHeight(26);
        tblDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollDetails = new JScrollPane(tblDetails);
        detailContainer.add(scrollDetails, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollOrders, detailContainer);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);

        add(splitPane, BorderLayout.CENTER);
    }

    public void loadSuppliersFilter() {
        cboSuppliers.removeAllItems();
        cboSuppliers.addItem(new SupplierItem(0, "-- Tất cả nhà cung cấp --"));
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        for (Supplier s : suppliers) {
            cboSuppliers.addItem(new SupplierItem(s.getSupplierId(), s.getSupplierName()));
        }
    }

    public void loadOrders() {
        orderTableModel.setRowCount(0);
        detailTableModel.setRowCount(0);
        lblDetailHeader.setText("Chi tiết các sản phẩm trong phiếu nhập (Chọn 1 phiếu để xem)");

        String searchCode = txtSearchCode.getText().trim();
        Integer supplierId = null;
        SupplierItem selected = (SupplierItem) cboSuppliers.getSelectedItem();
        if (selected != null && selected.id > 0) {
            supplierId = selected.id;
        }

        LocalDate fromDate = parseDate(txtFromDate.getText().trim());
        LocalDate toDate = parseDate(txtToDate.getText().trim());

        List<PurchaseOrder> list = purchaseService.filterPurchaseOrders(searchCode, supplierId, fromDate, toDate);
        for (PurchaseOrder po : list) {
            orderTableModel.addRow(new Object[]{
                po.getPurchaseId(),
                po.getPurchaseCode(),
                po.getSupplierName(),
                po.getEmployeeName(),
                DateUtils.formatDateTime(po.getPurchaseDate()),
                CurrencyUtils.format(po.getTotalAmount()),
                po.getNote() != null ? po.getNote() : "",
                po.getStatus()
            });
        }
    }

    private void loadDetails(int purchaseId, String purchaseCode) {
        detailTableModel.setRowCount(0);
        List<PurchaseDetail> details = purchaseService.getPurchaseDetails(purchaseId);
        int totalQty = 0;
        for (PurchaseDetail d : details) {
            totalQty += d.getQuantity();
            detailTableModel.addRow(new Object[]{
                d.getProductCode(),
                d.getProductName(),
                d.getQuantity(),
                CurrencyUtils.format(d.getImportPrice()),
                CurrencyUtils.format(d.getSubtotal())
            });
        }
        lblDetailHeader.setText("Chi tiết phiếu nhập " + purchaseCode + ": Gồm " + details.size() + " mặt hàng (Tổng: " + totalQty + " máy)");
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ex) {
            return null;
        }
    }

    private static class SupplierItem {
        final int id;
        final String name;

        SupplierItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
