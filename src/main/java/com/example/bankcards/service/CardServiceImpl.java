package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardEncryptor cardEncryptor;

    public Card createCard(String owner, String plainCardNumber, LocalDate expirationDate) {
        String encrypted = cardEncryptor.encrypt(plainCardNumber);

        Card card = Card.builder()
                .owner(owner)
                .encryptedNumber(encrypted)
                .expirationDate(expirationDate)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        return cardRepository.save(card);
    }

    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }

    public Page<Card> getUserCards(String owner, Pageable pageable) {
        return cardRepository.findByOwner(owner, pageable);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public void updateCardStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        card.setStatus(status);
        cardRepository.save(card);
    }

    public void activateCard(Long cardId) {
        updateCardStatus(cardId, CardStatus.ACTIVE);
    }

    public void blockCard(Long cardId) {
        updateCardStatus(cardId, CardStatus.BLOCKED);
    }

    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }
        cardRepository.deleteById(cardId);
    }

    public void requestCardBlock(String username, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getOwner().equals(username))
                .orElseThrow(CardOwnershipException::new);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public String getCardBalance(String username, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getOwner().equals(username))
                .orElseThrow(CardOwnershipException::new);
        return card.getBalance().toPlainString();
    }

    @Transactional
    public void transferBetweenOwnCards(String owner, Long fromCardId, Long toCardId, BigDecimal amount) {
        if (fromCardId.equals(toCardId)) {
            throw new SameCardTransferException();
        }

        Card from = cardRepository.findById(fromCardId)
                .filter(card -> card.getOwner().equals(owner))
                .orElseThrow(CardOwnershipException::new);

        Card to = cardRepository.findById(toCardId)
                .filter(card -> card.getOwner().equals(owner))
                .orElseThrow(CardOwnershipException::new);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }
}
