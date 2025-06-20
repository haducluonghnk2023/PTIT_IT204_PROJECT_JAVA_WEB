package com.data.ra.entity.admin;

public enum Progress {
    pending,
    handling,
    interviewing,
    done,
    rejected;
    public static Progress fromString(String value) {
        for (Progress progress : Progress.values()) {
            if (progress.name().equalsIgnoreCase(value)) {
                return progress;
            }
        }
        throw new IllegalArgumentException("Unknown progress: " + value);
    }
}