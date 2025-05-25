package com.dacs.quanlyhocvien.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Tải lên file ảnh lên Cloudinary
     * @param file File ảnh cần tải lên
     * @param folder Thư mục trên Cloudinary (vd: "course-thumbnails")
     * @return URL của ảnh đã tải lên
     */
    public String uploadImage(MultipartFile file, String folder) {
        try {
            if (file == null || file.isEmpty()) {
                logger.warn("Không thể tải lên ảnh rỗng");
                return null;
            }

            // Tạo public_id duy nhất cho ảnh
            String publicId = folder + "/" + UUID.randomUUID().toString();

            // Tải lên ảnh và nhận kết quả
            Map<String, Object> params = ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // Lấy URL từ kết quả
            String imageUrl = (String) uploadResult.get("secure_url");
            logger.info("Tải lên ảnh thành công: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            logger.error("Lỗi khi tải lên ảnh: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tải lên ảnh", e);
        }
    }

    /**
     * Xóa ảnh từ Cloudinary
     * @param publicId Public ID của ảnh cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteImage(String publicId) {
        try {
            // Thực hiện xóa ảnh
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            // Kiểm tra kết quả
            return "ok".equals(result.get("result"));

        } catch (IOException e) {
            logger.error("Lỗi khi xóa ảnh: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Trích xuất public_id từ URL Cloudinary
     * @param imageUrl URL của ảnh
     * @return public_id của ảnh
     */
    public String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            // Format URL: https://res.cloudinary.com/your-cloud-name/image/upload/v1234567890/folder/filename.jpg
            String[] urlParts = imageUrl.split("/");

            // Lấy phần public_id từ URL (loại bỏ phần mở rộng)
            String fileNameWithExtension = urlParts[urlParts.length - 1];
            String[] fileNameParts = fileNameWithExtension.split("\\.");

            // Tạo public_id với định dạng folder/filename
            return urlParts[urlParts.length - 2] + "/" + fileNameParts[0];

        } catch (Exception e) {
            logger.error("Lỗi khi trích xuất public_id: {}", e.getMessage(), e);
            return null;
        }
    }
}