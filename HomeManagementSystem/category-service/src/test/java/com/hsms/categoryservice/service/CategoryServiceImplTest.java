package com.hsms.categoryservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import com.hsms.categoryservice.entity.ServiceCategory;
import com.hsms.categoryservice.exception.DuplicateCategoryException;
import com.hsms.categoryservice.exception.ResourceNotFoundException;
import com.hsms.categoryservice.model.CategoryRequestDTO;
import com.hsms.categoryservice.model.CategoryResponseDTO;
import com.hsms.categoryservice.repository.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepo;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private ServiceCategory category;
    private CategoryRequestDTO requestDTO;

    @BeforeEach
    void setUp() {

        category = new ServiceCategory();
        category.setCategoryId(1L);
        category.setCategoryName("Plumbing");
        category.setDescription("Plumbing Services");
        category.setBasePrice(500.0);
        category.setActive(true);

        requestDTO = new CategoryRequestDTO();
        requestDTO.setCategoryName("Plumbing");
        requestDTO.setDescription("Plumbing Services");
        requestDTO.setBasePrice(500.0);
    }

    @Test
    void testAddCategorySuccess() {

        when(categoryRepo.existsByCategoryName("Plumbing"))
                .thenReturn(false);

        when(categoryRepo.save(any(ServiceCategory.class)))
                .thenReturn(category);

        CategoryResponseDTO response =
                categoryService.addCategory(requestDTO);

        assertNotNull(response);
        assertEquals("Plumbing",
                response.getCategoryName());

        verify(categoryRepo, times(1))
                .save(any(ServiceCategory.class));
    }

    @Test
    void testAddCategoryDuplicate() {

        when(categoryRepo.existsByCategoryName("Plumbing"))
                .thenReturn(true);

        assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.addCategory(requestDTO));
    }

    @Test
    void testFindCategoryByIdSuccess() {

        when(categoryRepo.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryResponseDTO response =
                categoryService.findCategoryById(1L);

        assertEquals(1L,
                response.getCategoryId());
    }

    @Test
    void testFindCategoryByIdNotFound() {

        when(categoryRepo.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.findCategoryById(1L));
    }

    @Test
    void testFindAllCategories() {

        when(categoryRepo.findAll())
                .thenReturn(Arrays.asList(category));

        assertEquals(
                1,
                categoryService.findAllCategories().size());
    }

    @Test
    void testGetActiveCategories() {

        when(categoryRepo.findByActiveTrue())
                .thenReturn(Arrays.asList(category));

        assertEquals(
                1,
                categoryService.getActiveCategories().size());
    }

    @Test
    void testUpdateCategorySuccess() {

        when(categoryRepo.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepo.save(any(ServiceCategory.class)))
                .thenReturn(category);

        CategoryResponseDTO response =
                categoryService.updateCategory(
                        1L,
                        requestDTO);

        assertEquals(
                "Plumbing",
                response.getCategoryName());
    }

    @Test
    void testUpdateCategoryNotFound() {

        when(categoryRepo.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(
                        1L,
                        requestDTO));
    }

    @Test
    void testDeleteCategorySuccess() {

        when(categoryRepo.findById(1L))
                .thenReturn(Optional.of(category));

        String response =
                categoryService.deleteCategory(1L);

        assertEquals(
                "Category deleted successfully",
                response);

        verify(categoryRepo, times(1))
                .save(category);
    }

    @Test
    void testDeleteCategoryNotFound() {

        when(categoryRepo.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(1L));
    }
}