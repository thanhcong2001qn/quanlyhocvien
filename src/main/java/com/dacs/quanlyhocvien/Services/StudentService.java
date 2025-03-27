package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.Request.ChangePasswordRequest;
import com.dacs.quanlyhocvien.DTO.Request.UpdateStudentRequest;
import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class StudentService {
    private final IStudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StudentService(IStudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public StudentModel addStudent(StudentModel student){
        return studentRepository.save(student);
    }
    public List<StudentModel> getAllStudents(){
        return studentRepository.findAll();
    }
    public StudentModel getStudentById(Long id){
        return studentRepository.findById(id).orElse(null);
    }
    public void deleteStudent(Long id){
        studentRepository.deleteById(id);
    }
    public StudentModel getStudentByEmail(String email){
        return studentRepository.findByAccount_Email(email);
    }
    public StudentModel getStudentByUserName(String userName){
        return studentRepository.findByAccount_Username(userName);
    }
    public StudentModel updateStudentByUserName(String userName, UpdateStudentRequest student){
        StudentModel studentModel = studentRepository.findByAccount_Username(userName);
        studentModel.getAccount().setFullName(student.getFullName());
        studentModel.getAccount().setPhoneNumber(student.getPhone());
        studentModel.getAccount().setGender(student.getGender());
        studentModel.getAccount().setDateOfBirth(student.getDateOfBirth());
        studentModel.getAccount().setAddress(student.getAddress());
        return studentRepository.save(studentModel);
    }
    public boolean changePassword(String userName, ChangePasswordRequest passwordData){
        StudentModel studentModel = studentRepository.findByAccount_Username(userName);
        String newPassword = passwordData.getNewPassword();
        String oldPassword = passwordData.getOldPassword();
        if (!passwordEncoder.matches(oldPassword, studentModel.getAccount().getPassword())){
           return false;
        }
        studentModel.getAccount().setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(studentModel);
        return true;
    }
}
