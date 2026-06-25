package com.hsms.categoryservice.service;

import java.util.List;

import com.hsms.categoryservice.model.CategoryRequestDTO;
import com.hsms.categoryservice.model.CategoryResponseDTO;
import com.hsms.categoryservice.model.CategoryStatusDTO;

public interface CategoryService {

    CategoryResponseDTO addCategory(CategoryRequestDTO category);

    CategoryResponseDTO findCategoryById(Long categoryId);

    List<CategoryResponseDTO> findAllCategories();
    
    List<CategoryResponseDTO> getActiveCategories();
    
    CategoryResponseDTO updateCategory(Long categoryId,
            CategoryRequestDTO category);

    String deleteCategory(Long categoryId);

    CategoryResponseDTO updateCategoryStatus(
            Long categoryId,
            CategoryStatusDTO statusDTO);

    Boolean categoryExists(Long categoryId);
}