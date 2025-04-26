package com.dacs.quanlyhocvien.Services.Processors.TAMCHUADUNG;

public class IntentAnalysisResult {
    private final Intent intent;
    private final boolean success;

    public IntentAnalysisResult(Intent intent, boolean success) {
        this.intent = intent;
        this.success = success;
    }

    public Intent getIntent() { return intent; }
    public boolean isSuccess() { return success; }
}