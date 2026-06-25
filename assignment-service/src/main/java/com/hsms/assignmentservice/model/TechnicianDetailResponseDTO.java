package com.hsms.assignmentservice.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianDetailResponseDTO {
	private Long userId;
	@JsonProperty("technician_Id")
	private Long technicianId;
    private String name;
    private String email;
    private String skill;
    private Integer experience;
    private String availabilityStatus;
    private Double rating;
    private List<AssignmentResponseDTO> currentAssignments;
}