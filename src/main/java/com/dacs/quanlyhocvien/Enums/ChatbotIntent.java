package com.dacs.quanlyhocvien.Enums;

public enum ChatbotIntent {
    GENERAL_KNOWLEDGE,
    DATABASE_QUERY,
    UNSAFE_COMMAND,
    ENGLISH,            // 🆕 tiếng Anh không được hỗ trợ
    GIBBERISH,          // 🆕 chuỗi không có nghĩa
    UNKNOWN             // fallback
}
