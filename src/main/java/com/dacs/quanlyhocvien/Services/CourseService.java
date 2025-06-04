package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.Response.*;
import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.exceptions.ResourceNotFoundException;
import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.LessonModel;
import com.dacs.quanlyhocvien.models.ModuleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final ICourseRepository courseRepository;

    public CourseService(ICourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Implement the methods for course management here
    // For example, methods to add, update, delete, and retrieve courses

    // Example method to add a course
    public void addCourse(String courseName, String courseDescription) {
        // Logic to add a course
    }

    // Example method to get all courses
    public List<CourseModel> getAllCourses() {
        // Logic to retrieve all courses
        return courseRepository.findAll();
    }

    // Trong CourseService
    public Page<CourseResponseDTO> getCourses(Pageable pageable, String search, String categoryIds,
                                              String levels, String priceTypes) {
        // Xây dựng Specification dựa trên các tham số
        Specification<CourseModel> spec = Specification.where(null);

        // Thêm điều kiện tìm kiếm
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        // Thêm điều kiện lọc danh mục
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Integer> catIds = Arrays.stream(categoryIds.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            spec = spec.and((root, query, cb) ->
                    root.get("category").get("categoryId").in(catIds)
            );
        }

        // Thêm điều kiện lọc cấp độ
        if (levels != null && !levels.isEmpty()) {
            List<String> levelList = Arrays.asList(levels.split(","));
            spec = spec.and((root, query, cb) ->
                    root.get("level").in(levelList)
            );
        }

        // Thêm điều kiện lọc giá
        if (priceTypes != null && !priceTypes.isEmpty()) {
            List<String> priceTypeList = Arrays.asList(priceTypes.split(","));

            if (priceTypeList.contains("free") && priceTypeList.contains("paid")) {
                // Không cần thêm điều kiện vì bao gồm cả hai loại
            } else if (priceTypeList.contains("free")) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("price"), 0)
                );
            } else if (priceTypeList.contains("paid")) {
                spec = spec.and((root, query, cb) ->
                        cb.greaterThan(root.get("price"), 0)
                );
            }
        }

        // Nếu bạn chỉ muốn các khóa học đã xuất bản
        spec = spec.and((root, query, cb) ->
                cb.isTrue(root.get("isPublished"))
        );

        // Lấy dữ liệu từ database
        Page<CourseModel> coursePage = courseRepository.findAll(spec, pageable);

        // Chuyển đổi từ Entity sang DTO để tránh vấn đề Lazy Loading
        return coursePage.map(CourseResponseDTO::fromEntity);
    }
    @Transactional(readOnly = true)
    public CourseDetailResponeDTO getCourseDetail(Long courseId) {
        // Tìm khóa học theo ID, ném exception nếu không tồn tại
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        // Tạo DTO kết quả
        CourseDetailResponeDTO courseDetail = new CourseDetailResponeDTO();

        // Ánh xạ thông tin cơ bản của khóa học
        courseDetail.setCourseId(course.getCourseId());
        courseDetail.setTitle(course.getTitle());
        courseDetail.setDescription(course.getDescription());
        courseDetail.setThumbnailPath(course.getThumbnailPath());
        courseDetail.setPrice(course.getPrice());
        courseDetail.setDiscountPrice(course.getDiscountPrice());
        courseDetail.setLevel(course.getLevel());
        courseDetail.setPublishedAt(course.getPublishedAt());
        courseDetail.setIsPublished(course.getIsPublished());
        courseDetail.setIsFeatured(course.getIsFeatured());
        courseDetail.setRating(course.getRating());
        courseDetail.setTotalStudents(course.getTotalStudents());
        courseDetail.setTotalReviews(course.getTotalReviews());
        courseDetail.setCreatedAt(course.getCreatedAt());
        courseDetail.setUpdatedAt(course.getUpdatedAt());

        // Thiết lập URL
        courseDetail.setCourseUrl("/course/" + course.getCourseId());

        // Ánh xạ thông tin danh mục
        if (course.getCategory() != null) {
            CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
            categoryDTO.setCategoryId(course.getCategory().getCategoryId());
            categoryDTO.setCategoryName(course.getCategory().getCategoryName());
            categoryDTO.setDescription(course.getCategory().getDescription());
            categoryDTO.setIconPath(course.getCategory().getIconPath());

            // Có thể cần gọi service khác để lấy tổng số khóa học trong danh mục này
            categoryDTO.setTotalCourses(null); // Hoặc gọi service khác để lấy số này

            courseDetail.setCategory(categoryDTO);
        }

//        // Ánh xạ thông tin giảng viên (nếu có)
//        if (course.getInstructor() != null) {
//            InstructorResponseDTO instructorDTO = new InstructorResponseDTO();
//            instructorDTO.setAccountId(course.getInstructor().getAccountId());
//            instructorDTO.setFullName(course.getInstructor().getFullName());
//            instructorDTO.setBiography(course.getInstructor().getBiography());
//            instructorDTO.setAvatarUrl(course.getInstructor().getAvatarUrl());
//            instructorDTO.setProfession(course.getInstructor().getProfession());
//
//            // Các thông tin khác của giảng viên có thể cần gọi service khác để lấy
//            courseDetail.setInstructor(instructorDTO);
//        }

        // Xử lý danh sách modules và bài học
        List<ModuleResponseDTO> moduleList = new ArrayList<>();
        Integer totalDuration = 0;
        Integer totalLessons = 0;
        Boolean hasFreeContent = false;

        // Sắp xếp modules theo vị trí
        List<ModuleModel> sortedModules = course.getModules().stream()
                .sorted(Comparator.comparing(ModuleModel::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        for (ModuleModel module : sortedModules) {
            ModuleResponseDTO moduleDTO = new ModuleResponseDTO();
            moduleDTO.setModuleId(module.getModuleId());
            moduleDTO.setTitle(module.getTitle());
            moduleDTO.setDescription(module.getDescription());
            moduleDTO.setPosition(module.getPosition());
            moduleDTO.setIsFree(module.getIsFree());

            // Nếu module là miễn phí, đánh dấu khóa học có nội dung miễn phí
            if (module.getIsFree()) {
                hasFreeContent = true;
            }

            // Xử lý bài học trong module
            List<LessonResponseDTO> lessons = new ArrayList<>();
            Integer moduleDuration = 0;

            // Sắp xếp bài học theo vị trí
            List<LessonModel> sortedLessons = module.getLessons().stream()
                    .sorted(Comparator.comparing(LessonModel::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            for (LessonModel lesson : sortedLessons) {
                LessonResponseDTO lessonDTO = new LessonResponseDTO();
                lessonDTO.setLessonId(lesson.getLessonId());
                lessonDTO.setTitle(lesson.getTitle());
                lessonDTO.setDescription(lesson.getDescription());
                lessonDTO.setDuration(lesson.getDuration());
                lessonDTO.setPosition(lesson.getPosition());
                lessonDTO.setIsFree(lesson.getIsFree());

                // Nếu bài học là miễn phí, đánh dấu khóa học có nội dung miễn phí
                if (lesson.getIsFree()) {
                    hasFreeContent = true;
                }

                // Cập nhật tổng thời lượng của module và khóa học
                if (lesson.getDuration() != null) {
                    moduleDuration += lesson.getDuration();
                    totalDuration += lesson.getDuration();
                }

                lessons.add(lessonDTO);
                totalLessons++;
            }

            // Cập nhật thông tin module
            moduleDTO.setLessons(lessons);
            moduleDTO.setTotalLessons(lessons.size());
            moduleDTO.setTotalDuration(moduleDuration);

            moduleList.add(moduleDTO);
        }

        // Cập nhật thông tin trên CourseDetailDTO
        courseDetail.setModules(moduleList);
        courseDetail.setTotalModules(moduleList.size());
        courseDetail.setTotalLessons(totalLessons);
        courseDetail.setDuration(totalDuration);
        courseDetail.setHasFreeContent(hasFreeContent);

        return courseDetail;
    }
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getRelatedCourses(Long currentCourseId, Long categoryId, int limit) {
        // Lấy khóa học cùng danh mục, đã được xuất bản, nhưng không phải khóa học hiện tại
        List<CourseModel> relatedCourses = courseRepository
                .findRelatedCoursesByCategory(categoryId, currentCourseId, PageRequest.of(0, limit));

        // Nếu không đủ khóa học theo danh mục, bổ sung các khóa học phổ biến
        if (relatedCourses.size() < limit) {
            int remainingItems = limit - relatedCourses.size();

            // Lấy thêm khóa học phổ biến không thuộc danh mục này và không phải khóa học hiện tại
            List<CourseModel> popularCourses = courseRepository
                    .findPopularCoursesExcludingCategoryAndCourse(categoryId, currentCourseId, PageRequest.of(0, remainingItems));

            relatedCourses.addAll(popularCourses);
        }

        // Chuyển đổi từ entity sang DTO
        return relatedCourses.stream()
                .map(CourseResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


}
