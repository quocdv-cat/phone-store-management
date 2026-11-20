package com.phonestore.utils;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

/**
 * Tiện ích xuất dữ liệu từ bảng JTable ra file CSV chuẩn UTF-8 BOM (mở trực tiếp bằng Microsoft Excel không lỗi tiếng Việt).
 */
public class CsvExportUtils {

    private CsvExportUtils() {
    }

    public static void exportTableToCsv(Component parent, JTable table, String defaultFilePrefix) {
        if (table == null || table.getModel().getRowCount() == 0) {
            MessageUtils.showWarning(parent, "Bảng hiện tại chưa có dữ liệu để xuất ra file Excel!");
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu file Excel CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel CSV Files (*.csv)", "csv"));
        chooser.setSelectedFile(new File(defaultFilePrefix + "_" + timestamp + ".csv"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        TableModel model = table.getModel();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // Ghi mã UTF-8 BOM để Microsoft Excel nhận diện đúng tiếng Việt có dấu
            writer.write('\uFEFF');

            int colCount = model.getColumnCount();
            for (int col = 0; col < colCount; col++) {
                writer.write(escapeCsv(model.getColumnName(col)));
                if (col < colCount - 1) {
                    writer.write(',');
                }
            }
            writer.newLine();

            int rowCount = model.getRowCount();
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    Object val = model.getValueAt(row, col);
                    writer.write(escapeCsv(val == null ? "" : val.toString()));
                    if (col < colCount - 1) {
                        writer.write(',');
                    }
                }
                writer.newLine();
            }

            MessageUtils.showInfo(parent, "Đã xuất dữ liệu ra file Excel thành công:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageUtils.showError(parent, "Lỗi khi xuất file Excel: " + ex.getMessage());
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
