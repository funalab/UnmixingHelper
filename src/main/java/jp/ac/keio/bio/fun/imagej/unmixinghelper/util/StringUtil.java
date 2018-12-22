package jp.ac.keio.bio.fun.imagej.unmixinghelper.util;

import jp.ac.keio.bio.fun.imagej.unmixinghelper.FluorInfo;

import java.util.List;

/**
 * @author Akira Funahashi
 * @author Yuta Tokuoka
 * <p>
 * Yuta Tokuoka implemented a python code which generates matrix data.
 * Akira Funahashi ported the python code to Java, and made it as an ImageJ plugin.
 * </p>
 */
public class StringUtil {
    public static String longestCommonSubstrings(String s, String t) {
        int[][] table = new int[s.length()][t.length()];
        int longest = 0;
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j < t.length(); j++) {
                if (s.charAt(i) != t.charAt(j)) {
                    continue;
                }

                table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
                if (table[i][j] > longest) {
                    longest = table[i][j];
                    result = "";
                }
                if (table[i][j] == longest) {
                    result = s.substring(i - longest + 1, i + 1);
                }
            }
        }
        return result;
    }

    public static String getCommonSubstring(List<FluorInfo> fluorInfos) {
        String common = null;
        for (int i = 0; i < fluorInfos.size() - 1; i++) {
            for (int j = i + 1; j < fluorInfos.size(); j++) {
                String s;
                if (common == null) {
                    s = fluorInfos.get(i).getFileName();
                    if (s.contains(".")) {
                        s = s.substring(0, s.lastIndexOf('.'));
                    }
                    s = s.replaceAll("_[0-9]+ms$", "");
                } else {
                    s = common;
                }
                String t = fluorInfos.get(j).getFileName();
                common = StringUtil.longestCommonSubstrings(s, t);
                if (common.length() == 0) {
                    return common;
                }
            }
        }
        return common;
    }
}
