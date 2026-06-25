package com.hsms.userservice.entity;

import com.hsms.userservice.enums.AvailabilityStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technicians")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Technician {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "technician_seq")
	@SequenceGenerator(name = "technician_seq", sequenceName = "technician_seq", allocationSize = 1)
	@Column(name = "technician_id")
	private Long technicianId;

	private Long userId;

	private String skill;

	private Integer experience;

	@Enumerated(EnumType.STRING)
	private AvailabilityStatus availabilityStatus;

	private Double rating;
}