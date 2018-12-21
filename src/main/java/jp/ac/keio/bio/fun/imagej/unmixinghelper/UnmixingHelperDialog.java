package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.ui.UIService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UnmixingHelperDialog extends JDialog implements ActionListener {
    private DatasetService datasetService;
    private OpService opService;
    private LogService log;
    private StatusService statusService;
    private UIService uiService;
    private String[] columns = new String[]{
            "File Name", "Fluor Name", "Exposure Time (ms)", "Background"
    };
    private List<FluorInfo> fluorInfos;
    private DefaultTableModel model;

    private final JPanel contentPanel = new JPanel();
    private MatrixDialog matrixDialog;
    private JTable matrixTable;

    public UnmixingHelperDialog(List<FluorInfo> _fluorInfos) {
        fluorInfos = _fluorInfos;
        generateUITableModel(fluorInfos);
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            JTable table = new JTable(model);
            resizeColumnWidth(table);
            getContentPane().add(new JScrollPane(table));
            resizeColumnWidth(table);

            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                final JButton matrixButton = new JButton("Edit Matrix");
                matrixButton.setActionCommand("Edit Matrix");
                matrixButton.addActionListener(this);
                buttonPane.add(matrixButton);
            }
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
    }

    private void generateUITableModel(List<FluorInfo> fluorInfos) {
        Object[][] data = generateUITableData(fluorInfos);
        final Class[] columnClass = new Class[]{
                String.class, String.class, Double.class, Double.class
        };
        //create table model with data
        model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // "File Name", "Fluor Name", "Exposure Time (ms)", "BackGround"
                return column != 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnClass[columnIndex];
            }
        };
    }

    private Object[][] generateUITableData(List<FluorInfo> fluorInfos) {
        int numColumns = 4;
        Object[][] data = new Object[fluorInfos.size()][numColumns];
        for (int i = 0; i < fluorInfos.size(); i++) {
            FluorInfo fi = fluorInfos.get(i);
            data[i][0] = fi.getFileName();
            data[i][1] = fi.getFluorName();
            data[i][2] = fi.getExposureTime();
            data[i][3] = fi.getBackGround();
        }
        return data;
    }

    private void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            if (width > 300) {
                width = 300;
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case "OK":
                System.out.println("OK pressed!");
                break;
            case "Cancel":
                if (matrixDialog.isVisible())
                    matrixDialog.dispose();
                dispose();
                break;
            case "Edit Matrix":
                if (matrixTable == null) {
                    matrixDialog = new MatrixDialog(fluorInfos);
                    matrixTable = matrixDialog.getMatrixTable();
                }
                matrixDialog.setVisible(true);
                break;
        }
    }

    public DatasetService getDatasetService() {
        return datasetService;
    }

    public void setDatasetService(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    public OpService getOpService() {
        return opService;
    }

    public void setOpService(OpService opService) {
        this.opService = opService;
    }

    public LogService getLog() {
        return log;
    }

    public void setLog(LogService log) {
        this.log = log;
    }

    public StatusService getStatusService() {
        return statusService;
    }

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public UIService getUiService() {
        return uiService;
    }

    public void setUiService(UIService uiService) {
        this.uiService = uiService;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Launch the application.
     */
    public static void main(final String[] args) {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
    }

}
