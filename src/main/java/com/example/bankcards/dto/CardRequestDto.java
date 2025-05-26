package com.example.bankcards.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardRequestDto {

    @NotBlank(message = "Владелец карты обязателен")
    private String owner;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @Future(message = "Дата истечения срока должна быть в будущем")
    private LocalDate expirationDate;
}
