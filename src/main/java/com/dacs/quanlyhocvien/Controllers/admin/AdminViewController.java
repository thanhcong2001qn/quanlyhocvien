package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.AccountService;
import com.dacs.quanlyhocvien.Services.AdminService;
import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.Services.TeacherService;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
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
    @Autowired
    private  TeacherService teacherService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private AccountService accountService;


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

    @GetMapping(value = "/add-admin")
    public String addAdmin(){
        return "views/admin/AddAdmin";
    }
    @GetMapping(value = "/all-admin")
    public String allAdmin(Model model){
        List<AdminModel> admins = adminService.getAllAdmins();
        model.addAttribute("admins", admins);
        return "views/admin/AllAdmin";
    }

    @GetMapping(value = "/all-account")
    public String allAccount(Model model){
        List<AccountModel> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "views/admin/AllAccounts";
    }


}
