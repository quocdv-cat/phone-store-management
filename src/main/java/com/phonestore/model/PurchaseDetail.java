package com.phonestore.model;

import java.math.BigDecimal;

public class PurchaseDetail {

    private int purchaseDetailId;
    private int purchaseId;
    private int productId;
    private String productCode;
    private String productName;
    private int quantity;
    private BigDecimal importPrice;
    private BigDecimal subtotal;

    public PurchaseDetail() {
    }

    public int getPurchaseDetailId() {
        return purchaseDetailId;
    }

    public void setPurchaseDetailId(int purchaseDetailId) {
        this.purchaseDetailId = purchaseDetailId;
    }

    public int getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
