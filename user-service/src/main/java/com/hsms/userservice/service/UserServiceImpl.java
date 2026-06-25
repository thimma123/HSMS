package com.hsms.userservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hsms.userservice.entity.Customer;
import com.hsms.userservice.entity.Technician;
import com.hsms.userservice.exception.ResourceNotFoundException;
import com.hsms.userservice.feign.AuthFeignClient;
import com.hsms.userservice.model.CustomerDetailResponseDTO;
import com.hsms.userservice.model.CustomerProfileRequestDTO;
import com.hsms.userservice.model.TechnicianDetailResponseDTO;
import com.hsms.userservice.model.TechnicianProfileRequestDTO;
import com.hsms.userservice.model.UserProfileResponseDTO;
import com.hsms.userservice.repository.CustomerRepository;
import com.hsms.userservice.repository.TechnicianRepository;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private final CustomerRepository customerRepository;
	private final TechnicianRepository technicianRepository;
	private final AuthFeignClient authFeignClient;
	private final ModelMapper modelMapper;

	public UserServiceImpl(CustomerRepository customerRepository,
							TechnicianRepository technicianRepository,
							AuthFeignClient authFeignClient,
							ModelMapper modelMapper) {
		this.customerRepository = customerRepository;
		this.technicianRepository = technicianRepository;
		this.authFeignClient = authFeignClient;
		this.modelMapper = modelMapper;
	}

	private UserProfileResponseDTO fetchUserProfile(Long userId) {

	    if (userId == null) {
	        log.error("userId is NULL - skipping Feign call");
	        UserProfileResponseDTO fallback = new UserProfileResponseDTO();
	        fallback.setUserId(null);
	        fallback.setName("Unknown User");
	        fallback.setEmail("unknown@example.com");
	        return fallback;
	    }

	    try {
	        return authFeignClient.getUserById(userId);
	    } catch (Exception ex) {
	        log.warn("Failed Feign call for userId={}", userId, ex);
	    }

	    UserProfileResponseDTO fallback = new UserProfileResponseDTO();
	    fallback.setUserId(userId);
	    fallback.setName("Unknown User");
	    fallback.setEmail("unknown@example.com");
	    return fallback;
	}

	@Override
	@Transactional
	public CustomerDetailResponseDTO createCustomer(CustomerProfileRequestDTO dto, Long userId, String email) {
		Customer customer = modelMapper.map(dto, Customer.class);
		customer.setUserId(userId);
		customer.setCreatedAt(LocalDateTime.now());
		
		Customer saved = customerRepository.save(customer);
		UserProfileResponseDTO user = fetchUserProfile(userId);

		CustomerDetailResponseDTO response = modelMapper.map(saved, CustomerDetailResponseDTO.class);
		response.setUserId(userId);
		response.setEmail(email);
		response.setName(user.getName());

		return response;
	}

	@Override
	@Transactional
	public CustomerDetailResponseDTO updateCustomer(Long userId, CustomerProfileRequestDTO dto) {
		Customer customer = customerRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer Not Found"));

		customer.setAddress(dto.getAddress());
		customer.setCity(dto.getCity());
		customer.setPincode(dto.getPincode());
		customer.setCreatedAt(LocalDateTime.now());

		Customer updated = customerRepository.save(customer);
		UserProfileResponseDTO user = fetchUserProfile(userId);

		CustomerDetailResponseDTO response = modelMapper.map(updated, CustomerDetailResponseDTO.class);
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());

		return response;
	}

	@Override
	public CustomerDetailResponseDTO getCustomer(Long userId) {
		Customer customer = customerRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer Not Found"));

		UserProfileResponseDTO user = fetchUserProfile(userId);

		CustomerDetailResponseDTO response = modelMapper.map(customer, CustomerDetailResponseDTO.class);
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());

		return response;
	}

	@Override
	public List<CustomerDetailResponseDTO> getAllCustomers() {
		return customerRepository.findAll().stream().map(customer -> {
			UserProfileResponseDTO user = fetchUserProfile(customer.getUserId());

			CustomerDetailResponseDTO dto = modelMapper.map(customer, CustomerDetailResponseDTO.class);
			dto.setUserId(user.getUserId());
			dto.setName(user.getName());
			dto.setEmail(user.getEmail());

			return dto;
		}).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void deleteCustomer(Long userId) {
		Customer customer = customerRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer Not Found"));

		customerRepository.delete(customer);
	}

	@Override
	public CustomerDetailResponseDTO getCustomerById(Long customerId) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer Not Found"));

		UserProfileResponseDTO user = fetchUserProfile(customer.getUserId());

		CustomerDetailResponseDTO response = modelMapper.map(customer, CustomerDetailResponseDTO.class);
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());

		return response;
	}

	// ================= TECHNICIAN =================

	@Override
	@Transactional
	public TechnicianDetailResponseDTO createTechnician(TechnicianProfileRequestDTO dto) {
		UserProfileResponseDTO user = fetchUserProfile(dto.getUserId());

		Technician technician = modelMapper.map(dto, Technician.class);
		technician.setRating(0.0);
		technician.setTechnicianId(dto.getTechnicianId());

		Technician saved = technicianRepository.save(technician);

		TechnicianDetailResponseDTO response = modelMapper.map(saved, TechnicianDetailResponseDTO.class);
		response.setTechnicianId(saved.getTechnicianId());
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		return response;
	}

	@Override
	@Transactional
	public TechnicianDetailResponseDTO updateTechnician(Long userId, TechnicianProfileRequestDTO dto) {
		Technician technician = technicianRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Technician Not Found"));

		technician.setSkill(dto.getSkill());
		technician.setExperience(dto.getExperience());
		technician.setAvailabilityStatus(dto.getAvailabilityStatus());

		Technician updated = technicianRepository.save(technician);
		UserProfileResponseDTO user = fetchUserProfile(userId);

		TechnicianDetailResponseDTO response = modelMapper.map(updated, TechnicianDetailResponseDTO.class);
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setTechnicianId(updated.getTechnicianId());
		
		return response;
	}

	@Override
	public TechnicianDetailResponseDTO getTechnician(Long userId) {
		Technician technician = technicianRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Technician Not Found"));

		UserProfileResponseDTO user = fetchUserProfile(userId);

		TechnicianDetailResponseDTO response = modelMapper.map(technician, TechnicianDetailResponseDTO.class);
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setTechnicianId(technician.getTechnicianId());

		return response;
	}

	@Override
	public List<TechnicianDetailResponseDTO> getAllTechnicians() {
		return technicianRepository.findAll().stream()
			.filter(Objects::nonNull)
			.filter(technician -> {
				if (technician.getUserId() == null) {
					log.warn("Skipping technician with null userId: {}", technician);
					return false;
				}
				return true;
			})
			.map(technician -> {
				UserProfileResponseDTO user = fetchUserProfile(technician.getUserId());

				TechnicianDetailResponseDTO dto = modelMapper.map(technician, TechnicianDetailResponseDTO.class);
				dto.setUserId(user.getUserId());
				dto.setName(user.getName());
				dto.setEmail(user.getEmail());
				dto.setTechnicianId(technician.getTechnicianId());

				return dto;
			})
			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void deleteTechnician(Long userId) {
		Technician technician = technicianRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Technician Not Found"));

		technicianRepository.delete(technician);
	}

	@Override
	public TechnicianDetailResponseDTO getTechnicianById(Long technicianId) {
		Technician technician = technicianRepository.findByUserId(technicianId)
				.orElseGet(() -> technicianRepository.findById(technicianId)
						.orElseThrow(() -> new ResourceNotFoundException("Technician Not Found")));

		UserProfileResponseDTO user = fetchUserProfile(technician.getUserId());

		TechnicianDetailResponseDTO response = modelMapper.map(technician, TechnicianDetailResponseDTO.class);
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setTechnicianId(technician.getTechnicianId());
		
		return response;
	}

	@Override
	@Transactional
	public TechnicianDetailResponseDTO updateTechnicianRating(Long technicianId, Double rating) {
		Technician technician = technicianRepository.findByUserId(technicianId)
				.orElseGet(() -> technicianRepository.findById(technicianId)
						.orElseThrow(() -> new ResourceNotFoundException("Technician Not Found")));
		technician.setRating(rating);
		
		Technician updated = technicianRepository.save(technician);
		UserProfileResponseDTO user = fetchUserProfile(updated.getUserId());
		
		TechnicianDetailResponseDTO response = modelMapper.map(updated, TechnicianDetailResponseDTO.class);
		response.setUserId(user.getUserId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setTechnicianId(updated.getTechnicianId());
		return response;
	}
}