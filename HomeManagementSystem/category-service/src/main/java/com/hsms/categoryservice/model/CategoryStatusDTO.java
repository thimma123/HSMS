package com.hsms.categoryservice.model;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryStatusDTO {
	@NotNull(message = "Status is required")
	private Boolean active;
}
