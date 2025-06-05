package com.dacs.quanlyhocvien.Services;


import com.dacs.quanlyhocvien.DTO.Response.UserStatsDTO;
import com.dacs.quanlyhocvien.Repository.IEnrollmentRepository;
import com.dacs.quanlyhocvien.Repository.IProgressRepository;
import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserStatsService {

    @Autowired
    private IEnrollmentRepository enrollmentRepository;

    @Autowired
    private IProgressRepository progressRepository;

    @Autowired
    private IStudentRepository studentRepository;

    /**
     * Lấy thống kê học tập của user
     */
    public UserStatsDTO getUserStats(String username) {
        UserStatsDTO stats = new UserStatsDTO();

        // Tìm student_id từ username
        StudentModel student = studentRepository.findByAccount_Username(username);

        Long studentId = student.getStudentId();

        // 1. Đếm số khóa học đã đăng ký
        int enrolledCoursesCount = enrollmentRepository.countByStudentId(studentId);
        stats.setEnrolledCoursesCount(enrolledCoursesCount);

        // 2. Đếm số bài học đã hoàn thành
        int completedLessonsCount = progressRepository.countCompletedLessonsByStudentId(studentId);
        stats.setCompletedLessonsCount(completedLessonsCount);

        // 3. Tính tổng thời gian học tập (giờ)
        int totalMinutes = progressRepository.calculateTotalLearningMinutesByStudentId(studentId);
        double totalHours = totalMinutes / 60.0;
        stats.setTotalLearningHours(totalHours);

        // 4. Tính điểm thành tích (mỗi bài học hoàn thành = 10 điểm)
        int achievementPoints = completedLessonsCount * 10;
        stats.setAchievementPoints(achievementPoints);


        return stats;
    }
}