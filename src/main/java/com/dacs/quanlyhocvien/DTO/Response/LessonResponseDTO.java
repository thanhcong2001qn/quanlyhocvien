package com.dacs.quanlyhocvien.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponseDTO {
    private Long lessonId;
    private Long moduleId;
    private String title;
    private String description;
    private Integer duration;
    private Integer position;
    private Boolean isFree;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String videoUrl;
    private String videoTitle;
    private Integer videoDuration; // in seconds
    private MultipartFile videoThumbnailFile; // For file upload
    private String videoThumbnailUrl; // For URL input
    private Boolean isVideoDownloadable;

}