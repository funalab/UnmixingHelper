package jp.ac.keio.bio.fun.imagej.unmixinghelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FluorInfo {
    private String fluorName;
    private String fileName;
    private double backGround;
    private double exposureTime;

    public FluorInfo(String imgName) {
        super();
        fileName = imgName;
        fluorName = parseFluorName(imgName);
        exposureTime = parseExposureTime(imgName);
    }

    private String parseFluorName(String filename) {
        String regex;
        if (hasExposureTime(filename)) {
            regex = "(.+_(.+)|(.+))_(\\d+\\.*\\d*)ms.+";
        } else {
            regex = "(.+_(.+)|(.+))\\..+$";
        }
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(filename);
        if (m.find()) {
            if (m.group(2) != null) {
                return m.group(2);
            }
            return m.group(3);
        }
        return filename;
    }

    private boolean hasExposureTime(String filename) {
        return filename.matches(".*_(\\d+\\.*\\d*)ms.+");
    }

    private double parseExposureTime(String filename) {
        String regex = ".*_(\\d+\\.*\\d*)ms.*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(filename);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return 0d;
    }

    public String getFluorName() {
        return fluorName;
    }

    public String getFileName() {
        return fileName;
    }

    public double getBackGround() {
        return backGround;
    }

    public void setBackGround(double backGround) {
        this.backGround = backGround;
    }

    public double getExposureTime() {
        return exposureTime;
    }

    @Override
    public String toString() {
        return "FluorInfo{" +
                "fluorName='" + fluorName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", backGround=" + backGround +
                ", exposureTime=" + exposureTime +
                '}';
    }

    public static void main(final String[] args) {
        String[] tests = {
                "0123_CFP_30ms.tif",
                "GFP_50ms.tif",
                "Orange.tif",
                "2345_Cherry.tif",
                "Keima_10ms.tif",
                "YFP_50ms.tif"};
        String[] ans = {"CFP", "GFP", "Orange", "Cherry", "Keima", "YFP"};

        int i = 0;
        for (String s : tests) {
            FluorInfo fi = new FluorInfo(s);
            boolean ok = fi.getFluorName().equals(ans[i]);
            System.out.println(ok + ": " + fi);
            i++;
        }
    }
}
