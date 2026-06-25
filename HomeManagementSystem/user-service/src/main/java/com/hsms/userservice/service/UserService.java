package com.hsms.userservice.service;

import java.util.List;

import com.hsms.userservice.model.CustomerDetailResponseDTO;
import com.hsms.userservice.model.CustomerProfileRequestDTO;
import com.hsms.userservice.model.TechnicianDetailResponseDTO;
import com.hsms.userservice.model.TechnicianProfileRequestDTO;

public interface UserService {



    CustomerDetailResponseDTO updateCustomer(
            Long userId,
            CustomerProfileRequestDTO dto);

    CustomerDetailResponseDTO getCustomer(
            Long userId);

    List<CustomerDetailResponseDTO> getAllCustomers();

    void deleteCustomer(Long userId);

    TechnicianDetailResponseDTO createTechnician(
            TechnicianProfileRequestDTO dto);

    TechnicianDetailResponseDTO updateTechnician(
            Long userId,
            TechnicianProfileRequestDTO dto);

    TechnicianDetailResponseDTO getTechnician(
            Long userId);

    List<TechnicianDetailResponseDTO> getAllTechnicians();

    void deleteTechnician(Long userId);

	TechnicianDetailResponseDTO getTechnicianById(Long technicianId);

	CustomerDetailResponseDTO getCustomerById(Long customerId);
	
	
	CustomerDetailResponseDTO createCustomer(
	        CustomerProfileRequestDTO dto,
	        Long userId,
	        String email);

	TechnicianDetailResponseDTO updateTechnicianRating(Long technicianId, Double rating);
}
