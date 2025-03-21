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
    @PutMapping(value = "/apiEditStudent")
    public ResponseEntity<StudentModel> updateStudent(@RequestBody StudentModel student) {
        StudentModel savedStudent = studentService.updateStudent(student);
        return new ResponseEntity<>(savedStudent, HttpStatus.OK);
    }
    @DeleteMapping(value = "/deleteStudent/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
