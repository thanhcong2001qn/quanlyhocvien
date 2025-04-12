package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IRoleRepository;
import com.dacs.quanlyhocvien.Repository.ITeacherRepository;
import com.dacs.quanlyhocvien.models.RoleModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.models.dto.TeacherResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final ITeacherRepository teacherRepository;
    private final IAccountRepository accountRepository;
    private final IRoleRepository roleRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;




    public TeacherModel addTeacher(TeacherModel teacher, MultipartFile file) {
        if (teacher == null || teacher.getAccount() == null) {
            throw new IllegalArgumentException("Teacher hoặc Account không được null");
        }
        RoleModel teacherRole = roleRepository.findByRoleName("teacher");
        AccountModel account = teacher.getAccount();
        teacher.getAccount().setRole(teacherRole);
        // Kiểm tra xem tài khoản đã tồn tại chưa
        AccountModel existingAccount = accountRepository.findByEmail(account.getEmail());
        if (existingAccount != null) {
            throw new IllegalArgumentException("Email đã tồn tại, không thể thêm giáo viên mới!");
        }else{
        // Nếu có file ảnh, lưu ảnh
            if (file != null && !file.isEmpty()) {
                account.setAvatarPath(fileStorageService.storeFile(file, account.getEmail()));
            }
            account.setPassword(passwordEncoder.encode("1234")); // Gán password mặc định
            account = accountRepository.save(account); // Lưu tài khoản trước
        }
        TeacherModel teacherModel = new TeacherModel();
        teacherModel.setAccount(account); // Gán account đã lưu vào teacher
        return teacherRepository.save(teacherModel);
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

    // ✅ Hàm phân trang Teacher và map sang DTO
    public Page<TeacherResponseDTO> getTeachers(Pageable pageable) {
        return teacherRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    // ✅ Hàm map từng TeacherModel → TeacherResponseDTO
    private TeacherResponseDTO mapToDto(TeacherModel teacher) {
        TeacherResponseDTO dto = new TeacherResponseDTO();
        dto.setTeacherId(teacher.getTeacherId());
        dto.setFullName(teacher.getAccount() != null ? teacher.getAccount().getFullName() : null);
        dto.setGender(teacher.getAccount() != null ? teacher.getAccount().getGender() : null);
        dto.setSubjectSpecialization(teacher.getSubjectSpecialization());
        dto.setHireDate(teacher.getHireDate() != null ? teacher.getHireDate().toString() : null);
        return dto;
    }
}
