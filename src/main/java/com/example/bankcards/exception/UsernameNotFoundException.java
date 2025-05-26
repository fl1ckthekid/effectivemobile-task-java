package com.example.bankcards.exception;

public class UsernameNotFoundException extends RuntimeException {

    public UsernameNotFoundException() {
        super("Имя пользователя не найдено");
    }
}
