package com.phonestore.view;

import com.phonestore.model.Customer;
import com.phonestore.model.Invoice;
import com.phonestore.service.CustomerService;
import com.phonestore.service.InvoiceService;
import com.phonestore.utils.CurrencyUtils;
import com.phonestore.utils.DateUtils;
import com.phonestore.utils.MessageUtils;
import com.phonestore.view.customer.CustomerDialog;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
 * Giao diện Quản lý Khách hàng (CustomerPanel).
 * Phụ trách bởi Thành viên 2 (Trung - Core Transaction Developer).
 * Tính năng: Quản lý danh sách khách hàng + Bảng trực tiếp lịch sử mua hàng của khách được chọn.
 */
public class CustomerPanel extends JPanel {

    private final CustomerService customerService;
    private final InvoiceService invoiceService;

    // Customers Table
    private DefaultTableModel customerTableModel;
    private JTable customerTable;
    private JTextField txtSearch;
    private JLabel lblStatus;
    private List<Customer> currentCustomerList;

    // Invoices History Table
    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JLabel lblHistoryTitle;
    private JLabel lblTotalSpent;
    private List<Invoice> currentHistoryList;

    private JButton btnEdit;
    private JButton btnDelete;

    public CustomerPanel() {
        this.customerService = new CustomerService();
        this.invoiceService = new InvoiceService();
        initComponents();
        loadCustomers("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. TOP PANEL: Title, Search & Toolbar
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));

        JPanel headerRow = new JPanel(new BorderLayout());
        JLabel lblHeader = new JLabel("QUẢN LÝ KHÁCH HÀNG (CUSTOMER MANAGEMENT)");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(new Color(30, 41, 59));

        JLabel lblSubHeader = new JLabel("Thêm, sửa, xóa an toàn, tra cứu nhanh và theo dõi lịch sử giao dịch mua hàng");
        lblSubHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubHeader.setForeground(new Color(100, 116, 139));

        headerRow.add(lblHeader, BorderLayout.NORTH);
        headerRow.add(lblSubHeader, BorderLayout.SOUTH);
        topContainer.add(headerRow, BorderLayout.NORTH);

