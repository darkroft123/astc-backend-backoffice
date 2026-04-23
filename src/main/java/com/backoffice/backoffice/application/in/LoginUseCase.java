package com.backoffice.backoffice.application.in;

public interface LoginUseCase {
    String execute(String email, String password);
}