package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardService {

    Card createCard(String owner, String plainCardNumber, LocalDate expirationDate);

    Optional<Card> getCardById(Long id);

    Page<Card> getUserCards(String owner, Pageable pageable);

    List<Card> getAllCards();

    void updateCardStatus(Long cardId, CardStatus status);

    void activateCard(Long cardId);

    void blockCard(Long cardId);

    void deleteCard(Long cardId);

    void requestCardBlock(String username, Long cardId);

    String getCardBalance(String username, Long cardId);

    void transferBetweenOwnCards(String owner, Long fromCardId, Long toCardId, BigDecimal amount);
}
