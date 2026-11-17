package com.phonestore.model;

/**
 * Lưu trữ cấu hình phân quyền truy cập và mức độ quyền (Chỉ xem / Toàn quyền)
 * theo từng Chức vụ (Tên quyền).
 */
public class PositionPermission {

    private String positionName;
    private boolean canProducts;
    private String modeProducts = "Chỉ xem";

    private boolean canCatBrand;
    private String modeCatBrand = "Chỉ xem";

    private boolean canSales;
    private String modeSales = "Bán hàng";

    private boolean canInvoices;
    private String modeInvoices = "Chỉ xem";

    private boolean canCustomers;
    private String modeCustomers = "Toàn quyền";

    private boolean canPurchase;
    private String modePurchase = "Nhập hàng";

    private boolean canSuppliers;
    private String modeSuppliers = "Chỉ xem";

    private boolean canEmployees;
    private String modeEmployees = "Chỉ xem";

    private boolean canStatistics;
    private String modeStatistics = "Chỉ xem";

    public PositionPermission() {
    }

    public PositionPermission(String positionName, boolean canProducts, boolean canCatBrand,
                              boolean canSales, boolean canInvoices, boolean canCustomers,
                              boolean canPurchase, boolean canSuppliers, boolean canEmployees,
                              boolean canStatistics) {
        this.positionName = positionName;
        this.canProducts = canProducts;
        this.canCatBrand = canCatBrand;
        this.canSales = canSales;
        this.canInvoices = canInvoices;
        this.canCustomers = canCustomers;
        this.canPurchase = canPurchase;
        this.canSuppliers = canSuppliers;
        this.canEmployees = canEmployees;
        this.canStatistics = canStatistics;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public boolean isCanProducts() {
        return canProducts;
    }

    public void setCanProducts(boolean canProducts) {
        this.canProducts = canProducts;
    }

    public String getModeProducts() {
        return modeProducts;
    }

    public void setModeProducts(String modeProducts) {
        this.modeProducts = modeProducts;
    }

    public boolean isCanCatBrand() {
        return canCatBrand;
    }

    public void setCanCatBrand(boolean canCatBrand) {
        this.canCatBrand = canCatBrand;
    }

    public String getModeCatBrand() {
        return modeCatBrand;
    }

    public void setModeCatBrand(String modeCatBrand) {
        this.modeCatBrand = modeCatBrand;
    }

    public boolean isCanSales() {
        return canSales;
    }

    public void setCanSales(boolean canSales) {
        this.canSales = canSales;
    }

    public String getModeSales() {
        return modeSales;
    }

    public void setModeSales(String modeSales) {
        this.modeSales = modeSales;
    }

    public boolean isCanInvoices() {
        return canInvoices;
    }

    public void setCanInvoices(boolean canInvoices) {
        this.canInvoices = canInvoices;
    }

    public String getModeInvoices() {
        return modeInvoices;
    }

    public void setModeInvoices(String modeInvoices) {
        this.modeInvoices = modeInvoices;
    }

    public boolean isCanCustomers() {
        return canCustomers;
    }

    public void setCanCustomers(boolean canCustomers) {
        this.canCustomers = canCustomers;
    }

    public String getModeCustomers() {
        return modeCustomers;
    }

    public void setModeCustomers(String modeCustomers) {
        this.modeCustomers = modeCustomers;
    }

    public boolean isCanPurchase() {
        return canPurchase;
    }

    public void setCanPurchase(boolean canPurchase) {
        this.canPurchase = canPurchase;
    }

    public String getModePurchase() {
        return modePurchase;
    }

    public void setModePurchase(String modePurchase) {
        this.modePurchase = modePurchase;
    }

    public boolean isCanSuppliers() {
        return canSuppliers;
    }

    public void setCanSuppliers(boolean canSuppliers) {
        this.canSuppliers = canSuppliers;
    }

    public String getModeSuppliers() {
        return modeSuppliers;
    }

    public void setModeSuppliers(String modeSuppliers) {
        this.modeSuppliers = modeSuppliers;
    }

    public boolean isCanEmployees() {
        return canEmployees;
    }

    public void setCanEmployees(boolean canEmployees) {
        this.canEmployees = canEmployees;
    }

    public String getModeEmployees() {
        return modeEmployees;
    }

    public void setModeEmployees(String modeEmployees) {
        this.modeEmployees = modeEmployees;
    }

    public boolean isCanStatistics() {
        return canStatistics;
    }

    public void setCanStatistics(boolean canStatistics) {
        this.canStatistics = canStatistics;
    }

    public String getModeStatistics() {
        return modeStatistics;
    }

    public void setModeStatistics(String modeStatistics) {
        this.modeStatistics = modeStatistics;
    }
}
