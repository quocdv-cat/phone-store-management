package com.phonestore.view;

import com.phonestore.model.PositionPermission;
import com.phonestore.service.EmployeeService;
import com.phonestore.utils.MessageUtils;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Cửa sổ "Sửa quyền" bật lên cho từng Chức vụ riêng biệt (Không cần Mã quyền).
 * Thiết kế theo đúng bố cục mẫu: Tên quyền -> Chi tiết quyền (Checkbox + ComboBox) -> Nút Sửa / Hủy.
 */
public class PositionPermissionDialog extends JDialog {

    private final EmployeeService employeeService = new EmployeeService();
    private final Runnable onPermissionChanged;

    private JComboBox<String> cbxPositionName;

    private JCheckBox chkSales;
    private JComboBox<String> cbxModeSales;

    private JCheckBox chkPurchase;
    private JComboBox<String> cbxModePurchase;

    private JCheckBox chkProducts;
    private JComboBox<String> cbxModeProducts;

    private JCheckBox chkCatBrand;
    private JComboBox<String> cbxModeCatBrand;

    private JCheckBox chkInvoices;
    private JComboBox<String> cbxModeInvoices;

    private JCheckBox chkCustomers;
    private JComboBox<String> cbxModeCustomers;

    private JCheckBox chkSuppliers;
    private JComboBox<String> cbxModeSuppliers;

    private JCheckBox chkEmployees;
    private JComboBox<String> cbxModeEmployees;

    private JCheckBox chkStatistics;
    private JComboBox<String> cbxModeStatistics;

    public PositionPermissionDialog(Frame parent, String initialPosition, Runnable onPermissionChanged) {
        super(parent, "Sửa quyền", true);
        this.onPermissionChanged = onPermissionChanged;
        initComponents();
        if (initialPosition != null && !initialPosition.trim().isEmpty()) {
            cbxPositionName.setSelectedItem(initialPosition.trim());
        }
        loadPermissionForSelectedPosition();
    }

