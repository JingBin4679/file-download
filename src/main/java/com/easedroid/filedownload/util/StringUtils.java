package com.easedroid.filedownload.util;

public class StringUtils {

    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static long tryParseLong(String longStr, long defaultVal) {
        try {
            long longVal = Long.parseLong(longStr);
            return longVal;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defaultVal;
    }


}
