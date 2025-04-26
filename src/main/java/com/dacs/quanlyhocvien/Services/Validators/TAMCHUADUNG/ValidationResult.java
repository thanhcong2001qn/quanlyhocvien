package com.dacs.quanlyhocvien.Services.Validators.TAMCHUADUNG;

/**
 * Class chứa kết quả kiểm tra tính hợp lệ của câu hỏi
 */
public class ValidationResult {
    private final boolean isValid;
    private final String message;
    private final QuestionType type;

    public ValidationResult(boolean isValid, String message, QuestionType type) {
        this.isValid = isValid;
        this.message = message;
        this.type = type;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getMessage() {
        return message;
    }

    public QuestionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("ValidationResult{isValid=%s, type=%s, message='%s'}",
                isValid, type, message);
    }
}