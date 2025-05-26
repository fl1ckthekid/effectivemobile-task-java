package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardEncryptor cardEncryptor;

    @InjectMocks
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_success() {
        String owner = "testUser";
        String plainCardNumber = "1234567890123456";
        String encrypted = "encryptedCard";
        LocalDate expiration = LocalDate.now().plusYears(2);

        when(cardEncryptor.encrypt(plainCardNumber)).thenReturn(encrypted);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.createCard(owner, plainCardNumber, expiration);

        assertEquals(owner, result.getOwner());
        assertEquals(encrypted, result.getEncryptedNumber());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void getCardById_found() {
        Card card = Card.builder().id(1L).owner("user").build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Optional<Card> result = cardService.getCardById(1L);

        assertTrue(result.isPresent());
        assertEquals("user", result.get().getOwner());
    }

    @Test
    void updateCardStatus_cardExists_statusUpdated() {
        Card card = Card.builder().id(1L).status(CardStatus.BLOCKED).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.updateCardStatus(1L, CardStatus.ACTIVE);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void updateCardStatus_cardNotFound_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.updateCardStatus(1L, CardStatus.ACTIVE));
    }

    @Test
    void deleteCard_exists_deletesSuccessfully() {
        when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.deleteCard(1L);

        verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCard_notFound_throwsException() {
        when(cardRepository.existsById(1L)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(1L));
    }

    @Test
    void requestCardBlock_correctUser_blocksCard() {
        Card card = Card.builder().id(1L).owner("alice").status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.requestCardBlock("alice", 1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void requestCardBlock_wrongUser_throwsException() {
        Card card = Card.builder().id(1L).owner("bob").build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardOwnershipException.class, () -> cardService.requestCardBlock("alice", 1L));
    }

    @Test
    void getCardBalance_correctUser_returnsBalance() {
        Card card = Card.builder().id(1L).owner("alice").balance(new BigDecimal("100.50")).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        String balance = cardService.getCardBalance("alice", 1L);

        assertEquals("100.50", balance);
    }

    @Test
    void getCardBalance_wrongUser_throwsException() {
        Card card = Card.builder().id(1L).owner("bob").build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardOwnershipException.class, () -> cardService.getCardBalance("alice", 1L));
    }

    @Test
    void transferBetweenOwnCards_successfulTransfer() {
        Card from = Card.builder().id(1L).owner("user").balance(new BigDecimal("100")).build();
        Card to = Card.builder().id(2L).owner("user").balance(new BigDecimal("50")).build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        cardService.transferBetweenOwnCards("user", 1L, 2L, new BigDecimal("30"));

        assertEquals(new BigDecimal("70"), from.getBalance());
        assertEquals(new BigDecimal("80"), to.getBalance());
        verify(cardRepository).save(from);
        verify(cardRepository).save(to);
    }

    @Test
    void transferBetweenOwnCards_insufficientFunds_throwsException() {
        Card from = Card.builder().id(1L).owner("user").balance(new BigDecimal("20")).build();
        Card to = Card.builder().id(2L).owner("user").balance(new BigDecimal("50")).build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(InsufficientFundsException.class,
                () -> cardService.transferBetweenOwnCards("user", 1L, 2L, new BigDecimal("30")));
    }

    @Test
    void transferBetweenOwnCards_sameCard_throwsException() {
        assertThrows(SameCardTransferException.class,
                () -> cardService.transferBetweenOwnCards("user", 1L, 1L, new BigDecimal("10")));
    }
}