    private void initComponents() {
        setSize(430, 640);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 12));
        root.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // 1. KHUNG TRÊN: Tên quyền (Không cần Mã quyền)
        JPanel topNamePanel = new JPanel(new BorderLayout());
        topNamePanel.setBorder(BorderFactory.createTitledBorder("Tên quyền"));
        cbxPositionName = new JComboBox<>(new String[]{
            "Nhân viên bán hàng", "Nhân viên kho", "Cửa hàng trưởng", "Kế toán", "Kỹ thuật viên"
        });
        cbxPositionName.setEditable(true);
        cbxPositionName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxPositionName.setPreferredSize(new Dimension(260, 32));
        cbxPositionName.addActionListener(e -> loadPermissionForSelectedPosition());
        topNamePanel.add(cbxPositionName, BorderLayout.CENTER);

        root.add(topNamePanel, BorderLayout.NORTH);

        // 2. KHUNG GIỮA: Chi tiết quyền:
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết quyền:"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        chkSales = new JCheckBox("Bán Hàng");
        cbxModeSales = new JComboBox<>(new String[]{"Bán hàng"});

        chkPurchase = new JCheckBox("Nhập Hàng");
        cbxModePurchase = new JComboBox<>(new String[]{"Nhập hàng"});

        chkProducts = new JCheckBox("Sản Phẩm");
        cbxModeProducts = new JComboBox<>(new String[]{"Chỉ xem", "Toàn quyền"});

        chkCatBrand = new JCheckBox("Loại Sản Phẩm");
        cbxModeCatBrand = new JComboBox<>(new String[]{"Chỉ xem", "Toàn quyền"});

        chkInvoices = new JCheckBox("Hóa Đơn");
        cbxModeInvoices = new JComboBox<>(new String[]{"Chỉ xem", "Toàn quyền"});

        chkCustomers = new JCheckBox("Khách Hàng");
        cbxModeCustomers = new JComboBox<>(new String[]{"Chỉ xem", "Toàn quyền"});

        chkSuppliers = new JCheckBox("Nhà Cung Cấp");
        cbxModeSuppliers = new JComboBox<>(new String[]{"Chỉ xem", "Toàn quyền"});

        chkEmployees = new JCheckBox("Nhân Viên & Tài Khoản");
        cbxModeEmployees = new JComboBox<>(new String[]{"Chỉ xem", "Toàn quyền"});

        chkStatistics = new JCheckBox("Thống Kê Doanh Thu");
        cbxModeStatistics = new JComboBox<>(new String[]{"Chỉ xem", "Toàn quyền"});

        addPermissionRow(detailPanel, gbc, 0, chkSales, cbxModeSales);
        addPermissionRow(detailPanel, gbc, 1, chkPurchase, cbxModePurchase);
        addPermissionRow(detailPanel, gbc, 2, chkProducts, cbxModeProducts);
        addPermissionRow(detailPanel, gbc, 3, chkCatBrand, cbxModeCatBrand);
        addPermissionRow(detailPanel, gbc, 4, chkInvoices, cbxModeInvoices);
        addPermissionRow(detailPanel, gbc, 5, chkCustomers, cbxModeCustomers);
        addPermissionRow(detailPanel, gbc, 6, chkSuppliers, cbxModeSuppliers);
        addPermissionRow(detailPanel, gbc, 7, chkEmployees, cbxModeEmployees);
        addPermissionRow(detailPanel, gbc, 8, chkStatistics, cbxModeStatistics);

        root.add(detailPanel, BorderLayout.CENTER);

        // 3. KHUNG DƯỚI: Nút Sửa & Hủy
        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));

        JButton btnSave = new JButton("Sửa", createWrenchIcon());
        btnSave.setIconTextGap(10);
        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnSave.setPreferredSize(new Dimension(115, 42));
        btnSave.addActionListener(e -> handleSave());

        JButton btnCancel = new JButton("Hủy", createCancelIcon());
        btnCancel.setIconTextGap(10);
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.setPreferredSize(new Dimension(115, 42));
        btnCancel.addActionListener(e -> dispose());

        bottomButtons.add(btnSave);
        bottomButtons.add(btnCancel);

        root.add(bottomButtons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addPermissionRow(JPanel panel, GridBagConstraints gbc, int row, JCheckBox checkBox, JComboBox<String> comboBox) {
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setPreferredSize(new Dimension(125, 28));

        checkBox.addActionListener(e -> comboBox.setEnabled(checkBox.isSelected()));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.45;
        panel.add(checkBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        panel.add(comboBox, gbc);
    }

    private void loadPermissionForSelectedPosition() {
        Object selected = cbxPositionName.getSelectedItem();
        if (selected == null) {
            return;
        }
        String posName = selected.toString().trim();
        PositionPermission perm = employeeService.getPermissionByPosition(posName);

        setRowState(chkSales, cbxModeSales, perm.isCanSales(), perm.getModeSales());
        setRowState(chkPurchase, cbxModePurchase, perm.isCanPurchase(), perm.getModePurchase());
        setRowState(chkProducts, cbxModeProducts, perm.isCanProducts(), perm.getModeProducts());
        setRowState(chkCatBrand, cbxModeCatBrand, perm.isCanCatBrand(), perm.getModeCatBrand());
        setRowState(chkInvoices, cbxModeInvoices, perm.isCanInvoices(), perm.getModeInvoices());
        setRowState(chkCustomers, cbxModeCustomers, perm.isCanCustomers(), perm.getModeCustomers());
        setRowState(chkSuppliers, cbxModeSuppliers, perm.isCanSuppliers(), perm.getModeSuppliers());
        setRowState(chkEmployees, cbxModeEmployees, perm.isCanEmployees(), perm.getModeEmployees());
        setRowState(chkStatistics, cbxModeStatistics, perm.isCanStatistics(), perm.getModeStatistics());
    }

    private void setRowState(JCheckBox chk, JComboBox<String> cbx, boolean checked, String mode) {
        chk.setSelected(checked);
        cbx.setEnabled(checked);
        if (mode != null) {
            cbx.setSelectedItem(mode);
        }
    }

    private void handleSave() {
        Object selected = cbxPositionName.getSelectedItem();
        String posName = selected != null ? selected.toString().trim() : "";

        PositionPermission perm = new PositionPermission(
                posName,
                chkProducts.isSelected(),
                chkCatBrand.isSelected(),
                chkSales.isSelected(),
                chkInvoices.isSelected(),
                chkCustomers.isSelected(),
                chkPurchase.isSelected(),
                chkSuppliers.isSelected(),
                chkEmployees.isSelected(),
                chkStatistics.isSelected()
        );
        perm.setModeSales(String.valueOf(cbxModeSales.getSelectedItem()));
        perm.setModePurchase(String.valueOf(cbxModePurchase.getSelectedItem()));
        perm.setModeProducts(String.valueOf(cbxModeProducts.getSelectedItem()));
        perm.setModeCatBrand(String.valueOf(cbxModeCatBrand.getSelectedItem()));
        perm.setModeInvoices(String.valueOf(cbxModeInvoices.getSelectedItem()));
        perm.setModeCustomers(String.valueOf(cbxModeCustomers.getSelectedItem()));
        perm.setModeSuppliers(String.valueOf(cbxModeSuppliers.getSelectedItem()));
        perm.setModeEmployees(String.valueOf(cbxModeEmployees.getSelectedItem()));
        perm.setModeStatistics(String.valueOf(cbxModeStatistics.getSelectedItem()));

        String err = employeeService.savePositionPermission(perm);
        if (err != null) {
            MessageUtils.showError(this, err);
            return;
        }

        MessageUtils.showInfo(this, "Đã cập nhật quyền cho chức vụ '" + posName + "' thành công!");
        if (onPermissionChanged != null) {
            onPermissionChanged.run();
        }
        dispose();
    }

    private static Icon createWrenchIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(new Color(95, 125, 139));
                g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(4, 18, 14, 8);
                g2.drawArc(11, 2, 9, 9, 45, 270);
                g2.fillOval(2, 16, 5, 5);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 22;
            }

            @Override
            public int getIconHeight() {
                return 22;
            }
        };
    }

    private static Icon createCancelIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(new Color(239, 68, 68));
                g2.fillOval(1, 1, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(7, 7, 15, 15);
                g2.drawLine(15, 7, 7, 15);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 22;
            }

            @Override
            public int getIconHeight() {
                return 22;
            }
        };
    }
}
