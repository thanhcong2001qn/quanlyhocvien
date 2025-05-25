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
    @GetMapping(value = "/all-student")
    public String allStudent(Model model){
//        List<StudentModel> students = studentService.getAllStudents();
//        model.addAttribute("students", studentService.getAllStudents());
        return "views/admin/AllStudent";
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
        model.addAttribute("dummy", "dummyValue"); // ✅ thêm dòng bảo vệ tránh lỗi Thymeleaf
        return "views/admin/AllAdmin";
    }
    @GetMapping(value = "/adminDetail/{id}")
    public String editAdmin(@PathVariable Long id,Model model){
        AdminModel admin = adminService.getAdminById(id);
        model.addAttribute("admin",admin);
        return "views/admin/AdminDetail";
    }

    @GetMapping(value = "/all-account")
    public String allAccount(Model model){
        List<AccountModel> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "views/admin/AllAccounts";
    }
    @GetMapping(value = "/add-course")
    public String addcourse(){
        return "views/admin/add-course";
    }
    @GetMapping(value = "/add-category")
    public String addCategory(){
        return "views/admin/add-category";
    }
    @GetMapping(value = "/all-categories")
    public String allCategory(Model model){
        model.addAttribute("dummy", "dummyValue"); // ✅ thêm dòng bảo vệ tránh lỗi Thymeleaf
        return "views/admin/all-categories";
    }
}
