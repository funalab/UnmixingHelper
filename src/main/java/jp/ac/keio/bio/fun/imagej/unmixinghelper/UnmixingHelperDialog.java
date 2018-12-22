package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import Jama.Matrix;
import ij.io.SaveDialog;
import jp.ac.keio.bio.fun.imagej.unmixinghelper.util.StringUtil;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.ui.UIService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class UnmixingHelperDialog extends JDialog implements ActionListener, TableModelListener {
    private DatasetService datasetService;
    private OpService opService;
    private LogService log;
    private StatusService statusService;
    private UIService uiService;
    private static final String[] columns = new String[]{
            "File Name", "Fluor Name", "Exposure Time (ms)", "Background"
    };
    private List<FluorInfo> fluorInfos;
    private String outputFilename;
    private boolean use_exposure_time_ratio;
    private boolean do_normalization;
    private DefaultTableModel model;

    private final JPanel contentPanel = new JPanel();
    private MatrixDialog matrixDialog;
    private JTable matrixTable;
    private JButton saveButton;

    public UnmixingHelperDialog(List<FluorInfo> _fluorInfos) {
        fluorInfos = _fluorInfos;
        use_exposure_time_ratio = true;
        do_normalization = true;
        String commonName = StringUtil.getCommonSubstring(fluorInfos);
        if (commonName.length() < 3) {
            outputFilename = "matrix";
        } else {
            outputFilename = (commonName + "_matrix").replaceAll("_+", "_");
        }
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
            table.getModel().addTableModelListener(this);

            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                final JButton matrixButton = new JButton("Edit Matrix");
                matrixButton.setActionCommand("Edit Matrix");
                matrixButton.addActionListener(this);
                buttonPane.add(matrixButton);
                getRootPane().setDefaultButton(matrixButton);
            }
            {
                saveButton = new JButton("Save");
                saveButton.setActionCommand("Save");
                saveButton.setEnabled(false);
                saveButton.addActionListener(this);
                buttonPane.add(saveButton);
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

    private void writeMatrix(String outputFile) {
        List<String> buf = new ArrayList<>();

        // Write configuration
        buf.add("Channels\t" + fluorInfos.size());
        buf.add("Fluors\t" + fluorInfos.size());
        buf.add("MixingMatrixValid\tyes");
        buf.add("UnmixingMatrixValid\tyes\n");

        // Write Channel Name
        StringBuilder chName = new StringBuilder("ChannelNames");
        for (FluorInfo fi : fluorInfos) {
            chName.append("\t").append(fi.getFileName());
        }
        buf.add(chName.toString());

        // Write Fluor Name
        StringBuilder flName = new StringBuilder("FluorNames");
        for (FluorInfo fi : fluorInfos) {
            flName.append("\t").append(fi.getFluorName());
        }
        buf.add(flName + "\n");

        // Write Measurement Background
        for (int i = 0; i < fluorInfos.size(); i++) {
            buf.add("MeasurementBackground\t" + i + "\t" + fluorInfos.get(i).getBackGround());
        }
        buf.add("\n");

        // Make Matrix
        Matrix matrix = new Matrix(getTableData(matrixTable));

        // Write Mixing Matrix
        matrix = matrix.transpose();
        if (do_normalization) {
            normalize(matrix);
        }
        writeMatrixToBuffer(buf, matrix, "MixingMatrixFluor");

        // Write Unmixing Matrix
        Matrix inv_matrix = matrix.transpose().inverse();
        writeMatrixToBuffer(buf, inv_matrix, "UnmixingMatrixFluor");

        // Write buffer to file
        Path outfilePath = Paths.get(outputFile);
        if (Files.notExists(outfilePath, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectories(outfilePath.getParent());
                Files.createFile(outfilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.write(Paths.get(outputFile), buf, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMatrixToBuffer(List<String> buf, Matrix matrix, String title) {
        StringBuilder mixmatName;
        for (int i = 0; i < fluorInfos.size(); i++) {
            mixmatName = new StringBuilder(title + "\t" + i);
            for (int j = 0; j < fluorInfos.size(); j++) {
                mixmatName.append("\t").append(String.format("%.16f", matrix.get(i, j)));
            }
            buf.add(mixmatName.toString());
        }
        buf.add("");
    }

    /**
     * @param vector raw of 2D matrix
     * @return sum of raw
     */
    private double sum(double[] vector) {
        double sum = 0.0;
        for (double v : vector) {
            sum += v;
        }
        return sum;
    }

    private void normalize(Matrix matrix) {
        double[][] tmpArray = matrix.getArray();
        for (int i = 0; i < tmpArray.length; i++) {
            double sum = sum(tmpArray[i]);
            for (int j = 0; j < tmpArray[0].length; j++) {
                tmpArray[i][j] = tmpArray[i][j] / sum;
            }
        }
    }

    public double[][] getTableData(JTable table) {
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        double[][] tableData = new double[nRow][nCol];
        for (int i = 0; i < nRow; i++)
            for (int j = 0; j < nCol; j++)
                tableData[i][j] = (double) dtm.getValueAt(i, j);
        return tableData;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case "Save":
                SaveDialog sd = new SaveDialog("Save Matrix", outputFilename, ".txt");
                if (sd.getFileName() != null && sd.getDirectory() != null) {
                    System.out.println(sd.getDirectory() + sd.getFileName());
                    writeMatrix(sd.getDirectory() + sd.getFileName());
                }
                break;
            case "Cancel":
                if (matrixDialog.isVisible())
                    matrixDialog.dispose();
                matrixTable = null;
                saveButton.setEnabled(false);
                dispose();
                break;
            case "Edit Matrix":
                if (matrixTable == null) {
                    matrixDialog = new MatrixDialog(fluorInfos);
                    matrixTable = matrixDialog.getMatrixTable();
                    saveButton.setEnabled(true);
                }
                matrixDialog.setVisible(true);
                break;
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        DefaultTableModel model = (DefaultTableModel) e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
        // System.out.println("Changed: " + columnName + " : " + row + " to: " + data);
        if (columnName.equals(columns[1])) {                // "Fluor Name"
            fluorInfos.get(row).setFluorName((String)data);
        } else if (columnName.equals(columns[2])) {         // "Exposure Time (ms)"
            fluorInfos.get(row).setExposureTime((Double) data);
        } else if (columnName.equals(columns[3])) {         // "Background"
            fluorInfos.get(row).setBackGround((Double) data);
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

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
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
