package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import net.imagej.DatasetService;
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
import java.util.ArrayList;
import java.util.List;

public class UnmixingHelperDialog extends JDialog implements ActionListener {
    private DatasetService datasetService;
    private OpService opService;
    private LogService log;
    private StatusService statusService;
    private UIService uiService;
    private String[] columns = new String[] {
            "File Name", "Fluor Name", "Exposure Time (ms)", "Background"
    };
    private List<FluorInfo> fluorInfos;
    private DefaultTableModel model, matrixModel;

    private final JPanel contentPanel = new JPanel();
    private JDialog matrixDialog; // = new JDialog();
    private JTable matrixTable;

    public UnmixingHelperDialog(List<FluorInfo> _fluorInfos) {
        fluorInfos = _fluorInfos;
        generateUITableModel(fluorInfos);
        generateMatrixTableModel(fluorInfos);
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

    private void generateMatrixTableModel(List<FluorInfo> fluorInfos) {
        Object[][] data = generateMatrixTableData(fluorInfos);
        String[] columns = new String[fluorInfos.size()];
        for (int i = 0; i < fluorInfos.size(); i++) {
            columns[i] = fluorInfos.get(i).getFluorName();
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

    private void generateUITableModel(List<FluorInfo> fluorInfos) {
        Object[][] data = generateUITableData(fluorInfos);
        final Class[] columnClass = new Class[] {
                String.class, String.class, Double.class, Double.class
        };
        //create table model with data
        model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // "File Name", "Fluor Name", "Exposure Time (ms)", "BackGround"
                if (column == 0) {
                    return false;
                }
                return true;
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
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }
            if(width > 300) {
                width=300;
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("OK")) {
            System.out.println("OK pressed!");
        } else if (action.equals("Cancel")) {
            System.out.println("Cancel pressed!");
        } else if (action.equals("Edit Matrix")) {
            System.out.println("Edit Matrix pressed!");
            if (matrixTable == null) {
                matrixDialog = new MatrixDialog(matrixModel, fluorInfos);
                matrixTable = ((MatrixDialog) matrixDialog).getMatrixTable();
            }
            /*
            if (matrixTable == null) {
                // Generate Matrix and JTable.
                matrixTable = new JTable(matrixModel);
                resizeColumnWidth(matrixTable);
                JPanel matrixPanel = new JPanel();
                JScrollPane scrollPane = new JScrollPane(matrixTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                matrixDialog.setBounds(700, 100, 450, 300);
                matrixDialog.setLayout(new BorderLayout());
                matrixPanel.setLayout(new BorderLayout());
                matrixPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
                matrixPanel.add(scrollPane, BorderLayout.CENTER);
                {
                    JPanel buttonPane = new JPanel();
                    // final JPanel buttonPane = new JPanel();
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
                matrixDialog.getContentPane().add(matrixPanel);
                resizeColumnWidth(matrixTable);
                List<String> rowNames = new ArrayList<>();
                for (FluorInfo f : fluorInfos) {
                    rowNames.add(f.getFluorName());
                }
                JTable rowTable = new RowNumberTable(matrixTable, rowNames);
                scrollPane.setRowHeaderView(rowTable);
                scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
                matrixDialog.pack();
            }
            */
            matrixDialog.show();
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
        try {
            final UnmixingHelperDialog dialog = new UnmixingHelperDialog(null);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
