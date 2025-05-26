package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardServiceImpl;
import com.example.bankcards.util.CardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardControllerImpl implements CardController {

    private final CardServiceImpl cardServiceImpl;
    private final CardMapper cardMapper;

    @Operation(summary = "Создать карту")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно создана",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CardRequestDto request) {
        Card card = cardServiceImpl.createCard(request.getOwner(), request.getCardNumber(), request.getExpirationDate());
        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    @Operation(summary = "Активировать карту")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта активирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(
            @Parameter(description = "ID карты", required = true)
            @PathVariable Long id) {
        cardServiceImpl.activateCard(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Заблокировать карту")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта заблокирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockCardByAdmin(
            @Parameter(description = "ID карты", required = true)
            @PathVariable Long id) {
        cardServiceImpl.blockCard(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить карту")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты", required = true)
            @PathVariable Long id) {
        cardServiceImpl.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все карты")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список всех карт",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<CardResponseDto>> getAllCards() {
        List<CardResponseDto> cards = cardServiceImpl.getAllCards()
                .stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Получить карту по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта найдена",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCardById(
            @Parameter(description = "ID карты", required = true)
            @PathVariable Long id) {
        return cardServiceImpl.getCardById(id)
                .map(cardMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Получить страницы карт пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница карт пользователя",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getUserCards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String username = authentication.getName();
        Page<CardResponseDto> cards = cardServiceImpl.getUserCards(username, PageRequest.of(page, size))
                .map(cardMapper::toDto);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Запрос блокировки своей карты")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта заблокирована"),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content)
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/request-block")
    public ResponseEntity<Void> requestCardBlock(
            @Parameter(description = "ID карты", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        cardServiceImpl.requestCardBlock(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Перевод между своими картами")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса или недостаточно средств",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content)
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @Valid @RequestBody TransferRequestDto request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        cardServiceImpl.transferBetweenOwnCards(username, request.getFromCardId(), request.getToCardId(), request.getAmount());
        return ResponseEntity.ok("Перевод выполнен");
    }

    @Operation(summary = "Получить баланс карты пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс карты",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content)
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}/balance")
    public ResponseEntity<String> getBalance(
            @Parameter(description = "ID карты", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        String balance = cardServiceImpl.getCardBalance(username, id);
        return ResponseEntity.ok(balance);
    }
}
