package com.hsms.userservice.security;

import java.io.IOException;
import java.util.List;
import javax.crypto.SecretKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);
	private static final String SIGNING_KEY = "VGhpc0lzTXlTZWNyZXRLZXlGb3JKV1RUb2tlbjEyMzQ1Njc4OTA=";
	private final SecretKey signKey;

	public HeaderAuthenticationFilter() {
		byte[] keyBytes = Decoders.BASE64.decode(SIGNING_KEY);
		this.signKey = Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				Claims claims = Jwts.parser().verifyWith(signKey).build().parseSignedClaims(token).getPayload();

				String email = claims.getSubject();
				String role = claims.get("role", String.class);
				Object userIdObj = claims.get("userId");
				Long userId = null;
				if (userIdObj != null) {
					try {
						userId = Long.valueOf(userIdObj.toString());
					} catch (NumberFormatException e) {
						log.error("Failed to parse userId in JWT token: {}", userIdObj);
					}
				}

				if (email != null && role != null) {
					String roleName = role.toUpperCase();
					if (roleName.startsWith("ROLE_")) {
						roleName = roleName.substring(5);
					}

					CustomPrincipal principal = new CustomPrincipal(userId, email, roleName);
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							principal, null, List.of(new SimpleGrantedAuthority(roleName)));

					SecurityContextHolder.getContext().setAuthentication(authentication);
					log.debug("user-service successfully set SecurityContext with principal: {}", principal);
				}
			} catch (Exception e) {
				log.error("user-service: JWT authentication failed: {}", e.getMessage());
			}
		} else {
			log.debug("user-service: Authorization header was missing or invalid!");
		}

		filterChain.doFilter(request, response);
	}
}