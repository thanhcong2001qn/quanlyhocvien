package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.TeacherService;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import com.dacs.quanlyhocvien.models.TeacherRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
public class AdminTeacherController {
    private final TeacherService teacherService;

    @Autowired
    public AdminTeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping(value = "/apiAddTeacher", consumes = "multipart/form-data")
    public ResponseEntity<?> createTeacher(@ModelAttribute TeacherRequestDTO requestDTO) {
        if (requestDTO.getTeacher() == null || requestDTO.getTeacher().getAccount() == null) {
            return new ResponseEntity<>("Lỗi: Thiếu thông tin giáo viên hoặc tài khoản", HttpStatus.BAD_REQUEST);
        }

        // Nếu username null, đặt bằng email
        if (requestDTO.getTeacher().getAccount().getUsername() == null) {
            requestDTO.getTeacher().getAccount().setUsername(requestDTO.getTeacher().getAccount().getEmail());
        }

        // Nếu email trống, trả về lỗi
        if (requestDTO.getTeacher().getAccount().getEmail() == null ||
                requestDTO.getTeacher().getAccount().getEmail().isEmpty()) {
            return new ResponseEntity<>("Email không được để trống", HttpStatus.BAD_REQUEST);
        }

        TeacherModel savedTeacher = teacherService.addTeacher(requestDTO.getTeacher(), requestDTO.getFile());
        return new ResponseEntity<>(savedTeacher, HttpStatus.CREATED);
    }
    @PutMapping(value = "/apiEditTeacher")
    public ResponseEntity<TeacherModel> updateTeacher(@RequestBody TeacherModel teacher) {
        TeacherModel savedTeacher = teacherService.updateTeacher(teacher);
        return new ResponseEntity<>(savedTeacher, HttpStatus.OK);
    }

    @DeleteMapping(value = "/deleteTeacher/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
