package com.abc.paymentservice.security;

import java.io.IOException;
import java.util.List;
import javax.crypto.SecretKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

	private static final String SECRET = "VGhpc0lzTXlTZWNyZXRLZXlGb3JKV1RUb2tlbjEyMzQ1Njc4OTA=";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				byte[] keyBytes = Decoders.BASE64.decode(SECRET);
				SecretKey key = Keys.hmacShaKeyFor(keyBytes);
				Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

				String email = claims.getSubject();
				String role = claims.get("role", String.class);
				Object userIdObj = claims.get("userId");
				Long userId = null;
				if (userIdObj instanceof Integer) {
					userId = ((Integer) userIdObj).longValue();
				} else if (userIdObj instanceof Long) {
					userId = (Long) userIdObj;
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
				}
			} catch (Exception e) {
				System.err.println("payment-service: JWT authentication failed: " + e.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}
}
