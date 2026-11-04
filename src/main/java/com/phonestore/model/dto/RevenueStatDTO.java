package com.phonestore.model.dto;

import java.math.BigDecimal;

public class RevenueStatDTO {

    private String periodLabel;
    private int invoiceCount;
    private BigDecimal totalRevenue;

    public RevenueStatDTO() {
    }

    public RevenueStatDTO(String periodLabel, int invoiceCount, BigDecimal totalRevenue) {
        this.periodLabel = periodLabel;
        this.invoiceCount = invoiceCount;
        this.totalRevenue = totalRevenue;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public int getInvoiceCount() {
        return invoiceCount;
    }

    public void setInvoiceCount(int invoiceCount) {
        this.invoiceCount = invoiceCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
