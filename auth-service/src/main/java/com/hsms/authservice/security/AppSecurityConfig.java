package com.hsms.authservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class AppSecurityConfig {

	private final JwtAuthenticationFilter authenticationFilter;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;

	public AppSecurityConfig(JwtAuthenticationFilter authenticationFilter,
							JwtAuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationFilter = authenticationFilter;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(auth ->

		auth.requestMatchers("/api/auth/**").permitAll()


				.requestMatchers("/api/admin/**").hasAuthority("ADMIN")

				.requestMatchers("/api/customer/**").hasAuthority("CUSTOMER")

				.requestMatchers("/api/technician/**").hasAuthority("TECHNICIAN")

				.requestMatchers("/api/service-manager/**").hasAuthority("SERVICE_MANAGER")

				.anyRequest().authenticated());

		http.csrf(csrf -> csrf.disable());

		http.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint));

		http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

		return configuration.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}
}