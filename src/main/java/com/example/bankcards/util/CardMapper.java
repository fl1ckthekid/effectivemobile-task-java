package com.example.bankcards.util;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CardMapper {

    private final CardEncryptor cardEncryptor;

    public CardResponseDto toDto(Card card) {
        String decrypted = cardEncryptor.decrypt(card.getEncryptedNumber());
        return CardResponseDto.builder()
                .id(card.getId())
                .maskedNumber(maskCardNumber(decrypted))
                .owner(card.getOwner())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }

    private String maskCardNumber(String plainCardNumber) {
        String lastFour = plainCardNumber.length() > 4
                ? plainCardNumber.substring(plainCardNumber.length() - 4)
                : plainCardNumber;
        return "**** **** **** " + lastFour;
    }
}

