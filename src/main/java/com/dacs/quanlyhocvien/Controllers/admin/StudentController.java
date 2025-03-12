package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping(value = "/apiAddStudent")
    public ResponseEntity<StudentModel> createStudent(@RequestBody StudentModel student) {
        StudentModel savedStudent = studentService.addStudent(student);
        if (savedStudent == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }else
            return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
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
