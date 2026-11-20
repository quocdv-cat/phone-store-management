package com.phonestore.view.customer;

import com.phonestore.model.Customer;
import com.phonestore.service.CustomerService;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Quản lý Khách hàng (Customer Management Panel).
 * Chức năng: Thêm, Sửa, Xóa (ràng buộc chưa từng mua hàng), Tìm kiếm nhanh theo SĐT hoặc Tên,
 * Xem lịch sử mua hàng, Dialog tạo nhanh cho POS.
 */
public class CustomerManagementPanel extends JPanel {

    private final CustomerService customerService;
    private DefaultTableModel tableModel;
    private JTable customerTable;
    private JTextField txtSearch;
    private JLabel lblStatus;
    private List<Customer> currentCustomerList;

    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnHistory;

    public CustomerManagementPanel() {
        this.customerService = new CustomerService();
        initComponents();
        loadCustomers("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. TOP PANEL: Title & Search bar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel lblHeader = new JLabel("QUẢN LÝ KHÁCH HÀNG (CUSTOMER MANAGEMENT)");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(new Color(30, 41, 59));

        JLabel lblSubHeader = new JLabel("Tra cứu, thêm mới, sửa, xóa và theo dõi toàn bộ lịch sử mua hàng của khách");
        lblSubHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubHeader.setForeground(new Color(100, 116, 139));

        titlePanel.add(lblHeader, BorderLayout.NORTH);
        titlePanel.add(lblSubHeader, BorderLayout.SOUTH);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Search Controls
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập Họ tên hoặc Số điện thoại (10 số)...");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(new Color(15, 118, 110));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);

        JButton btnResetSearch = new JButton("Làm mới");
        btnResetSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnResetSearch.setFocusPainted(false);

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // 2. TOOLBAR PANEL: Actions
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JButton btnAdd = new JButton("Thêm Khách Hàng");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setBackground(new Color(37, 99, 235));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);

        JButton btnQuickAdd = new JButton("Tạo Nhanh (Dùng Trong POS)");
        btnQuickAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnQuickAdd.setBackground(new Color(16, 185, 129));
        btnQuickAdd.setForeground(Color.WHITE);
        btnQuickAdd.setFocusPainted(false);
        btnQuickAdd.setToolTipText("Mở popup tạo nhanh khách hàng để kiểm tra dialog tích hợp khi lập hóa đơn bán hàng");

        btnEdit = new JButton("Sửa Thông Tin");
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnEdit.setEnabled(false);

        btnDelete = new JButton("Xóa Khách Hàng");
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnDelete.setEnabled(false);

        btnHistory = new JButton("Lịch Sử Mua Hàng");
        btnHistory.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHistory.setBackground(new Color(245, 158, 11));
        btnHistory.setForeground(Color.WHITE);
        btnHistory.setFocusPainted(false);
        btnHistory.setEnabled(false);

        toolbarPanel.add(btnAdd);
        toolbarPanel.add(btnQuickAdd);
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnHistory);

        JPanel northContainer = new JPanel(new BorderLayout(0, 10));
        northContainer.add(topPanel, BorderLayout.NORTH);
        northContainer.add(toolbarPanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // 3. CENTER PANEL: Customer JTable
        String[] columns = {
            "ID", "Họ và Tên", "Số Điện Thoại", "Email", "Địa Chỉ", "Ngày Tạo", "Đơn Đã Mua"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(30);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths & alignments
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(220);
        customerTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        customerTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        customerTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        customerTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        customerTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        customerTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        // 4. BOTTOM STATUS BAR
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        lblStatus = new JLabel("Đang tải dữ liệu...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(100, 116, 139));
        statusPanel.add(lblStatus, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        // Context Menu on right click
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem miHistory = new JMenuItem("Xem lịch sử mua hàng");
        JMenuItem miEdit = new JMenuItem("Chỉnh sửa thông tin");
        JMenuItem miDelete = new JMenuItem("Xóa khách hàng");
        popupMenu.add(miHistory);
        popupMenu.add(miEdit);
        popupMenu.addSeparator();
        popupMenu.add(miDelete);

        miHistory.addActionListener(e -> viewPurchaseHistory());
        miEdit.addActionListener(e -> editCustomer());
        miDelete.addActionListener(e -> deleteCustomer());

        customerTable.setComponentPopupMenu(popupMenu);

        // 5. EVENT LISTENERS
        btnSearch.addActionListener(e -> loadCustomers(txtSearch.getText()));
        btnResetSearch.addActionListener(e -> {
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
            boolean selected = customerTable.getSelectedRow() >= 0;
            btnEdit.setEnabled(selected);
            btnDelete.setEnabled(selected);
            btnHistory.setEnabled(selected);
        });

        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && customerTable.getSelectedRow() >= 0) {
                    viewPurchaseHistory();
                }
            }
        });

        btnAdd.addActionListener(e -> addCustomer());
        btnQuickAdd.addActionListener(e -> quickAddCustomer());
        btnEdit.addActionListener(e -> editCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnHistory.addActionListener(e -> viewPurchaseHistory());
    }

    /**
     * Tải danh sách khách hàng từ cơ sở dữ liệu lên bảng.
     */
    public void loadCustomers(String keyword) {
        tableModel.setRowCount(0);
        currentCustomerList = customerService.searchCustomers(keyword);

        for (Customer c : currentCustomerList) {
            String ordersDisplay = c.getTotalOrders() > 0 ? (c.getTotalOrders() + " hóa đơn") : "Chưa mua";
            tableModel.addRow(new Object[]{
                c.getCustomerId(),
                c.getFullName(),
                c.getPhone(),
                c.getEmail() != null && !c.getEmail().isEmpty() ? c.getEmail() : "—",
                c.getAddress() != null && !c.getAddress().isEmpty() ? c.getAddress() : "—",
                DateUtils.formatDateTime(c.getCreatedAt()),
                ordersDisplay
            });
        }

        lblStatus.setText("Hiển thị " + currentCustomerList.size() + " khách hàng"
                + (keyword != null && !keyword.trim().isEmpty() ? " (kết quả lọc theo: '" + keyword.trim() + "')" : ""));

        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        btnHistory.setEnabled(false);
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
        Customer created = CustomerQuickCreateDialog.showDialog(parent, "");
        if (created != null) {
            loadCustomers("");
            // Highlight khách hàng vừa thêm
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

        // 1. Kiểm tra điều kiện: chỉ xóa được khách hàng chưa từng mua hàng
        if (selected.getTotalOrders() > 0) {
            MessageUtils.showWarning(this,
                    "KHÔNG THỂ XÓA!\nKhách hàng '" + selected.getFullName() + "' đã có "
                    + selected.getTotalOrders() + " hóa đơn mua hàng trong hệ thống.\n"
                    + "Để đảm bảo tính chính xác của sổ sách và doanh thu, dữ liệu khách hàng này không thể xóa.");
            return;
        }

        // 3. Xác nhận xóa
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
                MessageUtils.showError(this, "Lỗi khi xóa khách hàng: " + ex.getMessage());
            }
        }
    }

    private void viewPurchaseHistory() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            MessageUtils.showWarning(this, "Vui lòng chọn khách hàng để xem lịch sử mua hàng!");
            return;
        }

        Window parent = SwingUtilities.getWindowAncestor(this);
        CustomerHistoryDialog dialog = new CustomerHistoryDialog(parent, selected);
        dialog.setVisible(true);
    }
}
