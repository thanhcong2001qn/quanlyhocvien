package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.Services.TeacherService;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class AdminTeacherViewController {
    @Autowired
    private TeacherService teacherService;

    public AdminTeacherViewController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

//    @GetMapping(value = "/dashboard")
//    public String admin(){
//        return "views/admin/Dashboard";
//    }

    @GetMapping(value = "/all-teacher")
    public String allTeacher(Model model){
        List<TeacherModel> teachers = teacherService.getAllTeachers();
        model.addAttribute("teachers", teachers);
        return "views/admin/AllTeacher";
    }
    @GetMapping(value = "/add-teacher")
    public String addTeacher(){
        return "views/admin/AddTeacher";
    }
    @GetMapping(value = "/teacherDetail/{id}")
    public String editTeacher(@PathVariable Long id,Model model){
        TeacherModel teacher = teacherService.getTeacherById(id);
        model.addAttribute("teacher",teacher);
        return "views/admin/TeacherDetail";
    }
}
