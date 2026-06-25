package com.hsms.authservice.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hsms.authservice.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {

	@Value("${app.jwt-secret}")
	private String jwtSecret;

	@Value("${app.jwt-expiration-milliseconds}")
	private long jwtExpirationDate;

	private SecretKey signKey;

	@jakarta.annotation.PostConstruct
	public void init() {
		this.signKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}

	public String generateToken(User user) {
		String role = user.getRoles().iterator().next().getRoleName();

		return Jwts.builder()
				.subject(user.getEmail())
				.claim("userId", user.getUserId())
				.claim("role", role)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtExpirationDate))
				.signWith(signKey)
				.compact();
	}

	public boolean validateToken(String token) {
		Jwts.parser().verifyWith(signKey).build().parseSignedClaims(token);
		return true;
	}

	public String getUsername(String token) {
		return Jwts.parser().verifyWith(signKey).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public Long getUserId(String token) {
		return Jwts.parser().verifyWith(signKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
	}

	public String getRole(String token) {
		return Jwts.parser().verifyWith(signKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
	}
}