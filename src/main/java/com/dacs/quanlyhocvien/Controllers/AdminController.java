package com.dacs.quanlyhocvien.Controllers;

import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AdminController {
    private StudentService studentService;

    @GetMapping(value = "/admin")
    public String admin(){
        return "views/admin/admin";
    }
    @GetMapping(value = "/AllStudent")
    public String allStudent(){
        return "views/admin/AllStudent";
    }
    @GetMapping(value = "/add-student")
    public String addStudent(){
        return "views/admin/AddStudent";
    }
    @PostMapping(value = "/api/students")
    public ResponseEntity<StudentModel> createStudent(@RequestBody StudentModel student) {
        StudentModel savedStudent = studentService.addStudent(student);
        return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
    }

}
