package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class AdminViewController {
    @Autowired
    private StudentService studentService;
    @GetMapping(value = "/dashboard")
    public String admin(){
        return "views/admin/Dashboard";
    }
    @GetMapping(value = "/AllStudent")
    public String allStudent(Model model){
        List<StudentModel> students = studentService.getAllStudents();
        model.addAttribute("students", studentService.getAllStudents());
        return "views/admin/AllStudent";
    }
    @GetMapping(value = "/add-student")
    public String addStudent(){
        return "views/admin/AddStudent";
    }
    @GetMapping(value = "/studentDetail/{id}")
    public String editStudent(@PathVariable Long id,Model model){
        StudentModel student = studentService.getStudentById(id);
        model.addAttribute("student",student);
        return "views/admin/StudentDetail";
    }

}
