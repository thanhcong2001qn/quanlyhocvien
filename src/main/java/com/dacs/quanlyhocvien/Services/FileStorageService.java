package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {
        private final Path fileStorageLocation;
        @Autowired
        public FileStorageService(@Value("${file.upload-dir:./static/images}") String uploadDir) {
            this.fileStorageLocation = Paths.get(uploadDir)
                    .toAbsolutePath().normalize();
            try {
                Files.createDirectories(this.fileStorageLocation);
            } catch (Exception ex) {
                throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
            }
        }

        public String storeFile(MultipartFile file,String email) {
            // Lấy tên file gốc
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            // Tạo tên file duy nhất để tránh ghi đè
            String fileName = email+ "_" + originalFileName;

            try {
                // Kiểm tra tên file hợp lệ
                if (fileName.contains("..")) {
                    throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
                }

                // Đường dẫn đầy đủ của file
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                // Lưu file vào hệ thống file
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                String path = "/images/"+fileName;
                return path;

            } catch (IOException ex) {
                throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
            }
        }
}
