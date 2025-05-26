package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
public interface AuthController {

    ResponseEntity<String> register(@RequestParam String username, @RequestParam String password);

    ResponseEntity<String> login(@RequestParam String username, @RequestParam String password);
}
