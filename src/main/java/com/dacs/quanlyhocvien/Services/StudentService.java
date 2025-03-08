package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    private IStudentRepository studentRepository;
    public StudentModel addStudent(StudentModel student){
        student.setPassword("1234");
        return studentRepository.save(student);
    }
}
