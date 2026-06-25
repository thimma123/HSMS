package com.hsms.categoryservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hsms.categoryservice.model.CategoryRequestDTO;
import com.hsms.categoryservice.model.CategoryStatusDTO;
import com.hsms.categoryservice.model.CategoryResponseDTO;
import com.hsms.categoryservice.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Valid
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

	
	private final CategoryService categoryService;

	@PostMapping
	public ResponseEntity<CategoryResponseDTO> addCategory(@Valid @RequestBody CategoryRequestDTO category) {

		return new ResponseEntity<>(categoryService.addCategory(category), HttpStatus.CREATED);
	}

	@PatchMapping("/{categoryId}/status")
	public ResponseEntity<CategoryResponseDTO> updateCategoryStatus(@PathVariable Long categoryId,
			@Valid @RequestBody CategoryStatusDTO statusDTO) {

		CategoryResponseDTO response = categoryService.updateCategoryStatus(categoryId, statusDTO);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{categoryId}")
	public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long categoryId) {

		return ResponseEntity.ok(categoryService.findCategoryById(categoryId));
	}

	@GetMapping("/all")
	public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {

		return ResponseEntity.ok(categoryService.findAllCategories());
	}

	@GetMapping("/active")
	public ResponseEntity<List<CategoryResponseDTO>> getActiveCategories() {

		return ResponseEntity.ok(categoryService.getActiveCategories());
	}

	@PutMapping("/{categoryId}")
	public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long categoryId,
			@Valid @RequestBody CategoryRequestDTO category) {

		return ResponseEntity.ok(categoryService.updateCategory(categoryId, category));
	}

	@DeleteMapping("/{categoryId}")
	public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {

		return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
	}

	@GetMapping("/{categoryId}/exists")
	public ResponseEntity<Boolean> categoryExists(@PathVariable Long categoryId) {
		return ResponseEntity.ok(categoryService.categoryExists(categoryId));
	}
}