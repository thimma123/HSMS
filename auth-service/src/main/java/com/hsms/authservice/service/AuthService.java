package com.hsms.authservice.service;

import com.hsms.authservice.model.LoginRequestDTO;
import com.hsms.authservice.model.LoginResponseDTO;
import com.hsms.authservice.model.RegisterRequestDTO;
import com.hsms.authservice.model.RegisterResponseDTO;

public interface AuthService {

	RegisterResponseDTO register(RegisterRequestDTO dto);

    LoginResponseDTO login(LoginRequestDTO dto);
}
