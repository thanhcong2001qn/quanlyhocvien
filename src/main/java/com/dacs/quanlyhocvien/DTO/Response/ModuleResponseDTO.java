package com.dacs.quanlyhocvien.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponseDTO {
    private Long moduleId;
    private String title;
    private String description;
    private Integer position;
    private Boolean isFree;
    private Integer totalLessons;
    private Integer totalDuration; // tổng thời lượng của module
    private List<LessonResponseDTO> lessons = new ArrayList<>();
}