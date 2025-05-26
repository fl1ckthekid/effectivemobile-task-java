package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
public interface CardController {

    ResponseEntity<CardResponseDto> createCard(@RequestBody CardRequestDto request);

    ResponseEntity<Void> activateCard(@PathVariable Long id);

    ResponseEntity<Void> blockCardByAdmin(@PathVariable Long id);

    ResponseEntity<Void> deleteCard(@PathVariable Long id);

    ResponseEntity<List<CardResponseDto>> getAllCards();

    ResponseEntity<CardResponseDto> getCardById(@PathVariable Long id);

    ResponseEntity<Page<CardResponseDto>> getUserCards(Authentication authentication,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size);

    ResponseEntity<Void> requestCardBlock(@PathVariable Long id, Authentication authentication);

    ResponseEntity<String> transfer(@RequestBody TransferRequestDto request, Authentication authentication);

    ResponseEntity<String> getBalance(@PathVariable Long id, Authentication authentication);
}
