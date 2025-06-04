package com.dacs.quanlyhocvien.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponseDTO {
    private Long moduleId;
    private Long courseId;
    private Integer orderIndex;
    private String title;
    private String description;
    private Integer position;
    private Boolean isFree;
    private Integer totalLessons;
    private Integer totalDuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LessonResponseDTO> lessons = new ArrayList<>();
}