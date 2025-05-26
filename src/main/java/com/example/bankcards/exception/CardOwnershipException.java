package com.example.bankcards.exception;

public class CardOwnershipException extends RuntimeException {
    public CardOwnershipException() {
        super("Карта не найдена или не принадлежит вам");
    }
}
