package com.dacs.quanlyhocvien.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private int enrolledCoursesCount;
    private int completedLessonsCount;
    private double totalLearningHours;
    private int achievementPoints;
}