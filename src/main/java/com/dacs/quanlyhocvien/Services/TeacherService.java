package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.ITeacherRepository;
import com.dacs.quanlyhocvien.models.TeacherModel;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {
    private final ITeacherRepository teacherRepository;
    private final IAccountRepository accountRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public TeacherService(ITeacherRepository teacherRepository, IAccountRepository accountRepository, FileStorageService fileStorageService) {
        this.teacherRepository = teacherRepository;
        this.accountRepository = accountRepository;
        this.fileStorageService = fileStorageService;
    }

    public TeacherModel addTeacher(TeacherModel teacher, MultipartFile file) {
        if (teacher == null || teacher.getAccount() == null) {
            throw new IllegalArgumentException("Teacher hoặc Account không được null");
        }

        AccountModel account = teacher.getAccount();
        // Kiểm tra xem tài khoản đã tồn tại chưa
        AccountModel existingAccount = accountRepository.findByEmail(account.getEmail());
        if (existingAccount != null) {
            throw new IllegalArgumentException("Email đã tồn tại, không thể thêm giáo viên mới!");
        }else{

        // Nếu có file ảnh, lưu ảnh
            if (file != null && !file.isEmpty()) {
                account.setAvatarPath(fileStorageService.storeFile(file, account.getEmail()));
            }
            account.setPassword("1234"); // Gán password mặc định
            account = accountRepository.save(account); // Lưu tài khoản trước
        }

        teacher.setAccount(account); // Gán account đã lưu vào teacher
        return teacherRepository.save(teacher);
    }

    public TeacherModel updateTeacher(TeacherModel teacher) {
        if (teacher == null || teacher.getAccount() == null) {
            throw new IllegalArgumentException("Giáo viên hoặc tài khoản không được null");
        }

        Optional<AccountModel> existingAccount = accountRepository.findById(teacher.getAccount().getAccountId());
        if (existingAccount.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản giáo viên liên kết!");
        }

        AccountModel updatedAccount = existingAccount.get();
        updatedAccount.setFullName(teacher.getAccount().getFullName());
        updatedAccount.setDateOfBirth(teacher.getAccount().getDateOfBirth());
        updatedAccount.setPhoneNumber(teacher.getAccount().getPhoneNumber());
        updatedAccount.setAddress(teacher.getAccount().getAddress());
        updatedAccount.setGender(teacher.getAccount().getGender());

        accountRepository.save(updatedAccount);

        Optional<TeacherModel> existingTeacher = teacherRepository.findById(teacher.getTeacherId());
        if (existingTeacher.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy giáo viên!");
        }

        TeacherModel updatedTeacher = existingTeacher.get();
        updatedTeacher.setSubjectSpecialization(teacher.getSubjectSpecialization());
        updatedTeacher.setAccount(updatedAccount);

        return teacherRepository.save(updatedTeacher);
    }

    public List<TeacherModel> getAllTeachers(){
        List<TeacherModel> teachers = teacherRepository.findAll();
        return teachers;
    }

    public TeacherModel getTeacherById(Long id){
        return teacherRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteTeacher(Long id){
        teacherRepository.deleteById(id);
    }

    public List<TeacherModel> searchTeachers(String name, String subject) {
        if ((name == null || name.trim().isEmpty()) &&
                (subject == null || subject.trim().isEmpty())) {
            return teacherRepository.findAll();
        }
        return teacherRepository.searchTeachers(name, subject);
    }
}
