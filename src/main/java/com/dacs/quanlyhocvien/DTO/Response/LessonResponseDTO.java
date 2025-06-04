package com.dacs.quanlyhocvien.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponseDTO {
    private Long lessonId;
    private String title;
    private String description;
    private Integer duration;
    private Integer position;
    private Boolean isFree;
}