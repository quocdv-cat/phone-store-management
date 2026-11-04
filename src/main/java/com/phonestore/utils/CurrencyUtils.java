package com.phonestore.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tiện ích định dạng tiền tệ VNĐ.
 */
public class CurrencyUtils {

    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        VND_FORMAT = new DecimalFormat("#,##0 đ", symbols);
    }

    private CurrencyUtils() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "0 đ";
        }
        return VND_FORMAT.format(amount);
    }

    public static String format(double amount) {
        return VND_FORMAT.format(amount);
    }
}
