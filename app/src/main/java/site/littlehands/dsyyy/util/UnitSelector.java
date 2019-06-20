package site.littlehands.dsyyy.util;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class UnitSelector {

    private static final String FORMAT = "%.0f %s";

    private static final String[] BIT_RATE_UNITS = new String[]{
            "bps", "kbps", "Mbps", "Gbps", "Tbps"
    };

    private static final String[] BYTE_UNITS = new String[]{
            "B", "KB", "MB", "GB", "TB"
    };


    public static String br(double br) {
        int i = 0;

        while (br >= 1000) {
            br /= 1000;
            i++;
        }

        return String.format(FORMAT, br, BIT_RATE_UNITS[i]);
    }

    public static String br(double br, int index) {

        for (int i = 0; i < index; i++) {
            br /= 1000;
        }

        return String.format(FORMAT, br, BIT_RATE_UNITS[index]);
    }

    public static String size(double Byte) {
        int i = 0;

        while (Byte >= 1024) {
            Byte /= 1024;
            i++;
        }

        return String.format(FORMAT, Byte, BYTE_UNITS[i]);
    }

    public static String size(double Byte, int index) {

        for (int i = 0; i < index; i++) {
            Byte /= 1024;
        }

        return String.format(FORMAT, Byte, BYTE_UNITS[index]);
    }

}
