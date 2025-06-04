package com.dacs.quanlyhocvien.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonOrderDTO {
    private Long lessonId;
    private Integer position;
}