        // Toolbar + Search Bar
        JPanel toolRow = new JPanel(new BorderLayout(10, 0));
        toolRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnAdd = new JButton("Thêm Khách Hàng");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(new Color(37, 99, 235));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);

        JButton btnQuickAdd = new JButton("Tạo Nhanh (POS)");
        btnQuickAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickAdd.setBackground(new Color(16, 185, 129));
        btnQuickAdd.setForeground(Color.WHITE);
        btnQuickAdd.setFocusPainted(false);

        btnEdit = new JButton("Sửa Thông Tin");
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEdit.setEnabled(false);

        btnDelete = new JButton("Xóa Khách Hàng");
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDelete.setEnabled(false);

        actionButtons.add(btnAdd);
        actionButtons.add(btnQuickAdd);
        actionButtons.add(btnEdit);
        actionButtons.add(btnDelete);
        toolRow.add(actionButtons, BorderLayout.WEST);

        // Ô tìm kiếm gọn gàng, có sẵn placeholder, không cần nhãn "Từ khóa:" tránh bị che khuất
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Họ tên hoặc SĐT (10 số)...");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(new Color(15, 118, 110));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setFocusPainted(false);

        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnReset);
        toolRow.add(searchBox, BorderLayout.EAST);

        topContainer.add(toolRow, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 2. CENTER PANEL: SplitPane (Bảng Khách hàng ở trên, Bảng Lịch sử Hóa đơn ở dưới)
        // 2.1 Bảng Khách hàng
        JPanel customerTablePanel = new JPanel(new BorderLayout(0, 5));
        customerTablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Danh Sách Khách Hàng",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        String[] custCols = {"ID", "Họ và Tên", "Số Điện Thoại", "Email", "Địa Chỉ", "Ngày Tạo", "Đơn Đã Mua"};
        customerTableModel = new DefaultTableModel(custCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(28);
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        customerTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        customerTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        customerTable.getColumnModel().getColumn(5).setCellRenderer(centerRender);
        customerTable.getColumnModel().getColumn(6).setCellRenderer(centerRender);

        customerTablePanel.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // 2.2 Bảng Lịch sử Mua hàng
        JPanel historyPanel = new JPanel(new BorderLayout(0, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Lịch Sử Hóa Đơn Của Khách Hàng Đang Chọn",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(30, 41, 59)
        ));

        JPanel historyHeader = new JPanel(new BorderLayout(10, 0));
        historyHeader.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));

        lblHistoryTitle = new JLabel("Vui lòng chọn một khách hàng ở bảng trên để xem các hóa đơn đã mua.");
        lblHistoryTitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHistoryTitle.setForeground(new Color(71, 85, 105));

        lblTotalSpent = new JLabel("Tổng chi tiêu: 0 đ");
        lblTotalSpent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalSpent.setForeground(new Color(16, 185, 129));

        historyHeader.add(lblHistoryTitle, BorderLayout.WEST);
        historyHeader.add(lblTotalSpent, BorderLayout.EAST);
        historyPanel.add(historyHeader, BorderLayout.NORTH);

        String[] histCols = {"Mã Hóa Đơn", "Ngày Mua", "Nhân Viên Lập", "Tổng Tiền", "Giảm Giá", "Thực Thu", "Trạng Thái", "Ghi Chú"};
        historyTableModel = new DefaultTableModel(histCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(26);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(SwingConstants.RIGHT);
        historyTable.getColumnModel().getColumn(3).setCellRenderer(rightRender);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(rightRender);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(rightRender);

        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, customerTablePanel, historyPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);
        add(splitPane, BorderLayout.CENTER);

        // 3. BOTTOM STATUS BAR
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
        lblStatus = new JLabel("Đang tải dữ liệu...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(100, 116, 139));
        statusPanel.add(lblStatus, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        // Context Menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Chỉnh sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa khách hàng");
        popupMenu.add(miEdit);
        popupMenu.addSeparator();
        popupMenu.add(miDelete);
        miEdit.addActionListener(e -> editCustomer());
        miDelete.addActionListener(e -> deleteCustomer());
        customerTable.setComponentPopupMenu(popupMenu);

        // Double click history table row to view invoice details
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && historyTable.getSelectedRow() >= 0) {
                    Invoice inv = currentHistoryList.get(historyTable.getSelectedRow());
                    Window parent = SwingUtilities.getWindowAncestor(CustomerPanel.this);
                    new InvoicePreviewDialog(parent, inv.getInvoiceId()).setVisible(true);
                }
            }
        });

        // Event Handlers
        btnSearch.addActionListener(e -> loadCustomers(txtSearch.getText()));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadCustomers("");
        });
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadCustomers(txtSearch.getText());
                }
            }
        });

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Customer selected = getSelectedCustomer();
                boolean hasSelection = (selected != null);
                btnEdit.setEnabled(hasSelection);
                btnDelete.setEnabled(hasSelection);
                if (hasSelection) {
                    loadCustomerHistory(selected);
                } else {
                    clearCustomerHistory();
                }
            }
        });

        btnAdd.addActionListener(e -> addCustomer());
        btnQuickAdd.addActionListener(e -> quickAddCustomer());
        btnEdit.addActionListener(e -> editCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
    }

    public void loadCustomers(String keyword) {
        customerTableModel.setRowCount(0);
        clearCustomerHistory();
        currentCustomerList = customerService.searchCustomers(keyword);

        for (Customer c : currentCustomerList) {
            String orders = c.getTotalOrders() > 0 ? (c.getTotalOrders() + " hóa đơn") : "Chưa mua";
            customerTableModel.addRow(new Object[]{
                c.getCustomerId(),
                c.getFullName(),
                c.getPhone(),
                c.getEmail() != null && !c.getEmail().isEmpty() ? c.getEmail() : "—",
                c.getAddress() != null && !c.getAddress().isEmpty() ? c.getAddress() : "—",
                DateUtils.formatDateTime(c.getCreatedAt()),
                orders
            });
        }

        lblStatus.setText("Hiển thị " + currentCustomerList.size() + " khách hàng"
                + (keyword != null && !keyword.trim().isEmpty() ? " (lọc theo: '" + keyword.trim() + "')" : ""));

        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        // Auto select first customer if available
        if (!currentCustomerList.isEmpty()) {
            customerTable.setRowSelectionInterval(0, 0);
        }
    }

    private void loadCustomerHistory(Customer customer) {
        historyTableModel.setRowCount(0);
        lblHistoryTitle.setText("Khách hàng: " + customer.getFullName() + " (SĐT: " + customer.getPhone() + ") - Nhấp đúp vào hóa đơn để xem chi tiết / in");

        BigDecimal totalSpent = customerService.getTotalSpent(customer.getCustomerId());
        lblTotalSpent.setText("Tổng chi tiêu: " + CurrencyUtils.format(totalSpent));

        currentHistoryList = invoiceService.searchInvoices(null, customer.getCustomerId(), null, null, null);
        for (Invoice inv : currentHistoryList) {
            historyTableModel.addRow(new Object[]{
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
    }

    private void clearCustomerHistory() {
        historyTableModel.setRowCount(0);
        lblHistoryTitle.setText("Vui lòng chọn một khách hàng ở bảng trên để xem các hóa đơn đã mua.");
        lblTotalSpent.setText("Tổng chi tiêu: 0 đ");
    }

    private Customer getSelectedCustomer() {
        int row = customerTable.getSelectedRow();
        if (row >= 0 && currentCustomerList != null && row < currentCustomerList.size()) {
            return currentCustomerList.get(row);
        }
        return null;
    }

    private void addCustomer() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        CustomerDialog dialog = new CustomerDialog(parent, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadCustomers(txtSearch.getText());
        }
    }

    private void quickAddCustomer() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Customer created = QuickCustomerDialog.showDialog(parent, "");
        if (created != null) {
            loadCustomers("");
            for (int i = 0; i < currentCustomerList.size(); i++) {
                if (currentCustomerList.get(i).getCustomerId() == created.getCustomerId()) {
                    customerTable.setRowSelectionInterval(i, i);
                    customerTable.scrollRectToVisible(customerTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    private void editCustomer() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            MessageUtils.showWarning(this, "Vui lòng chọn khách hàng cần chỉnh sửa!");
            return;
        }

        Window parent = SwingUtilities.getWindowAncestor(this);
        CustomerDialog dialog = new CustomerDialog(parent, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadCustomers(txtSearch.getText());
        }
    }

    private void deleteCustomer() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            MessageUtils.showWarning(this, "Vui lòng chọn khách hàng cần xóa!");
            return;
        }

        if (selected.getTotalOrders() > 0) {
            MessageUtils.showWarning(this,
                    "KHÔNG THỂ XÓA!\nKhách hàng '" + selected.getFullName() + "' đã có "
                    + selected.getTotalOrders() + " hóa đơn mua hàng trong hệ thống.\n"
                    + "Để đảm bảo tính chính xác của sổ sách và doanh thu, dữ liệu khách hàng này không thể xóa.");
            return;
        }

        boolean confirm = MessageUtils.showConfirm(this,
                "Bạn có chắc chắn muốn xóa khách hàng:\n"
                + "ID: #" + selected.getCustomerId() + "\n"
                + "Họ tên: " + selected.getFullName() + "\n"
                + "Số điện thoại: " + selected.getPhone() + "\n\n"
                + "Lưu ý: Hành động này không thể hoàn tác!");

        if (confirm) {
            try {
                customerService.deleteCustomer(selected.getCustomerId());
                MessageUtils.showInfo(this, "Xóa khách hàng '" + selected.getFullName() + "' thành công!");
                loadCustomers(txtSearch.getText());
            } catch (Exception ex) {
                MessageUtils.showError(this, ex.getMessage());
            }
        }
    }
}
