/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import Jama.Matrix;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.RealType;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.Previewable;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * @author Akira Funahashi
 * @author Yuta Tokuoka
 * <p>
 * Yuta Tokuoka implemented a python code which generates matrix data.
 * Akira Funahashi ported the python code to Java, and made it as an ImageJ plugin.
 * </p>
 */
@Plugin(type = Command.class, menuPath = "Plugins>Unmixing Helper")
public class UnmixingHelper<T extends RealType<T>> implements Command, Previewable {
    private Properties properties;
    private int num_channel;
    private boolean use_exposure_time_ratio;
    private boolean do_normalization;
    private String outputFile;
    private Matrix matrix;
    private List<FluorInfo> fluorInfos;
    private List<List<String>> chList;

    //@Parameter
    //private Dataset currentData;

    @Parameter
    private UIService uiService;

    @Parameter
    private OpService opService;

    @Parameter
    private DatasetService datasetService;

    @Parameter
    private LogService log;

    @Parameter
    private StatusService statusService;

    private static UnmixingHelperDialog dialog = null;
    // GUI widgets
    /*
    @Parameter(label = "double")
    private double pDouble;

    @Parameter(label = "int")
    private int pInt;

    @Parameter(label = "Results", type = ItemIO.OUTPUT)
    private String result;
    */
    // GUI widgets

    public UnmixingHelper() {
        fluorInfos = new ArrayList<FluorInfo>();
        properties = new Properties();
        chList = new ArrayList<>();
    }

    @Override
    public void run() {
        num_channel = datasetService.getDatasets().size();
        use_exposure_time_ratio = true;
        do_normalization = true;

        for (Dataset ds : datasetService.getDatasets()) {
            String imgName = ds.getImgPlus().getName();
            FluorInfo fi = new FluorInfo(imgName);
            fluorInfos.add(fi);
            System.out.println(fi);
        }

        fluorInfos.sort(Comparator.comparing(FluorInfo::getFluorName));
        fluorInfos.forEach(System.out::println);

        SwingUtilities.invokeLater(() -> {
            if (dialog == null) {
                dialog = new UnmixingHelperDialog(fluorInfos);
            }
            dialog.setVisible(true);

            dialog.setOpService(opService);
            dialog.setLog(log);
            dialog.setStatusService(statusService);
            dialog.setUiService(uiService);
        });
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

    /**
     *
     */
    private void normalize() {
        double[][] tmpArray = matrix.getArray();
        for (int i = 0; i < tmpArray.length; i++) {
            double sum = sum(tmpArray[i]);
            for (int j = 0; j < tmpArray[0].length; j++) {
                tmpArray[i][j] = tmpArray[i][j] / sum;
            }
        }
    }

    /**
     * @param propertiesPath path to .properties file
     */
    private void makeMatrix(String propertiesPath) {
        writeConfig();
    }

    /**
     *
     */
    private void writeConfig() {
        List<String> buf = new ArrayList<>();

        // Write configuration
        buf.add("Channels\t" + num_channel);
        buf.add("Fluors\t" + num_channel);
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
        double[][] tmpMatrix = new double[num_channel][num_channel];
        for (int i = 0; i < num_channel; i++) {
            for (int j = 0; j < num_channel; j++) {
                double val = Double.parseDouble(chList.get(i).get(j));
                double weight = 1.0;
                if (use_exposure_time_ratio) {
                    weight = fluorInfos.get(i).getExposureTime() / fluorInfos.get(j).getExposureTime();
                    //weight = (double)(etList.get(i)) / etList.get(j);
                }
                tmpMatrix[i][j] = val * weight;
            }
        }
        matrix = new Matrix(tmpMatrix);
        // Write Mixing Matrix
        // matrix.print(6, 6);
        matrix = matrix.transpose();
        // matrix.print(6, 6);
        if (do_normalization) {
            normalize();
        }
        // matrix.print(6, 6);
        StringBuilder mixmatName;
        for (int i = 0; i < num_channel; i++) {
            mixmatName = new StringBuilder("MixingMatrixFluor\t" + i);
            for (int j = 0; j < num_channel; j++) {
                mixmatName.append("\t").append(String.format("%.16f", matrix.get(i, j)));
            }
            buf.add(mixmatName.toString());
        }
        buf.add("");

        // Write Unmixing Matrix
        Matrix inv_matrix = matrix.transpose().inverse();
        StringBuilder unmixName;
        for (int i = 0; i < num_channel; i++) {
            unmixName = new StringBuilder("UnmixingMatrixFluor\t" + i);
            for (int j = 0; j < num_channel; j++) {
                unmixName.append("\t").append(String.format("%.16f", inv_matrix.get(i, j)));
            }
            buf.add(unmixName.toString());
        }
        buf.add("\n");

        // Write buffer to file
        Path outfilePath = Paths.get(properties.getProperty("output"));
        if (Files.notExists(outfilePath, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectories(outfilePath.getParent());
                Files.createFile(outfilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.write(Paths.get(properties.getProperty("output")), buf, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        log.info("WidgetDemo: canceled");
    }

    @Override
    public void preview() {
        log.info("WidgetDemo: previews and counting");
        statusService.showStatus("Updated");
    }

    private void append(final StringBuilder sb, final String s) {
        sb.append(s + "\n");
    }

    public int getNumChannel() {
        return num_channel;
    }

    public boolean isUseExposureTimeRatio() {
        return use_exposure_time_ratio;
    }

    public boolean isDoNormalization() {
        return do_normalization;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public UIService getUiService() {
        return uiService;
    }

    public OpService getOpService() {
        return opService;
    }

    public DatasetService getDatasetService() {
        return datasetService;
    }

    public LogService getLog() {
        return log;
    }

    public StatusService getStatusService() {
        return statusService;
    }

    public static UnmixingHelperDialog getDialog() {
        return dialog;
    }

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        // ask the user for a file to open
        //final File file = ij.ui().chooseFile(null, "open");

        ArrayList<File> fl = new ArrayList<>();
        final File file1 = new File("CFP_30ms.tif");
        final File file2 = new File("GFP_50ms.tif");
        final File file3 = new File("Orange_30ms.tif");
        final File file4 = new File("Cherry_30ms.tif");
        final File file5 = new File("Keima_10ms.tif");
        final File file6 = new File("YFP_50ms.tif");
        fl.add(file1);
        fl.add(file2);
        fl.add(file3);
        fl.add(file4);
        fl.add(file5);
        fl.add(file6);

        for (File f : fl) {
            // load the dataset
            Dataset ds = ij.scifio().datasetIO().open(f.getPath());
            // show the image
            ij.ui().show(ds);
        }
        // invoke the plugin
        ij.command().run(UnmixingHelper.class, true);

    }
}
