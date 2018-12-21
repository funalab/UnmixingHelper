package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class HeaderRenderer implements TableCellRenderer {

    DefaultTableCellRenderer renderer;

    public HeaderRenderer(JTable table) {
        renderer = (DefaultTableCellRenderer)
                table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col) {
        return renderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, col);
    }
}
