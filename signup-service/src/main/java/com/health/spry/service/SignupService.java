package com.health.spry.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.health.spry.dto.SignupRequest;
import com.health.spry.dto.SignupResponse;
import com.health.spry.exception.UserAlreadyExistsException;
import com.health.spry.model.User;
import com.health.spry.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

	@Autowired
    private final UserRepository userRepository;
    
	@Autowired
	private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse registerUser(SignupRequest request) {
        log.info("Attempting to register user with username: {}", request.getUsername());

        // Check if username or Email already exists
        if (userRepository.existsByUsernameOrEmail(request.getUsername(),request.getEmail())) {
            log.error("Username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("User with the Username or Email already Exists");
        }

       

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        return SignupResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .createdAt(savedUser.getCreatedAt())
                .message("User registered successfully")
                .build();
    }
}
