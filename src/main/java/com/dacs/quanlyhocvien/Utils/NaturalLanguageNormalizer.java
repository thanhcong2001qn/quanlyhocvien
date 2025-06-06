package com.dacs.quanlyhocvien.Utils;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dùng để chuẩn hóa các từ rút gọn, từ viết tắt, lỗi chính tả phổ biến
 * thành dạng chuẩn hóa hơn (vd: sv -> sinh viên, id -> mã, lop -> lớp)
 */
public class NaturalLanguageNormalizer {

    private static final Map<String, String> NORMALIZATION_MAP = NormalizationLoader.load();

    /**
     * Chuẩn hóa chuỗi nhập từ người dùng trước khi gửi đi AI hoặc ánh xạ.
     * @param input câu hỏi gốc của người dùng
     * @return câu hỏi đã được thay thế từ viết tắt → đầy đủ
     */
    public static String normalize(String input) {
        if (input == null || input.isBlank()) return input;

        String normalized = input.toLowerCase();
        for (Map.Entry<String, String> entry : NORMALIZATION_MAP.entrySet()) {
            String regex = "\\b" + Pattern.quote(entry.getKey()) + "\\b";
            normalized = normalized.replaceAll(regex, entry.getValue());
        }

        return normalized;
    }
}
