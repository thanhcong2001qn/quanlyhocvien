package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.Response.LessonResponseDTO;
import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.Repository.ILessonRepository;
import com.dacs.quanlyhocvien.Repository.IModuleRepository;
import com.dacs.quanlyhocvien.exceptions.ResourceNotFoundException;
import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.LessonModel;
import com.dacs.quanlyhocvien.models.ModuleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {
    private final ILessonRepository lessonRepository;
    private final ICourseRepository courseRepository;

    @Autowired
    public LessonService(ILessonRepository lessonRepository,
                         IModuleRepository moduleRepository,
                         ICourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }
    @Transactional(readOnly = true)
    public List<LessonResponseDTO> getLessonsByCourseId(Long courseId) {
        // Kiểm tra khóa học tồn tại
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Khóa học", "ID", courseId));

        // Lấy tất cả module của khóa học và sắp xếp theo vị trí
        List<ModuleModel> modules = courseRepository.findModulesByCourseId(courseId).stream()
                .sorted(Comparator.comparing(ModuleModel::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        List<LessonResponseDTO> result = new ArrayList<>();

        // Duyệt qua từng module để lấy bài học
        for (ModuleModel module : modules) {
            // Lấy tất cả bài học của module và sắp xếp theo vị trí
            List<LessonModel> lessons = lessonRepository.findByModuleId(module.getModuleId()).stream()
                    .sorted(Comparator.comparing(LessonModel::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            // Chuyển đổi từ entity sang DTO
            for (LessonModel lesson : lessons) {
                LessonResponseDTO dto = new LessonResponseDTO();
                dto.setLessonId(lesson.getLessonId());
                dto.setModuleId(module.getModuleId());
                dto.setTitle(lesson.getTitle());
                dto.setDescription(lesson.getDescription());
                dto.setDuration(lesson.getDuration());
                dto.setPosition(lesson.getPosition());
                dto.setIsFree(lesson.getIsFree());
                dto.setCreatedAt(lesson.getCreatedAt());
                dto.setUpdatedAt(lesson.getUpdatedAt());

                // Lấy thông tin video cho bài học
                VideoModel video = iVideoRepository.findByLesson_LessonId(lesson.getLessonId());
                if (video != null) {
                    dto.setVideoUrl(video.getVideoUrl());
                    dto.setVideoTitle(video.getTitle());
                    dto.setVideoDuration(video.getDuration());
                    dto.setVideoThumbnailUrl(video.getThumbnailPath());
                    dto.setIsVideoDownloadable(video.getIsDownloadable());
                }

                result.add(dto);
            }
        }

        return result;
    }
}
