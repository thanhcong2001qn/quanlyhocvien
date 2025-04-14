package com.dacs.quanlyhocvien.Services.Processors.TAMCHUADUNG;

public enum Intent {
    COUNT_TEACHER,
    COUNT_STUDENT,
    COUNT_COURSE,
    LIST_TEACHER,
    LIST_STUDENT,
    LIST_COURSE,
    SEARCH_TEACHER,
    SEARCH_STUDENT,
    SEARCH_COURSE,
    DETAIL_TEACHER,
    DETAIL_STUDENT,
    DETAIL_COURSE,
    UNKNOWN;

    public static Intent fromString(String text) {
        try {
            return valueOf(text.toUpperCase());
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}