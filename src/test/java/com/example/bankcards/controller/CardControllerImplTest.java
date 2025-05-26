package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.CardServiceImpl;
import com.example.bankcards.util.CardMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CardControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardServiceImpl cardServiceImpl;

    @MockBean
    private CardMapper cardMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void createCard_shouldReturnCardResponseWithMaskedNumber() throws Exception {
        CardRequestDto request = new CardRequestDto();
        request.setOwner("user1");
        request.setCardNumber("1234567890123456");
        request.setExpirationDate(LocalDate.of(2030, 12, 31));

        Card card = new Card();
        card.setId(1L);
        card.setOwner("user1");
        card.setEncryptedNumber("encryptedCardNumber");

        CardResponseDto responseDto = CardResponseDto.builder()
                .id(1L)
                .owner("user1")
                .maskedNumber("**** **** **** 3456")
                .expirationDate(LocalDate.of(2030, 12, 31))
                .status(null)
                .balance(null)
                .build();

        when(cardServiceImpl.createCard(anyString(), anyString(), any(LocalDate.class))).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(responseDto);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.owner").value(responseDto.getOwner()))
                .andExpect(jsonPath("$.maskedNumber").value(responseDto.getMaskedNumber()));
    }

    @Test
    void activateCard_shouldReturnOk() throws Exception {
        doNothing().when(cardServiceImpl).activateCard(1L);

        mockMvc.perform(post("/api/cards/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void blockCardByAdmin_shouldReturnOk() throws Exception {
        doNothing().when(cardServiceImpl).blockCard(1L);

        mockMvc.perform(post("/api/cards/1/block"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCard_shouldReturnNoContent() throws Exception {
        doNothing().when(cardServiceImpl).deleteCard(1L);

        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllCards_shouldReturnList() throws Exception {
        Card card1 = new Card();
        card1.setId(1L);
        Card card2 = new Card();
        card2.setId(2L);

        CardResponseDto dto1 = new CardResponseDto();
        dto1.setId(1L);
        CardResponseDto dto2 = new CardResponseDto();
        dto2.setId(2L);

        when(cardServiceImpl.getAllCards()).thenReturn(List.of(card1, card2));
        when(cardMapper.toDto(card1)).thenReturn(dto1);
        when(cardMapper.toDto(card2)).thenReturn(dto2);

        mockMvc.perform(get("/api/cards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()));
    }

    @Test
    void getCardById_found_shouldReturnCard() throws Exception {
        Card card = new Card();
        card.setId(1L);

        CardResponseDto dto = new CardResponseDto();
        dto.setId(1L);

        when(cardServiceImpl.getCardById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(dto);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()));
    }

    @Test
    void getCardById_notFound_shouldReturn404() throws Exception {
        when(cardServiceImpl.getCardById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserCards_shouldReturnPagedCards() throws Exception {
        Authentication auth = new TestingAuthenticationToken("user1", null);

        Card card = new Card();
        card.setId(1L);
        CardResponseDto dto = new CardResponseDto();
        dto.setId(1L);

        when(cardServiceImpl.getUserCards(eq("user1"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.toDto(card)).thenReturn(dto);

        mockMvc.perform(get("/api/cards")
                        .principal(auth)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dto.getId()));
    }

    @Test
    void requestCardBlock_shouldReturnOk() throws Exception {
        Authentication auth = new TestingAuthenticationToken("user1", null);

        doNothing().when(cardServiceImpl).requestCardBlock("user1", 1L);

        mockMvc.perform(post("/api/cards/1/request-block")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void transfer_shouldReturnOk() throws Exception {
        Authentication auth = new TestingAuthenticationToken("user1", null);

        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100.0));

        doNothing().when(cardServiceImpl).transferBetweenOwnCards("user1", 1L, 2L, BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/cards/transfer")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод выполнен"));
    }

    @Test
    void getBalance_shouldReturnBalance() throws Exception {
        Authentication auth = new TestingAuthenticationToken("user1", null);

        when(cardServiceImpl.getCardBalance("user1", 1L)).thenReturn("1000.00");

        mockMvc.perform(get("/api/cards/1/balance")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.00"));
    }
}
