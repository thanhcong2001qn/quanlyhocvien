package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.LessonOrderDTO;
import com.dacs.quanlyhocvien.DTO.ModuleOrderDTO;
import com.dacs.quanlyhocvien.DTO.Response.ModuleResponseDTO;
import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.Repository.ILessonRepository;
import com.dacs.quanlyhocvien.Repository.IModuleRepository;
import com.dacs.quanlyhocvien.exceptions.ResourceNotFoundException;
import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.ModuleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for module operations
 *
 * @author thanhcong2001qn
 * @version 1.0
 * @since 2025-05-27
 */
@Service
public class ModuleService {

    private static final Logger logger = LoggerFactory.getLogger(ModuleService.class);

    @Autowired
    private IModuleRepository moduleRepository;

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private LessonService lessonService;
    @Autowired
    private ILessonRepository iLessonRepository;

    /**
     * Get all modules for a course with their lessons
     *
     * @param courseId Course ID
     * @return List of modules with lessons
     */
    public List<ModuleResponseDTO> getModulesByCourse(Long courseId) {
        List<ModuleModel> modules = moduleRepository.findByCourse_CourseIdOrderByPosition(courseId);

        return modules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Count modules for a course
     *
     * @param courseId Course ID
     * @return Number of modules
     */
    public int countModulesByCourse(Long courseId) {
        return moduleRepository.countByCourse_CourseId(courseId);
    }

    /**
     * Count lessons for a course across all modules
     *
     * @param courseId Course ID
     * @return Number of lessons
     */
    public int countLessonsByModule(Long courseId) {
        return moduleRepository.countLessonsByModuleId(courseId);
    }

    /**
     * Create a new module
     *
     * @param moduleDTO Module data
     * @return Created module
     * @throws ResourceNotFoundException if course not found
     */
    @Transactional
    public ModuleResponseDTO createModule(ModuleResponseDTO moduleDTO) {
        CourseModel course = courseRepository.findById(moduleDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + moduleDTO.getCourseId()));

        // If position is not specified, set it to the next available position
        if (moduleDTO.getPosition() == null) {
            int maxPosition = moduleRepository.findMaxPositionByCourse_CourseId(moduleDTO.getCourseId());
            moduleDTO.setPosition(maxPosition + 1);
        }

        ModuleModel module = new ModuleModel();
        module.setCourse(courseRepository.findByCourseId(moduleDTO.getCourseId()));
        module.setTitle(moduleDTO.getTitle());
        module.setDescription(moduleDTO.getDescription());
        module.setPosition(moduleDTO.getPosition());
        module.setIsFree(moduleDTO.getIsFree());
        module.setCreatedAt(LocalDateTime.now());
        module.setUpdatedAt(LocalDateTime.now());

        ModuleModel savedModule = moduleRepository.save(module);

        // Update course updatedAt
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        logger.info("Module created successfully with id: {}", savedModule.getModuleId());

        return mapToDTO(savedModule);
    }

    /**
     * Update a module
     *
     * @param moduleDTO Module data with updates
     * @return Updated module
     * @throws ResourceNotFoundException if module not found
     */
    @Transactional
    public ModuleResponseDTO updateModule(ModuleResponseDTO moduleDTO) {
        logger.info("Updating module with id: {}", moduleDTO.getModuleId());

        ModuleModel module = moduleRepository.findById(moduleDTO.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy module với ID: " + moduleDTO.getModuleId()));

        module.setTitle(moduleDTO.getTitle());
        module.setDescription(moduleDTO.getDescription());

        if (moduleDTO.getPosition() != null) {
            module.setPosition(moduleDTO.getPosition());
        }

        if (moduleDTO.getIsFree() != null) {
            module.setIsFree(moduleDTO.getIsFree());
        }

        module.setUpdatedAt(LocalDateTime.now());

        ModuleModel updatedModule = moduleRepository.save(module);

        // Update course updatedAt
        CourseModel course = courseRepository.findById(module.getCourse().getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + module.getCourse().getCourseId()));
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        logger.info("Module updated successfully with id: {}", updatedModule.getModuleId());

        return mapToDTO(updatedModule);
    }

    /**
     * Delete a module and all its lessons
     *
     * @param moduleId Module ID
     * @throws ResourceNotFoundException if module not found
     */
    @Transactional
    public void deleteModule(Long moduleId) {
        ModuleModel module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy module với ID: " + moduleId));

        // Check if module has lessons
        int lessonCount = iLessonRepository.countLessonsByModule_ModuleId(moduleId);
        if (lessonCount > 0) {
            throw new RuntimeException("Không thể xóa module vì còn chứa " + lessonCount + " bài học. Vui lòng xóa tất cả bài học trước.");
        }

        // Delete the module
        moduleRepository.delete(module);

        // Update course updatedAt
        CourseModel course = courseRepository.findById(module.getCourse().getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + module.getCourse().getCourseId()));
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    /**
     * Update order of modules in a course
     *
     * @param courseId Course ID
     * @param orderUpdates List of module order updates
     */
    @Transactional
    public void updateModuleOrder(Long courseId, List<ModuleOrderDTO> orderUpdates) {
        logger.info("Updating module order for courseId: {} with {} updates", courseId, orderUpdates.size());

        // Verify that the course exists
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        // Update each module's order
        for (ModuleOrderDTO update : orderUpdates) {
            moduleRepository.updateModulePosition(update.getModuleId(), update.getPosition());
        }

        // Update course updatedAt
        CourseModel course = courseRepository.getById(courseId);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        logger.info("Module order updated successfully for courseId: {}", courseId);
    }


    private ModuleResponseDTO mapToDTO(ModuleModel module) {
        ModuleResponseDTO dto = new ModuleResponseDTO();

        dto.setModuleId(module.getModuleId());
        dto.setCourseId(module.getCourse().getCourseId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setPosition(module.getPosition());
        dto.setIsFree(module.getIsFree());
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());

        // Get lessons for this module
        //dto.setLessons(lessonService.getLessonsByModule(module.getModuleId()));

        return dto;
    }
}