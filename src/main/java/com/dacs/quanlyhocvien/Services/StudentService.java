package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.Request.ChangePasswordRequest;
import com.dacs.quanlyhocvien.DTO.Request.UpdateStudentRequest;
import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.StudentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.dacs.quanlyhocvien.models.dto.StudentResponseDTO;

import java.util.List;

@Service
public class StudentService {
    private final IStudentRepository studentRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StudentService(IStudentRepository studentRepository, FileStorageService fileStorageService, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
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

    public List<StudentModel> searchStudents (String name, String className){
        return studentRepository.findByAccountFullNameContainingIgnoreCaseAndClassNameContainingIgnoreCase(name, className);
    }
    public Page<StudentResponseDTO> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable)
                .map(this::mapToDto); // map từng StudentModel sang StudentResponseDTO
    }

    private StudentResponseDTO mapToDto(StudentModel student) {
        return StudentResponseDTO.builder()
                .studentId(student.getStudentId())
                .fullName(student.getAccount() != null ? student.getAccount().getFullName() : null)
                .gender(student.getAccount() != null ? student.getAccount().getGender() : null)
//                .className(student.getClassName()) // className lấy từ StudentModel
                .dateOfBirth(student.getAccount() != null ? student.getAccount().getDateOfBirth() : null)
                .address(student.getAccount() != null ? student.getAccount().getAddress() : null)
                .phoneNumber(student.getAccount() != null ? student.getAccount().getPhoneNumber() : null)
                .build();
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
