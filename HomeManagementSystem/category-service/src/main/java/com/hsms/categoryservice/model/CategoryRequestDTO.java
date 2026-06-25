package com.hsms.categoryservice.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDTO {

    @NotBlank(message = "Category name is required")
    @Size(min = 3, max = 50,
          message = "Category name must be between 3 and 50 characters")
    private String categoryName;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 100,
          message = "Description must be between 5 and 100 characters")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false,
                message = "Base price must be greater than 0")
    private Double basePrice;

	private Boolean active;
		
}