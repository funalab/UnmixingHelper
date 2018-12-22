package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Akira Funahashi
 * @author Yuta Tokuoka
 * <p>
 * Yuta Tokuoka implemented a python code which generates matrix data.
 * Akira Funahashi ported the python code to Java, and made it as an ImageJ plugin.
 * </p>
 */
public class MatrixDialog extends JDialog implements ActionListener {
    private JTable matrixTable;
    private DefaultTableModel matrixModel;
    private List<FluorInfo> fluorInfos;

    public MatrixDialog(List<FluorInfo> _fluorInfos) {
        super();
        // Generate Matrix and JTable.
        fluorInfos = _fluorInfos;
        generateMatrixTableModel(fluorInfos);
        matrixTable = new JTable(matrixModel);
        JTableHeader header = matrixTable.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer(matrixTable));
        JPanel matrixPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(matrixTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBounds(700, 100, 450, 300);
        this.setLayout(new BorderLayout());
        matrixPanel.setLayout(new BorderLayout());
        matrixPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        matrixPanel.add(scrollPane, BorderLayout.CENTER);
        {
            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            matrixPanel.add(buttonPane, BorderLayout.SOUTH);
            {
                final JButton okButton = new JButton("OK");
                okButton.setActionCommand("OK");
                okButton.setEnabled(false);
                okButton.addActionListener(this);
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                matrixModel.addTableModelListener(e -> okButton.setEnabled(true));
            }
        }
        this.getContentPane().add(matrixPanel);
        List<String> rowNames = new ArrayList<>();
        for (FluorInfo f : fluorInfos) {
            rowNames.add(f.getFluorName());
        }
        JTable rowTable = new RowNumberTable(matrixTable, rowNames);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
        this.pack();
    }

    private void generateMatrixTableModel(List<FluorInfo> fluorInfos) {
        Object[][] data = generateMatrixTableData(fluorInfos);
        String[] columns = new String[fluorInfos.size()];
        int num = (int) 'z' - (int) 'a' + 1;
        for (int i = 0; i < fluorInfos.size(); i++) {
            int ascii = (int) 'a' + i % num;
            char c = (char) ascii;
            int idx = i / num;
            columns[i] = c + String.valueOf(idx);
        }
        //create table model with data
        matrixModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };
    }

    private Object[][] generateMatrixTableData(List<FluorInfo> fluorInfos) {
        int numColumns = fluorInfos.size();
        Object[][] data = new Object[numColumns][numColumns];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = 0d;
            }
        }
        return data;
    }

    public void resetTable() {
        generateMatrixTableModel(fluorInfos);
        matrixTable.setModel(matrixModel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("OK")) {
            dispose();
        }
    }

    public JTable getMatrixTable() {
        return matrixTable;
    }
}
