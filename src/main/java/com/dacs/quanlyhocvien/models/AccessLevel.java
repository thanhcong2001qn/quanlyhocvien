package com.dacs.quanlyhocvien.models;

public enum AccessLevel {
    FULL_ACCESS("Full Access"),
    RESTRICTED_ACCESS("Restricted Access"),
    LIMITED_ACCESS("Limited Access");

    private final String displayName;

    AccessLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}