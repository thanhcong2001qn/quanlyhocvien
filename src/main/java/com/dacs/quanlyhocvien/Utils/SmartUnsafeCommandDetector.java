package com.dacs.quanlyhocvien.Utils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SmartUnsafeCommandDetector {

    // ✅ Danh sách từ khóa thô
    private static final List<String> RAW_KEYWORDS = List.of(
            "xoa", "xóa", "delete", "drop", "truncate", "alter", "update", "thay doi", "cap nhat", "sua", "ghi de", "over write", "truncate", "remove"
    );

    // ✅ Các mẫu regex phức tạp bao phủ tình huống nguy hiểm
    private static final List<Pattern> DANGEROUS_PATTERNS = List.of(
            Pattern.compile("x[oó]a.*tat ca", Pattern.CASE_INSENSITIVE),
            Pattern.compile("x[oó]a.*(h[ọo]c v[ií]en|gi[aả]ng vi[êe]n|kh[oó]a h[oọ]c).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("delete\\s+from\\s+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("drop\\s+(table|database)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("truncate\\s+table\\s+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("update\\s+.*\\s+set\\s+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("thay\\s*doi|cap\\s*nhat|sua|ghi\\s*de|overwrite", Pattern.CASE_INSENSITIVE)
    );

    // ✅ Chuẩn hoá văn bản
    private static String normalize(String input) {
        if (input == null) return "";
        String noDiacritics = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noDiacritics.toLowerCase(Locale.ROOT).trim();
    }

    // ✅ Hàm kiểm tra chính
    public static boolean isUnsafe(String input) {
        String normalized = normalize(input);
        return RAW_KEYWORDS.stream().anyMatch(normalized::contains)
                || DANGEROUS_PATTERNS.stream().anyMatch(p -> p.matcher(normalized).find());
    }
}
