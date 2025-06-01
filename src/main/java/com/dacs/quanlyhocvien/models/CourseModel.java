package com.dacs.quanlyhocvien.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"category", "modules"})
public class CourseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private CourseCategoryModel category;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column
    private Integer duration;

    @Column(length = 50)
    private String level;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column
    private Double rating;

    @Column(name = "total_students")
    private Integer totalStudents = 0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleModel> modules = new ArrayList<>();

    // Helper method để thêm module vào khóa học
    public void addModule(ModuleModel module) {
        modules.add(module);
        module.setCourse(this);
    }

    // Helper method để xóa module khỏi khóa học
    public void removeModule(ModuleModel module) {
        modules.remove(module);
        module.setCourse(null);
    }
}
