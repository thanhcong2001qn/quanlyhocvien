package com.dacs.quanlyhocvien.Controllers.Course;

import com.dacs.quanlyhocvien.DTO.LessonOrderDTO;
import com.dacs.quanlyhocvien.DTO.Response.ApiResponse;
import com.dacs.quanlyhocvien.DTO.Response.LessonResponseDTO;
import com.dacs.quanlyhocvien.Services.LessonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/lessons")
public class LessonApiController {
    private final LessonService lessonService;

    public LessonApiController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<?> getLessonsByModule(
            @PathVariable Long moduleId) {
        try {
            List<LessonResponseDTO> lessons = lessonService.getLessonsByModule(moduleId);
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{moduleId}")
    public ResponseEntity<?> createLesson(
            @PathVariable Long moduleId,
            @RequestBody LessonResponseDTO lessonDTO) {

        try {
            lessonDTO.setModuleId(moduleId);
            LessonResponseDTO createdLesson = lessonService.createLesson(lessonDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<?> updateLesson(
            @PathVariable Long lessonId,
            @RequestBody LessonResponseDTO lessonDTO) {

        try {
            lessonDTO.setLessonId(lessonId);
            LessonResponseDTO updatedLesson = lessonService.updateLesson(lessonDTO);
            return ResponseEntity.ok(updatedLesson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<?> deleteLesson(
            @PathVariable Long lessonId) {
        try {
            lessonService.deleteLesson(lessonId);
            return ResponseEntity.ok(new ApiResponse(true, "Bài học đã được xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{moduleId}/order")
    public ResponseEntity<?> updateLessonOrder(
            @PathVariable Long moduleId,
            @RequestBody List<LessonOrderDTO> orderUpdates) {
        try {
            lessonService.updateLessonOrder(moduleId, orderUpdates);
            return ResponseEntity.ok(new ApiResponse(true, "Thứ tự bài học đã được cập nhật"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
