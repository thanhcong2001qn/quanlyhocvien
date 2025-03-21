package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class StudentService {
    private final IStudentRepository studentRepository;
    private final FileStorageService fileStorageService;
    @Autowired
    public StudentService(IStudentRepository studentRepository, FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
    }

    public StudentModel addStudent(StudentModel student){
        return studentRepository.save(student);
    }
    public StudentModel updateStudent(StudentModel student){
        student.getAccount().setPassword("1234");
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
}
