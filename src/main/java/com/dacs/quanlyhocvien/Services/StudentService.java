package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    private IStudentRepository studentRepository;
    public StudentModel addStudent(StudentModel student){
        if (studentRepository.findByEmail(student.getEmail()) == null){
            student.setPassword("1234");
            return studentRepository.save(student);
        }
        else return null;
    }
    public StudentModel updateStudent(StudentModel student){
        student.setPassword("1234");
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
}
