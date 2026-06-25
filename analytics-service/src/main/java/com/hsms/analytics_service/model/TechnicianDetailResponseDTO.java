package com.hsms.analytics_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianDetailResponseDTO {
	@JsonProperty("userId")
	private Long technicianId;
	@JsonProperty("name")
	private String technicianName;
	private Long completedJobs;
	private Double rating;
}
