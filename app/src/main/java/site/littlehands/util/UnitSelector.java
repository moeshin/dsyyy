package site.littlehands.util;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class UnitSelector {

    private static final String FORMAT = "%.2f %s";

    private static final String[] BIT_RATE_UNITS = new String[]{
            "bps", "kbps", "Mbps", "Gbps", "Tbps"
    };

    private static final String[] BYTE_UNITS = new String[]{
            "B", "KB", "MB", "GB", "TB"
    };


    public static String bitRate(double bitRate) {
        int i = 0;

        while (bitRate >= 1000) {
            bitRate /= 1000;
            i++;
        }

        return String.format(FORMAT, bitRate, BIT_RATE_UNITS[i]);
    }

    public static String bitRate(double bitRate, int index) {

        for (int i = 0; i < index; i++) {
            bitRate /= 1000;
        }

        return String.format(FORMAT, bitRate, BIT_RATE_UNITS[index]);
    }

    public static String Byte(double Byte) {
        int i = 0;

        while (Byte >= 1024) {
            Byte /= 1024;
            i++;
        }

        return String.format(FORMAT, Byte, BYTE_UNITS[i]);
    }

    public static String Byte(double Byte, int index) {

        for (int i = 0; i < index; i++) {
            Byte /= 1024;
        }

        return String.format(FORMAT, Byte, BYTE_UNITS[index]);
    }

}
