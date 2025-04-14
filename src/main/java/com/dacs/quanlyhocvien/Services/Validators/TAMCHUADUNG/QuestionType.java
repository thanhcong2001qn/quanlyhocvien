package com.dacs.quanlyhocvien.Services.Validators.TAMCHUADUNG;

/**
 * Enum định nghĩa các loại câu hỏi có thể có trong hệ thống
 */
public enum QuestionType {
    USER,           // Câu hỏi về người dùng (học viên, giáo viên)
    COURSE,         // Câu hỏi về khóa học
    ASSIGNMENT,     // Câu hỏi về bài tập
    ACTION,         // Câu hỏi về hành động
    DATABASE_QUERY, // Câu hỏi cần truy vấn database
    GENERAL,        // Câu hỏi chung
    INVALID         // Câu hỏi không hợp lệ
}