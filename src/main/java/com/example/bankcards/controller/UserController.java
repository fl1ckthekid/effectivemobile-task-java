package com.example.bankcards.controller;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
public interface UserController {

    ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto dto);

    ResponseEntity<List<UserResponseDto>> getAllUsers();

    ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id);

    ResponseEntity<Void> deleteUser(@PathVariable Long id);
}
