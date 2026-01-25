package com.health.spry.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Signup", description = "User registration and signup APIs")
public class SignupController {
	
}
