package com.auca.main;


public final class TextUtils {

    private TextUtils() {
    }

    public static String two(int number) {
        return String.format("%02d", number);
    }

    public static String yesNo(boolean value) {
        if (value) {
            return "YES";
        }
        return "NO";
    }

    public static String[] copyOf(String[] source) {
        String[] result = new String[source.length];

        for (int i = 0; i < source.length; i++) {
            result[i] = source[i];
        }

        return result;
    }

    public static String[] merge(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];

        for (int i = 0; i < first.length; i++) {
            result[i] = first[i];
        }

        for (int i = 0; i < second.length; i++) {
            result[first.length + i] = second[i];
        }

        return result;
    }

    public static String shortLabel(String label) {
        int position = label.indexOf(" [");

        if (position >= 0) {
            return label.substring(0, position);
        }

        return label;
    }

    public static String boxLine(String text) {
        return String.format("| %-58s |", text);
    }
}
