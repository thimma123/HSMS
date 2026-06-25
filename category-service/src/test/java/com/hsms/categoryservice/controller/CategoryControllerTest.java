package com.hsms.categoryservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsms.categoryservice.model.CategoryRequestDTO;
import com.hsms.categoryservice.model.CategoryResponseDTO;
import com.hsms.categoryservice.service.CategoryService;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryRequestDTO requestDTO;
    private CategoryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        requestDTO = new CategoryRequestDTO();
        requestDTO.setCategoryName("Plumbing");
        requestDTO.setDescription("Plumbing Service");
        requestDTO.setBasePrice(500.0);

        responseDTO = new CategoryResponseDTO();
        responseDTO.setCategoryId(1L);
        responseDTO.setCategoryName("Plumbing");
        responseDTO.setDescription("Plumbing Service");
        responseDTO.setBasePrice(500.0);
    }

    @Test
    void testAddCategory() throws Exception {

        when(categoryService.addCategory(any(CategoryRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetCategoryById() throws Exception {

        when(categoryService.findCategoryById(1L))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllCategories() throws Exception {

        when(categoryService.findAllCategories())
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/categories/all"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetActiveCategories() throws Exception {

        when(categoryService.getActiveCategories())
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCategory() throws Exception {

        when(categoryService.updateCategory(
                anyLong(),
                any(CategoryRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCategory() throws Exception {

        when(categoryService.deleteCategory(1L))
                .thenReturn("Category deleted successfully");

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk());
    }
}