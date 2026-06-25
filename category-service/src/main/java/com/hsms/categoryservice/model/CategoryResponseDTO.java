package com.hsms.categoryservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponseDTO {
	private Long categoryId;
    private String categoryName;
    private String description;
    private Double basePrice;
    private Boolean active;

}
