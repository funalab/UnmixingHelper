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
    private int num_channel;
    private List<FluorInfo> fluorInfos;

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
        fluorInfos = new ArrayList<>();
    }

    @Override
    public void run() {
        num_channel = datasetService.getDatasets().size();

        for (Dataset ds : datasetService.getDatasets()) {
            String imgName = ds.getImgPlus().getName();
            FluorInfo fi = new FluorInfo(imgName);
            fluorInfos.add(fi);
            // System.out.println(fi);
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

    @Override
    public void cancel() {
        log.info("WidgetDemo: canceled");
    }

    @Override
    public void preview() {
        log.info("WidgetDemo: previews and counting");
        statusService.showStatus("Updated");
    }

    public int getNumChannel() {
        return num_channel;
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
        String[] testfile0 = {"CFP_30ms.tif", "GFP_50ms.tif",
                "Orange_30ms.tif", "Cherry_30ms.tif",
                "Keima_10ms.tif", "YFP_50ms.tif",
        };
        String[] testfile = {"06GRCOC_CFP_5ms.tif", "06GRCOC_GFP_5ms.tif",
                "06GRCOC_Orange_5ms.tif", "06GRCOC_Cherry_5ms.tif",
                "06GRCOC_Keima_2.5ms.tif", "06GRCOC_YFP_5ms.tif",
        };

        ArrayList<File> fl = new ArrayList<>();
        for (String s : testfile) {
            File f = new File(s);
            fl.add(f);
        }

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
