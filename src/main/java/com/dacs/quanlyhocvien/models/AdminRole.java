package com.dacs.quanlyhocvien.models;

public enum AdminRole {
    SUPER_ADMIN("Super Admin"),
    COURSE_ADMIN("Course Admin"),
    USER_ADMIN("User Admin"),
    CONTENT_ADMIN("Content Admin");

    private final String displayName;

    AdminRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}