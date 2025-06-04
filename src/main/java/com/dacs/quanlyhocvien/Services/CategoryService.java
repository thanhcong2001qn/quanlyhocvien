package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.ICategoryRepository;
import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final ICategoryRepository categoryRepository;
    public CategoryService(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CourseCategoryModel> getAllCategories() {
        // Logic to retrieve all categories
        return categoryRepository.findAll();
    }
}
