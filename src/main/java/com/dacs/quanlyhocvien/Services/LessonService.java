package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.LessonOrderDTO;
import com.dacs.quanlyhocvien.DTO.Response.LessonResponseDTO;
import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.Repository.ILessonRepository;
import com.dacs.quanlyhocvien.Repository.IModuleRepository;
import com.dacs.quanlyhocvien.Repository.IVideoRepository;
import com.dacs.quanlyhocvien.exceptions.ResourceNotFoundException;
import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.LessonModel;
import com.dacs.quanlyhocvien.models.ModuleModel;
import com.dacs.quanlyhocvien.models.VideoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {
    private final ILessonRepository lessonRepository;
    private final ICourseRepository courseRepository;
    private final IModuleRepository iModuleRepository;
    private final ILessonRepository iLessonRepository;
    private final FileStorageService fileStorageService;
    private final IVideoRepository iVideoRepository;

    @Autowired
    public LessonService(ILessonRepository lessonRepository,
                         IModuleRepository moduleRepository,
                         ICourseRepository courseRepository, IModuleRepository iModuleRepository, ILessonRepository iLessonRepository, FileStorageService fileStorageService, IVideoRepository iVideoRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.iModuleRepository = iModuleRepository;
        this.iLessonRepository = iLessonRepository;
        this.fileStorageService = fileStorageService;
        this.iVideoRepository = iVideoRepository;
    }
    public List<LessonResponseDTO> getLessonsByModule(Long moduleId) {
        List<LessonModel> lessons = lessonRepository.findByModule_ModuleIdOrderByPosition(moduleId);

        return lessons.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
                dto.setTitle(lesson.getTitle());
                dto.setDescription(lesson.getDescription());
                dto.setDuration(lesson.getDuration());
                dto.setPosition(lesson.getPosition());
                dto.setIsFree(lesson.getIsFree());

                result.add(dto);
            }
        }

        return result;
    }
    @Transactional
    public LessonResponseDTO createLesson(LessonResponseDTO lessonDTO) {

        ModuleModel module = iModuleRepository.findById(lessonDTO.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy module với ID: " + lessonDTO.getModuleId()));

        // If position is not specified, set it to the next available position
        if (lessonDTO.getPosition() == null) {
            Integer maxPosition = lessonRepository.findMaxPositionByModuleId(lessonDTO.getModuleId());
            // Xử lý trường hợp không có lesson nào trong module
            lessonDTO.setPosition(maxPosition != null ? maxPosition + 1 : 1);
        }

        // Create lesson
        LessonModel lesson = new LessonModel();
        lesson.setModule(iModuleRepository.findByModuleId(lessonDTO.getModuleId()));
        lesson.setTitle(lessonDTO.getTitle());
        lesson.setDescription(lessonDTO.getDescription());
        lesson.setDuration(lessonDTO.getDuration());
        lesson.setPosition(lessonDTO.getPosition());
        lesson.setIsFree(lessonDTO.getIsFree() != null ? lessonDTO.getIsFree() : false);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        // Save lesson first to get the ID
        LessonModel savedLesson = lessonRepository.save(lesson);
        // Process video if present
        if (lessonDTO.getVideoUrl() != null && !lessonDTO.getVideoUrl().isEmpty()) {
            VideoModel video = new VideoModel();
            video.setLesson(lessonRepository.findByLessonId(savedLesson.getLessonId()));
            video.setTitle(lessonDTO.getVideoTitle() != null ? lessonDTO.getVideoTitle() : lessonDTO.getTitle());
            video.setVideoUrl(lessonDTO.getVideoUrl());
            video.setDuration(lessonDTO.getVideoDuration());
            video.setIsDownloadable(lessonDTO.getIsVideoDownloadable() != null ? lessonDTO.getIsVideoDownloadable() : false);

            // Process thumbnail if provided
            if (lessonDTO.getVideoThumbnailFile() != null) {
                String thumbnailPath = fileStorageService.storeFile(
                        lessonDTO.getVideoThumbnailFile(),
                        "thumbnails/video/" + savedLesson.getLessonId()
                );
                video.setThumbnailPath(thumbnailPath);
            } else if (lessonDTO.getVideoThumbnailUrl() != null) {
                video.setThumbnailPath(lessonDTO.getVideoThumbnailUrl());
            }

            video.setCreatedAt(LocalDateTime.now());
            video.setUpdatedAt(LocalDateTime.now());

            VideoModel savedVideo = iVideoRepository.save(video);
            // Update lesson duration from video if not provided
            if (lesson.getDuration() == null && video.getDuration() != null) {
                // Convert seconds to minutes for lesson duration
                int durationMinutes = (int) Math.ceil(video.getDuration() / 60.0);
                lesson.setDuration(durationMinutes);
                lessonRepository.save(lesson);
            }
        }

        // Update module updatedAt
        module.setUpdatedAt(LocalDateTime.now());
        iModuleRepository.save(module);
        return mapToDTO(savedLesson);
    }
//
//    /**
//     * Update a lesson
//     *
//     * @param lessonDTO Lesson data with updates
//     * @return Updated lesson
//     * @throws ResourceNotFoundException if lesson not found
//     */
//    @Transactional
//    public LessonDTO updateLesson(LessonDTO lessonDTO) {
//        logger.info("Updating lesson with id: {}", lessonDTO.getLessonId());
//
//        LessonModel lesson = lessonRepository.findById(lessonDTO.getLessonId())
//                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonDTO.getLessonId()));
//
//        lesson.setTitle(lessonDTO.getTitle());
//        lesson.setDescription(lessonDTO.getDescription());
//
//        if (lessonDTO.getDuration() != null) {
//            lesson.setDuration(lessonDTO.getDuration());
//        }
//
//        if (lessonDTO.getPosition() != null) {
//            lesson.setPosition(lessonDTO.getPosition());
//        }
//
//        if (lessonDTO.getIsFree() != null) {
//            lesson.setIsFree(lessonDTO.getIsFree());
//        }
//
//        lesson.setUpdatedAt(LocalDateTime.now());
//
//        LessonModel updatedLesson = lessonRepository.save(lesson);
//
//        // Update module updatedAt
//        ModuleModel module = moduleRepository.findById(lesson.getModuleId())
//                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy module với ID: " + lesson.getModuleId()));
//        module.setUpdatedAt(LocalDateTime.now());
//        moduleRepository.save(module);
//
//        // Update course duration
//        updateCourseDuration(module.getCourseId());
//
//        logger.info("Lesson updated successfully with id: {}", updatedLesson.getLessonId());
//
//        return mapToDTO(updatedLesson);
//    }

    /**
     * Delete a lesson
     *
     * @param lessonId Lesson ID
     * @throws ResourceNotFoundException if lesson not found
     */
    @Transactional
    public void deleteLesson(Long lessonId) {
        LessonModel lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // Get module for later updates
        ModuleModel module = iModuleRepository.findById(lesson.getModule().getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy module với ID: " + lesson.getModule().getModuleId()));
        Long courseId = module.getCourse().getCourseId();

        // Delete the lesson
        lessonRepository.delete(lesson);

        // Update module updatedAt
        module.setUpdatedAt(LocalDateTime.now());
        iModuleRepository.save(module);

    }
    @Transactional
    public void updateLessonOrder(Long moduleId, List<LessonOrderDTO> orderUpdates) {// Verify that the module exists
        iModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy module với ID: " + moduleId));

        // Update each lesson's order
        for (LessonOrderDTO update : orderUpdates) {
            iLessonRepository.updateLessonPosition(update.getLessonId(), update.getPosition());
        }

        // Update module updatedAt
        ModuleModel module = iModuleRepository.getById(moduleId);
        module.setUpdatedAt(LocalDateTime.now());
        iModuleRepository.save(module);
    }
    private LessonResponseDTO mapToDTO(LessonModel lesson) {
        LessonResponseDTO dto = new LessonResponseDTO();

        dto.setLessonId(lesson.getLessonId());
        dto.setModuleId(lesson.getModule().getModuleId());
        dto.setTitle(lesson.getTitle());
        dto.setDescription(lesson.getDescription());
        dto.setDuration(lesson.getDuration());
        dto.setPosition(lesson.getPosition());
        dto.setIsFree(lesson.getIsFree());
        dto.setCreatedAt(lesson.getCreatedAt());
        dto.setUpdatedAt(lesson.getUpdatedAt());
        return dto;
    }
}
