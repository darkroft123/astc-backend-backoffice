package com.backoffice.backoffice.application.in;

public record LoginCommand(
        String username,
        String password) {
}