package com.phonestore.service;

import com.phonestore.dao.StatisticsDAO;
import com.phonestore.model.Product;
import com.phonestore.model.dto.DashboardSummaryDTO;
import com.phonestore.model.dto.RevenueStatDTO;
import com.phonestore.model.dto.TopProductDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý nghiệp vụ Báo cáo Thống kê & Dashboard.
 */
public class StatisticsService {

    private final StatisticsDAO statisticsDAO;

    public StatisticsService() {
        this.statisticsDAO = new StatisticsDAO();
    }

    public StatisticsService(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    public DashboardSummaryDTO getDashboardSummary() {
        return statisticsDAO.getDashboardSummary();
    }

    public List<RevenueStatDTO> getRevenueByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        if (fromDate.isAfter(toDate)) {
            LocalDate tmp = fromDate;
            fromDate = toDate;
            toDate = tmp;
        }
        return statisticsDAO.getRevenueByDateRange(fromDate, toDate);
    }

    public List<RevenueStatDTO> getRevenueBy12Months(int year) {
        if (year <= 0) {
            year = LocalDate.now().getYear();
        }
        return statisticsDAO.getRevenueBy12Months(year);
    }

    public List<TopProductDTO> getTopSellingProducts(int limit) {
        int n = limit > 0 ? limit : 10;
        return statisticsDAO.getTopSellingProducts(n);
    }

    public List<Product> getLowStockProducts(int threshold) {
        int th = threshold >= 0 ? threshold : 5;
        return statisticsDAO.getLowStockProducts(th);
    }
}
