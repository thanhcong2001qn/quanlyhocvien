package com.dacs.quanlyhocvien.Controllers.Course;

import com.dacs.quanlyhocvien.DTO.ModuleOrderDTO;
import com.dacs.quanlyhocvien.DTO.Response.ApiResponse;
import com.dacs.quanlyhocvien.DTO.Response.ModuleResponseDTO;
import com.dacs.quanlyhocvien.Services.ModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ROLE_STUDENT')" + " or hasRole('ROLE_ADMIN')")
@RequestMapping(value = "/api/modules")
public class ModuleApiController {
    private final ModuleService moduleService;

    public ModuleApiController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getModulesByCourse(
            @PathVariable Long courseId) {
        try {
            List<ModuleResponseDTO> modules = moduleService.getModulesByCourse(courseId);
            return ResponseEntity.ok(modules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    @PostMapping("/{courseId}")
    public ResponseEntity<?> createModule(
            @PathVariable Long courseId,@RequestBody ModuleResponseDTO moduleDTO) {
        try {
            moduleDTO.setCourseId(courseId);
            ModuleResponseDTO createdModule = moduleService.createModule(moduleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdModule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    @PutMapping("/{moduleId}")
    public ResponseEntity<?> updateModule(
            @PathVariable Long moduleId,
            @RequestBody ModuleResponseDTO moduleDTO) {
        try {
            moduleDTO.setModuleId(moduleId);
            ModuleResponseDTO updatedModule = moduleService.updateModule(moduleDTO);
            return ResponseEntity.ok(updatedModule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    @DeleteMapping("/{moduleId}")
    public ResponseEntity<?> deleteModule(
            @PathVariable Long moduleId) {
        try {
            moduleService
                    .deleteModule(moduleId);
            return ResponseEntity.ok(new ApiResponse(true, "Module đã được xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    @PutMapping("/{courseId}/order")
    public ResponseEntity<?> updateModuleOrder(
            @PathVariable Long courseId,
            @RequestBody List<ModuleOrderDTO> orderUpdates) {
        try {
            moduleService.updateModuleOrder(courseId, orderUpdates);
            return ResponseEntity.ok(new ApiResponse(true, "Thứ tự module đã được cập nhật"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
