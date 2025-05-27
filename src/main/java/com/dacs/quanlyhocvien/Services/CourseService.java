package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.Response.*;
import com.dacs.quanlyhocvien.Repository.ICategoryRepository;
import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.Repository.IEnrollmentRepository;
import com.dacs.quanlyhocvien.Repository.ILessonRepository;
import com.dacs.quanlyhocvien.exceptions.ResourceNotFoundException;
import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.LessonModel;
import com.dacs.quanlyhocvien.models.ModuleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final ICourseRepository courseRepository;
    private final IEnrollmentRepository enrollmentRepository;
    private final CloudinaryService cloudinaryService;
    private final ILessonRepository iLessonRepository;
    private final ICategoryRepository iCategoryRepository;

    public CourseService(ICourseRepository courseRepository,
                         IEnrollmentRepository enrollmentRepository, CloudinaryService cloudinaryService, ILessonRepository iLessonRepository, ICategoryRepository iCategoryRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.cloudinaryService = cloudinaryService;
        this.iLessonRepository = iLessonRepository;
        this.iCategoryRepository = iCategoryRepository;
    }

    public CourseModel saveCourse(CourseModel course) {
        courseRepository.save(course);
        return course;
    }

    // Example method to get all courses
    public List<CourseModel> getAllCourses() {
        // Logic to retrieve all courses
        return courseRepository.findAll();
    }

    /**
     * Lấy danh sách khóa học phân trang với các bộ lọc
     */
    public Page<CourseResponseDTO> getCourses(Pageable pageable, String search, String categoryIds,
                                              String levels, String priceTypes, String isPublished, Long accountId) {
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

        if (isPublished != null && !isPublished.isEmpty()) {
            if ("true".equalsIgnoreCase(isPublished)) {
                spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isPublished")));
            } else if ("false".equalsIgnoreCase(isPublished)) {
                spec = spec.and((root, query, cb) -> cb.isFalse(root.get("isPublished")));
            }
            // Nếu giá trị không phải true/false, không áp dụng bộ lọc
        }

        // Lấy dữ liệu từ database
        Page<CourseModel> coursePage = courseRepository.findAll(spec, pageable);

        // Chuyển đổi từ Entity sang DTO
        if (accountId != null) {
            // Lấy danh sách ID khóa học đã đăng ký để đánh dấu trạng thái
            final List<Long> enrolledIds = enrollmentRepository.findCourseIdsByAccountId(accountId);

            // Chuyển đổi và thêm thông tin đăng ký
            return coursePage.map(course -> {
                CourseResponseDTO dto = CourseResponseDTO.fromEntity(course);
                dto.setIsEnrolled(enrolledIds.contains(course.getCourseId()));
                return dto;
            });
        } else {
            // Nếu không có accountId, chỉ chuyển đổi bình thường
            return coursePage.map(CourseResponseDTO::fromEntity);
        }
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
    public List<CourseResponseDTO> getPopularCourses(int limit) {
        // Tìm khóa học có nhiều người đăng ký nhất
        List<CourseModel> popularCourses = courseRepository.findPopularCourses(PageRequest.of(0, limit));

        // Chuyển đổi sang DTO và trả về
        return popularCourses.stream()
                .map(course -> {
                    CourseResponseDTO dto = CourseResponseDTO.fromEntity(course);

                    // Thêm thông tin về số lượng người đăng ký
                    dto.setEnrollmentCount(course.getTotalStudents());

                    // Thêm thông tin đánh giá
                    dto.setRating(4.5); // Giả định - thay thế bằng dữ liệu thực từ DB
                    dto.setRatingCount(85); // Giả định - thay thế bằng dữ liệu thực từ DB

                    // Kiểm tra xem khóa học có phải mới không (ví dụ: tạo trong 7 ngày qua)
                    LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
                    dto.setIsNew(course.getCreatedAt().isAfter(oneWeekAgo));

                    return dto;
                })
                .collect(Collectors.toList());
    }
    public long countAllCourses() {
        try {
            long count = courseRepository.count();
            return count;
        } catch (Exception e) {
            return 0;
        }
    }


    public long countByStatus(boolean isPublished) {
        try {
            long count = courseRepository.countByIsPublished(isPublished);
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    public long countByFeatured(boolean isFeatured) {
        try {
            long count = courseRepository.countByIsFeatured(isFeatured);

            return count;
        } catch (Exception e) {
            return 0;
        }
    }
    @Transactional
    public void deleteCourse(Long courseId) {
        // Lấy thông tin khóa học
        CourseModel course = getCourseById(courseId);
        if (course == null) {
            throw new RuntimeException("Không tìm thấy khóa học với ID: " + courseId);
        }

        try {
            // 1. Xóa thumbnail trên Cloudinary (nếu có)
            String thumbnailUrl = course.getThumbnailPath();
            if (thumbnailUrl != null && thumbnailUrl.contains("cloudinary")) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(thumbnailUrl);
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                }
            }



            // 3. Xóa các lịch sử đăng ký khóa học (nếu có)
            enrollmentRepository.deleteAllByCourseId(courseId);


//            // 4. Xóa các đánh giá khóa học (nếu có)
//            reviewRepository.deleteAllByCourseId(courseId);


            // 5. Xóa khóa học
            courseRepository.deleteById(courseId);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa khóa học: " + e.getMessage(), e);
        }
    }
    public CourseModel getCourseById(Long courseId) {
        Optional<CourseModel> courseOptional = courseRepository.findById(courseId);
        return courseOptional.orElse(null);
    }
    @Transactional
    public CourseModel updateCourse(Long courseId, CourseUpdateDTO courseUpdateDTO, MultipartFile thumbnail) {
        // Lấy thông tin khóa học hiện tại
        CourseModel course = getCourseById(courseId);
        if (course == null) {
            throw new RuntimeException("Không tìm thấy khóa học với ID: " + courseId);
        }

        try {
            // Cập nhật thông tin cơ bản
            course.setTitle(courseUpdateDTO.getTitle());
            course.setDescription(courseUpdateDTO.getDescription());
            course.setPrice(courseUpdateDTO.getPrice());
            course.setDiscountPrice(courseUpdateDTO.getDiscountPrice());
            course.setDuration(courseUpdateDTO.getDuration());
            course.setLevel(courseUpdateDTO.getLevel());

            // Xử lý trạng thái xuất bản
            boolean wasPublished = course.getIsPublished() != null && course.getIsPublished();
            boolean willBePublished = courseUpdateDTO.getIsPublished() != null && courseUpdateDTO.getIsPublished();

            course.setIsPublished(courseUpdateDTO.getIsPublished());

            // Nếu khóa học được xuất bản lần đầu, cập nhật thời gian xuất bản
            if (!wasPublished && willBePublished) {
                course.setPublishedAt(LocalDateTime.now());
            }

            course.setIsFeatured(courseUpdateDTO.getIsFeatured());

            // Cập nhật danh mục nếu có thay đổi
            if (courseUpdateDTO.getCategoryId() != null) {
                CourseCategoryModel category = iCategoryRepository.findById(courseUpdateDTO.getCategoryId())
                        .orElseThrow(() -> {
                            return new RuntimeException("Không tìm thấy danh mục với ID: " + courseUpdateDTO.getCategoryId());
                        });

                course.setCategory(category);;
            }

            // Xử lý thumbnail nếu có
            if (thumbnail != null && !thumbnail.isEmpty()) {
                // Xóa thumbnail cũ trên Cloudinary (nếu có)
                String oldThumbnailUrl = course.getThumbnailPath();
                if (oldThumbnailUrl != null && oldThumbnailUrl.contains("cloudinary")) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(oldThumbnailUrl);
                    if (publicId != null) {
                        cloudinaryService.deleteImage(publicId);
                    }
                }

                // Upload thumbnail mới lên Cloudinary
                String folder = "course-thumbnails";
                String filename = "course_" + courseId + "_" + System.currentTimeMillis();
                String thumbnailUrl = cloudinaryService.uploadImage(thumbnail, filename);

                // Cập nhật đường dẫn thumbnail mới
                course.setThumbnailPath(thumbnailUrl);
            }

            // Cập nhật thời gian cập nhật
            // Không cần thiết nếu có @UpdateTimestamp trên trường updatedAt
            // course.setUpdatedAt(new Date());

            // Lưu khóa học đã cập nhật
            CourseModel updatedCourse = courseRepository.save(course);


            return updatedCourse;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật khóa học: " + e.getMessage(), e);
        }
    }
}
