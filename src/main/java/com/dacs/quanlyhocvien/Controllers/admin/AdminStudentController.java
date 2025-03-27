package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.DTO.Request.StudentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class AdminStudentController {
    private final StudentService studentService;
    @Autowired
    public AdminStudentController(StudentService studentService) {
        this.studentService = studentService;
    }

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


}
