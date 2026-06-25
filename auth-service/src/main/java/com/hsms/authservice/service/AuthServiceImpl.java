package com.hsms.authservice.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hsms.authservice.entity.Role;
import com.hsms.authservice.entity.User;
import com.hsms.authservice.exception.InvalidCredentialsException;
import com.hsms.authservice.exception.ResourceAlreadyExistsException;
import com.hsms.authservice.exception.ResourceNotFoundException;
import com.hsms.authservice.model.LoginRequestDTO;
import com.hsms.authservice.model.LoginResponseDTO;
import com.hsms.authservice.model.RegisterRequestDTO;
import com.hsms.authservice.model.RegisterResponseDTO;
import com.hsms.authservice.repository.RoleRepository;
import com.hsms.authservice.repository.UserRepository;
import com.hsms.authservice.security.JwtTokenUtil;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder encoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email Already Exists");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));

        // Default Role = CUSTOMER
        String roleName = dto.getRole();
        if (roleName == null || roleName.isBlank()) {
            roleName = "CUSTOMER";
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role Not Found"));

        user.getRoles().add(role);

        User saved = userRepository.save(user);

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setUserId(saved.getUserId());
        response.setMessage("Registration Successful. Hello " + saved.getFirstName());
        return response;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid Email or Password"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        String token = jwtUtil.generateToken(user);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setRole(user.getRoles().iterator().next().getRoleName());

        return response;
    }
}