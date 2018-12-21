package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MatrixDialog extends JDialog implements ActionListener {
    private JTable matrixTable;

    public MatrixDialog(DefaultTableModel matrixModel, List<FluorInfo> fluorInfos) {
        super();
        // Generate Matrix and JTable.
        matrixTable = new JTable(matrixModel);
        // resizeColumnWidth(matrixTable);
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
            }
            {
                final JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                cancelButton.addActionListener(this);
                buttonPane.add(cancelButton);
            }
        }
        this.getContentPane().add(matrixPanel);
        // resizeColumnWidth(matrixTable);
        List<String> rowNames = new ArrayList<>();
        for (FluorInfo f : fluorInfos) {
            rowNames.add(f.getFluorName());
        }
        JTable rowTable = new RowNumberTable(matrixTable, rowNames);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("OK")) {
            System.out.println("OK pressed!");
        } else if (action.equals("Cancel")) {
            System.out.println("Cancel pressed!");
        }
    }

    public JTable getMatrixTable() {
        return matrixTable;
    }
}
