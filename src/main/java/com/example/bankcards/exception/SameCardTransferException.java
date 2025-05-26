package com.example.bankcards.exception;

public class SameCardTransferException extends RuntimeException {
    public SameCardTransferException() {
        super("Нельзя перевести на ту же карту");
    }
}
