package com.virtual_paddock.backend.infrastructure.helper.simulator;

import com.virtual_paddock.backend.api.dtos.raceresult.RaceResultBulkItemRequest;

import java.util.List;

public final class LapTimeHelper {

    private LapTimeHelper() {}

    public static String formatMillis(Long ms) {
        if (ms == null || ms <= 0 || ms == 2147483647L) return null;
        long totalSeconds = ms / 1000;
        long millis = ms % 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    /**
     * Convierte un string en segundos (ej: "105.3559" o "1849.4352") a formato legible mm:ss.SSS / hh:mm:ss.SSS.
     */
    public static String formatSecondsStringToLapTime(String secondsStr) {
        if (secondsStr == null || secondsStr.trim().isEmpty()) return null;
        String val = secondsStr.trim();
        if (val.contains(":")) return val;

        try {
            double totalSec = Double.parseDouble(val);
            if (totalSec <= 0 || totalSec >= 2147483647L) return null;

            long totalSeconds = (long) totalSec;
            long millis = Math.round((totalSec - totalSeconds) * 1000.0);
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            if (hours > 0) {
                return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis);
            } else {
                return String.format("%02d:%02d.%03d", minutes, seconds, millis);
            }
        } catch (Exception ignored) {
            return val;
        }
    }

    public static Long parseTimeToMillis(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 3) {
                long hours = Long.parseLong(parts[0]);
                long min = Long.parseLong(parts[1]);
                double sec = Double.parseDouble(parts[2]);
                return (hours * 3600000L) + (min * 60000L) + (long) (sec * 1000.0);
            } else if (parts.length == 2) {
                long min = Long.parseLong(parts[0]);
                double sec = Double.parseDouble(parts[1]);
                return (min * 60000L) + (long) (sec * 1000.0);
            } else if (parts.length == 1) {
                double sec = Double.parseDouble(parts[0]);
                return (long) (sec * 1000.0);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static void resolveFastestLap(List<RaceResultBulkItemRequest> items) {
        if (items == null || items.isEmpty()) return;
        RaceResultBulkItemRequest fastest = null;
        Long minLapMs = Long.MAX_VALUE;

        for (RaceResultBulkItemRequest item : items) {
            Long ms = parseTimeToMillis(item.getBestLapTime());
            if (ms != null && ms < minLapMs && ms > 0) {
                minLapMs = ms;
                fastest = item;
            }
        }

        if (fastest != null) {
            fastest.setFastestLap(true);
        }
    }
}
