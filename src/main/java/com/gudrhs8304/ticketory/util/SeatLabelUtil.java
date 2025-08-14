package com.gudrhs8304.ticketory.util;

public final class SeatLabelUtil {
    private SeatLabelUtil() {}

    public static String toLabel(String rowLabel, Integer colNumber) {
        if (rowLabel == null || colNumber == null) return null;
        return rowLabel + colNumber;
    }
}
