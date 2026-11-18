package com.phonestore.model.dto;

import java.math.BigDecimal;

public class RevenueStatDTO {

    private String periodLabel;
    private int invoiceCount;
    private BigDecimal totalRevenue;
    private BigDecimal estimatedProfit = BigDecimal.ZERO;

    public RevenueStatDTO() {
    }

    public RevenueStatDTO(String periodLabel, int invoiceCount, BigDecimal totalRevenue) {
        this.periodLabel = periodLabel;
        this.invoiceCount = invoiceCount;
        this.totalRevenue = totalRevenue;
        this.estimatedProfit = BigDecimal.ZERO;
    }

    public RevenueStatDTO(String periodLabel, int invoiceCount, BigDecimal totalRevenue, BigDecimal estimatedProfit) {
        this.periodLabel = periodLabel;
        this.invoiceCount = invoiceCount;
        this.totalRevenue = totalRevenue;
        this.estimatedProfit = estimatedProfit;
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

    public BigDecimal getEstimatedProfit() {
        return estimatedProfit;
    }

    public void setEstimatedProfit(BigDecimal estimatedProfit) {
        this.estimatedProfit = estimatedProfit;
    }
}
