package com.dacs.quanlyhocvien.DTO.Response;

import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponeDTO {
    private Long categoryId;
    private String categoryName;
    private String description;
    private String iconPath;

    // Phương thức chuyển đổi từ Entity sang DTO
    public static CategoryResponeDTO fromEntity(CourseCategoryModel entity) {
        if (entity == null) return null;

        CategoryResponeDTO dto = new CategoryResponeDTO();
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setDescription(entity.getDescription());
        dto.setIconPath(entity.getIconPath());

        return dto;
    }

    // Phương thức chuyển đổi danh sách Entity sang danh sách DTO
    public static List<CategoryResponeDTO> fromEntities(List<CourseCategoryModel> entities) {
        if (entities == null) return Collections.emptyList();

        return entities.stream()
                .map(CategoryResponeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", description='" + description + '\'' +
                ", iconPath='" + iconPath + '\'' +
                '}';
    }
}

