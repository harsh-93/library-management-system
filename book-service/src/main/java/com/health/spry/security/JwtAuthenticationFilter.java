package com.health.spry.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		final String authorizationHeader = request.getHeader("Authorization");
		final int authErrorCode = HttpStatus.UNAUTHORIZED.value();

		String username = null;
		String jwt = null;

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			// Extract JWT token from Authorization header

			jwt = authorizationHeader.substring(7);
			username = jwtUtil.extractUsername(jwt);
			log.debug("Extracted username from JWT: {}", username);
			
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				// Validate token and set authentication
				if (jwtUtil.validateToken(jwt, username)) {
					UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
							username, null, new ArrayList<>()); // Here since there is not Authorizatoin flow involved,
																// hence Empty arraylist for Granted authorities

					authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
					log.debug("Successfully authenticated user: {}", username);
				}

			}

		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
			setErrorResponse(request, authErrorCode, "Token has expired");
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
			setErrorResponse(request, authErrorCode, "Malformed token");
		} catch (SignatureException e) {
			log.error("JWT signature does not match: {}", e.getMessage());
			setErrorResponse(request, authErrorCode,
					"Token signature verification failed - token may have been tampered with");
		} catch (JwtException e) {
			log.error("JWT token validation error: {}", e.getMessage());
			setErrorResponse(request, authErrorCode, "Token validation failed: " + e.getMessage());
		} catch (Exception e) {
			log.error("Error processing JWT: {}", e.getMessage());
			setErrorResponse(request, authErrorCode, "Error processing token");
		}



		filterChain.doFilter(request, response);
	}

	private void setErrorResponse(HttpServletRequest request, int errorCode, String errorMessage) {
		request.setAttribute("errorCode", String.valueOf(errorCode));
		request.setAttribute("errorMessage", errorMessage);
	}
}
