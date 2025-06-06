package com.dacs.quanlyhocvien.Exceptions;

public class ChatbotException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;
    private final String technicalMessage;

    public ChatbotException(String errorCode, String userMessage, String technicalMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getTechnicalMessage() {
        return technicalMessage;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", errorCode, userMessage, technicalMessage);
    }
}
