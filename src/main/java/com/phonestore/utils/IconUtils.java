package com.phonestore.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Tiện ích vẽ các biểu tượng (Vector Icons) sắc nét cho danh sách Module trên Sidebar.
 */
public class IconUtils {

    public enum IconType {
        PHONE, CATEGORY, CART, INVOICE, CUSTOMER, WAREHOUSE, SUPPLIER, EMPLOYEE, CHART, KEY, LOGOUT
    }

    private IconUtils() {
    }

    public static Icon createSidebarIcon(IconType type, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.translate(x, y);

                switch (type) {
                    case PHONE -> {
                        g2.drawRoundRect(4, 1, 10, 16, 3, 3);
                        g2.drawLine(7, 3, 11, 3);
                        g2.fillOval(8, 14, 2, 2);
                    }
                    case CATEGORY -> {
                        g2.drawRoundRect(2, 2, 6, 6, 2, 2);
                        g2.drawRoundRect(10, 2, 6, 6, 2, 2);
                        g2.drawRoundRect(2, 10, 6, 6, 2, 2);
                        g2.drawRoundRect(10, 10, 6, 6, 2, 2);
                    }
                    case CART -> {
                        g2.drawLine(1, 2, 4, 2);
                        g2.drawLine(4, 2, 6, 11);
                        g2.drawLine(6, 11, 15, 11);
                        g2.drawLine(15, 11, 17, 5);
                        g2.drawLine(5, 5, 17, 5);
                        g2.fillOval(6, 13, 3, 3);
                        g2.fillOval(13, 13, 3, 3);
                    }
                    case INVOICE -> {
                        g2.drawRoundRect(3, 1, 12, 16, 2, 2);
                        g2.drawLine(6, 5, 12, 5);
                        g2.drawLine(6, 9, 12, 9);
                        g2.drawLine(6, 13, 10, 13);
                    }
                    case CUSTOMER -> {
                        g2.drawOval(6, 2, 6, 6);
                        g2.drawArc(2, 10, 14, 8, 0, 180);
                    }
                    case WAREHOUSE -> {
                        int[] px = {9, 2, 16};
                        int[] py = {2, 7, 7};
                        g2.drawPolygon(px, py, 3);
                        g2.drawRect(3, 7, 12, 9);
                        g2.drawRect(7, 11, 4, 5);
                    }
                    case SUPPLIER -> {
                        g2.drawRect(1, 4, 10, 8);
                        g2.drawPolygon(new int[]{11, 15, 17, 17, 11}, new int[]{6, 6, 9, 12, 12}, 5);
                        g2.fillOval(3, 12, 3, 3);
                        g2.fillOval(12, 12, 3, 3);
                    }
                    case EMPLOYEE -> {
                        g2.drawOval(4, 2, 6, 6);
                        g2.drawArc(1, 10, 12, 7, 0, 180);
                        g2.drawLine(14, 6, 14, 12);
                        g2.drawLine(11, 9, 17, 9);
                    }
                    case CHART -> {
                        g2.drawLine(2, 16, 16, 16);
                        g2.fillRect(3, 10, 3, 6);
                        g2.fillRect(8, 6, 3, 10);
                        g2.fillRect(13, 2, 3, 14);
                    }
                    case KEY -> {
                        g2.drawOval(2, 5, 7, 7);
                        g2.drawLine(9, 8, 16, 8);
                        g2.drawLine(13, 8, 13, 11);
                        g2.drawLine(16, 8, 16, 12);
                    }
                    case LOGOUT -> {
                        g2.drawLine(8, 2, 3, 2);
                        g2.drawLine(3, 2, 3, 16);
                        g2.drawLine(3, 16, 8, 16);
                        g2.drawLine(7, 9, 16, 9);
                        g2.drawLine(12, 5, 16, 9);
                        g2.drawLine(12, 13, 16, 9);
                    }
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }
}
