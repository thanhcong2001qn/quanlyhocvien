package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.VideoModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IVideoRepository extends JpaRepository<VideoModel,Long> {
    VideoModel findByLesson_LessonId(Long lessonLessonId);
}
