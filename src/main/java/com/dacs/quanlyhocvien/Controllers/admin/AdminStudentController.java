package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.DTO.Request.StudentRequest;
import com.dacs.quanlyhocvien.models.dto.AdminResponseDTO;
import com.dacs.quanlyhocvien.models.dto.StudentResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class AdminStudentController {
    private final StudentService studentService;
    private final IStudentRepository studentRepository;
//    @PostMapping(value = "/apiAddStudent")
//    public ResponseEntity<StudentModel> createStudent(@ModelAttribute StudentRequestDTO requestDTO) {
//        StudentModel savedStudent = studentService.addStudent(requestDTO.getStudent(),requestDTO.getFile());
//        if (savedStudent == null) {
//            return new ResponseEntity<>(HttpStatus.CONFLICT);
//        }else
//            return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
//    }

    @DeleteMapping(value = "/deleteStudent/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public String searchStudents(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "class", required = false) String className,
            Model model,
            HttpServletRequest request) {

        // Gọi service để tìm kiếm student theo name và class
        List<StudentModel> students = studentService.searchStudents(name, className);
        model.addAttribute("students", students);

        // Nếu là Ajax request thì trả về fragment (chỉ tbody)
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "fragments/student/student-table :: tbody"; // 👈 Chỉ render tbody
        }

        // Nếu không thì trả về nguyên trang All Students
        return "views/admin/AllStudents"; // 👈 Trả về trang đầy đủ
    }


    @GetMapping("/api/students")
    public ResponseEntity<Page<StudentResponseDTO>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponseDTO> studentDTOs = studentService.getStudents(pageable); // 👉 gọi service
        return ResponseEntity.ok(studentDTOs);
    }
}
